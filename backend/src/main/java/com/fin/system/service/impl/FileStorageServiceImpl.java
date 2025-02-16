package com.fin.system.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fin.system.Mapper.FileStorageMapper;
import com.fin.system.commen.R;
import com.fin.system.dto.FileChunkDto;
import com.fin.system.entity.FileChunk;
import com.fin.system.entity.FileStorage;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.FileChunkService;
import com.fin.system.service.FileStorageService;
import com.fin.system.util.BulkFileUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文件存储表(FileStorage)表服务实现类
 */
@Service()
@Slf4j
public class FileStorageServiceImpl extends ServiceImpl<FileStorageMapper, FileStorage> implements FileStorageService {

    /**
     * 默认分块大小
     */
    @Value("${file.chunk-size}")
    public Long defaultChunkSize;
    /**
     * 上传地址
     */
    @Value("${file.path}")
    private String baseFileSavePath;

    @Resource
    FileChunkService fileChunkService;
    private final ConcurrentHashMap<String, ReentrantLock> mergeLocks = new ConcurrentHashMap<>();

    @Override
    public R<List<FileStorage>> getFileList(HttpServletRequest request, String parent) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            return R.error("用户未登录");
        }
        UserInfo user = (UserInfo) session.getAttribute("userInfo");
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileStorage::getCreateById, user.userId);
        List<FileStorage> fileInfoList = this.list(queryWrapper);
        List<FileStorage> matchedFiles = fileInfoList.stream()
                .filter(file -> matchStringWithoutSlash(file.getFilePath(), parent))
                .sorted(Comparator.comparing((FileStorage f) -> f.getFileType().equals("dir") ? 0 : 1)
                        .thenComparing(FileStorage::getFilePath))
                .collect(Collectors.toList());
        return R.success(matchedFiles);
    }

    @Override
    public Boolean uploadFile(FileChunkDto dto) {
        // 简单校验，如果是正式项目，要考虑其他数据的校验
        if (dto.getFile() == null) {
            throw new RuntimeException("文件不能为空");
        }
        Path tempDir = Paths.get(baseFileSavePath, "temp", dto.getIdentifier());
        Path finalFile = Paths.get(baseFileSavePath, dto.getIdentifier());
        try {
            // 创建目录
            if (!Files.exists(tempDir)) {
                Files.createDirectories(tempDir);
            }

            // 保存分片
            Path chunkFile = tempDir.resolve(dto.getChunkNumber() + ".chunk");
            Files.copy(dto.getFile().getInputStream(), chunkFile, StandardCopyOption.REPLACE_EXISTING);
            FileChunk chunk = BeanUtil.copyProperties(dto, FileChunk.class);
            chunk.setUpdateBy(chunk.getCreateBy());
            fileChunkService.save(chunk);
            // 检查是否所有分片上传完成
            if (isUploadComplete(tempDir, dto.getTotalChunks())) {
                //防止并发重复操作
                if (Files.exists(finalFile)) return true;
                mergeChunks(dto.getIdentifier(), dto.getTotalChunks(), tempDir, finalFile);
                this.saveFile(dto, finalFile.toAbsolutePath().toString());
            }
            return true;
        } catch (IOException e) {
            throw new RuntimeException("文件操作失败", e);
        }
    }

//    /**
//     * 分片上传方法
//     * 这里使用 RandomAccessFile 方法，也可以使用 MappedByteBuffer 方法上传
//     * 可以省去文件合并的过程
//     *
//     * @param fullFileName 文件名
//     * @param dto          文件dto
//     */
//    private Boolean uploadSharding(String fullFileName, FileChunkDto dto) {
//        // try 自动资源管理
//        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fullFileName, "rw")) {
//            // 分片大小必须和前端匹配，否则上传会导致文件损坏
//            long chunkSize = dto.getChunkSize() == 0L ? defaultChunkSize : dto.getChunkSize().longValue();
//            // 偏移量, 意思是我从拿一个位置开始往文件写入，每一片的大小 * 已经存的块数
//            long offset = chunkSize * (dto.getChunkNumber() - 1);
//            // 定位到该分片的偏移量
//            randomAccessFile.seek(offset);
//            // 写入
//            randomAccessFile.write(dto.getFile().getBytes());
//        } catch (IOException e) {
//            log.error("文件上传失败：" + e);
//            return Boolean.FALSE;
//        }
//        return Boolean.TRUE;
//    }

    private boolean isUploadComplete(Path tempDir, int totalChunks) throws IOException {
        try (Stream<Path> files = Files.list(tempDir)) {
            long chunkCount = files.count();
            return chunkCount == totalChunks;
        }
    }

    private void mergeChunks(String identifier, int totalChunks, Path tempDir, Path finalFile) {
        ReentrantLock lock = mergeLocks.computeIfAbsent(identifier, k -> new ReentrantLock());
        lock.lock();
        try {
            try (BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(finalFile.toFile()))) {

                for (int i = 1; i <= totalChunks; i++) {
                    Path chunk = tempDir.resolve(i + ".chunk");
                    try (BufferedInputStream bis = new BufferedInputStream(
                            new FileInputStream(chunk.toFile()))) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = bis.read(buffer)) > 0) {
                            bos.write(buffer, 0, len);
                        }
                    }
                }
            }
            // 清理临时文件
            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (IOException e) {
            throw new RuntimeException("合并文件失败", e);
        } finally {
            lock.unlock();
            mergeLocks.remove(identifier);
        }
    }

    @SneakyThrows
    @Override
    public void downloadByIdentifier(String id, HttpServletRequest request, HttpServletResponse response, String range) {
        response.setHeader("Accept-Ranges", "bytes");
        FileStorage file = this.getOne(new LambdaQueryWrapper<FileStorage>()
                .eq(FileStorage::getId, id));

        if (BeanUtil.isNotEmpty(file)) {
            File toFile = new File(baseFileSavePath + File.separator + file.getIdentifier());
            String fileName = new String(file.getRealName().getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

            long fileLength = toFile.length();
            Long start = null;
            Long end = null;

            // 判断是否带有 Range 头
            if (range != null && range.startsWith("bytes=")) {
                String[] ranges = range.substring("bytes=".length()).split("-");
                start = Long.parseLong(ranges[0]);
                end = ranges.length > 1 ? Long.parseLong(ranges[1]) : fileLength - 1;
            }

            // 调用简化后的 downloadFile 方法
            BulkFileUtil.downloadFile(request, response, toFile, fileName, start, end);
        } else {
            throw new RuntimeException("文件不存在");
        }
    }

    private void saveFile(FileChunkDto dto, String fileName) {
        String name = dto.getFileName();
        MultipartFile file = dto.getFile();
        FileStorage fileStorage = new FileStorage();
        fileStorage.setRealName(file.getOriginalFilename());
        fileStorage.setFileName(fileName);
        fileStorage.setSuffix(FileUtil.getSuffix(name));
        fileStorage.setFileType(file.getContentType());
        fileStorage.setSize(dto.getTotalSize());
        fileStorage.setIdentifier(dto.getIdentifier());
        fileStorage.setCreateBy(dto.getCreateBy());
        fileStorage.setUpdateBy(dto.getCreateBy());
        fileStorage.setCreateById(dto.getCreateById());
        if (Objects.equals(dto.getDirPath(), "")) {
            fileStorage.setFilePath(dto.getRelativePath());
        } else {
            fileStorage.setFilePath(dto.getDirPath() + "/" + dto.getRelativePath());
        }
        this.save(fileStorage);
    }

    private boolean matchStringWithoutSlash(String input, String patternStr) {
        if (!Objects.equals(patternStr, "")) {
            patternStr += "/";
        }
        // 使用正则表达式构造匹配模式
        String regex = Pattern.quote(patternStr) + "(?!.*[/]).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        // 返回匹配结果
        return matcher.matches();
    }

    @Scheduled(cron = "0 0 2 * * ?") // 每天凌晨2点执行
    public void cleanExpiredTempFiles() {
        Path tempDir = Paths.get(baseFileSavePath, "temp");
        if (!Files.exists(tempDir)) {
            return;
        }

        try (Stream<Path> dirStream = Files.list(tempDir)) {
            dirStream.filter(Files::isDirectory)
                    .forEach(this::cleanExpiredDirectory);
        } catch (IOException e) {
            log.error("清理临时文件失败", e);
        }
    }

    private void cleanExpiredDirectory(Path dir) {
        try {
            // 获取目录的最后修改时间
            Instant lastModified = Files.getLastModifiedTime(dir).toInstant();
            Instant now = Instant.now();

            // 如果目录超过24小时未修改，则删除
            if (Duration.between(lastModified, now).toHours() > 24) {
                FileUtils.deleteDirectory(dir.toFile());
                log.info("已清理过期临时目录: {}", dir);
            }
        } catch (IOException e) {
            log.error("清理临时目录失败: {}", dir, e);
        }
    }
}