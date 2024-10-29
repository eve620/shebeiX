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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件存储表(FileStorage)表服务实现类
 *
 * @author 散装java
 * @since 2022-11-15 17:49:41
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
        Path rootPath = Paths.get(baseFileSavePath);
        String fullFileName = rootPath.toAbsolutePath().resolve(dto.getIdentifier()).toString();
        // 检查目录是否存在，不存在则创建
        if (!rootPath.toFile().exists()) {
            try {
                Files.createDirectories(rootPath); // 注意：这会递归创建所有必需的父目录
            } catch (IOException e) {
                // 处理创建目录失败的情况，例如记录日志或抛出自定义异常
                throw new RuntimeException("创建文件保存目录失败：" + e.getMessage());
            }
        }
        Boolean uploadFlag;
        // 如果是单文件上传
        if (dto.getTotalChunks() == 1) {
            uploadFlag = this.uploadSingleFile(fullFileName, dto);
        } else {
            // 分片上传
            uploadFlag = this.uploadSharding(fullFileName, dto);
        }
        // 如果本次上传成功则存储数据到 表中
        if (uploadFlag) {
            this.saveFile(dto, fullFileName);
        }
        return uploadFlag;
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

    /**
     * 分片上传方法
     * 这里使用 RandomAccessFile 方法，也可以使用 MappedByteBuffer 方法上传
     * 可以省去文件合并的过程
     *
     * @param fullFileName 文件名
     * @param dto          文件dto
     */
    private Boolean uploadSharding(String fullFileName, FileChunkDto dto) {
        // try 自动资源管理
        try (RandomAccessFile randomAccessFile = new RandomAccessFile(fullFileName, "rw")) {
            // 分片大小必须和前端匹配，否则上传会导致文件损坏
            long chunkSize = dto.getChunkSize() == 0L ? defaultChunkSize : dto.getChunkSize().longValue();
            // 偏移量, 意思是我从拿一个位置开始往文件写入，每一片的大小 * 已经存的块数
            long offset = chunkSize * (dto.getChunkNumber() - 1);
            // 定位到该分片的偏移量
            randomAccessFile.seek(offset);
            // 写入
            randomAccessFile.write(dto.getFile().getBytes());
        } catch (IOException e) {
            log.error("文件上传失败：" + e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @SneakyThrows
    private Boolean uploadSingleFile(String fullFileName, FileChunkDto dto) {
        File localPath = new File(fullFileName);
        dto.getFile().transferTo(localPath);
        return Boolean.TRUE;
    }

    private void saveFile(FileChunkDto dto, String fileName) {

        FileChunk chunk = BeanUtil.copyProperties(dto, FileChunk.class);
        chunk.setUpdateBy(chunk.getCreateBy());
        fileChunkService.save(chunk);
        // 如果所有快都上传完成，那么在文件记录表中存储一份数据
        // todo 这里最好每次上传完成都存一下缓存，从缓存中查看是否所有块上传完成，这里偷懒了
        if (dto.getChunkNumber().equals(dto.getTotalChunks())) {
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
    }

    private boolean matchStringWithoutSlash(String input, String patternStr) {
        if (!Objects.equals(patternStr, "")) {
            patternStr += "/";
        }
        // 使用正则表达式构造匹配模式
        String regex = patternStr + "(?!.*[/]).*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);

        // 返回匹配结果
        return matcher.matches();
    }
}

