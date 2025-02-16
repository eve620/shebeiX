package com.fin.system.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import com.alibaba.excel.util.FieldUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.dto.FileChunkDto;
import com.fin.system.entity.FileChunk;
import com.fin.system.entity.FileStorage;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.FileChunkService;
import com.fin.system.service.FileStorageService;
import com.fin.system.vo.CheckResultVo;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 文件存储表(FileStorage)表控制层
 *
 * @author 散装java
 * @since 2022-11-15 17:49:39
 */
@RestController
@RequestMapping("fileStorage")
public class FileStorageController {
    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private FileChunkService fileChunkService;
    @Value("${file.path}")
    private String baseFileSavePath;

    @GetMapping("/list")
    public R<List<FileStorage>> list(HttpServletRequest request, @RequestParam String parent) {
        return fileStorageService.getFileList(request, parent);
    }

    /**
     * 本接口为实际上传接口
     *
     * @param dto      入参
     * @param response response 配合前端返回响应的状态码
     * @return boolean
     */
    @PostMapping("/upload")
    public R<Boolean> upload(HttpServletRequest request, FileChunkDto dto, HttpServletResponse response) {
        UserInfo user = getCurrentUser(request);
        if (user == null) {
            return R.error("用户未登录");
        }
        dto.setCreateBy(user.userName);
        dto.setCreateById(user.userId);
        try {
            Boolean status = fileStorageService.uploadFile(dto);
            if (status) {
                return R.ok("上传成功");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return R.error("上传失败");
            }
        } catch (Exception e) {
            // 这个code 是根据前端组件的特性来的，也可以自己定义规则
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return R.error("上传失败");
        }
    }

    @GetMapping("/check")
    public R<CheckResultVo> checkUpload(HttpServletRequest request, String md5, String filePath) {
        UserInfo user = getCurrentUser(request);
        if (user == null) {
            return R.error("用户未登录");
        }
        LambdaQueryWrapper<FileChunk> chunkQueryWrapper = new LambdaQueryWrapper<>();
        chunkQueryWrapper.eq(FileChunk::getIdentifier, md5);
        //获取所有chunks及chunk的号码
        List<FileChunk> uploadedChunks = fileChunkService.list(chunkQueryWrapper);
        List<Integer> chunkNums = uploadedChunks.stream()
                .map(FileChunk::getChunkNumber)
                .toList();
        CheckResultVo res = new CheckResultVo();
        res.setUploaded(false);
        res.setUploadedChunks(chunkNums);
        if (chunkNums.isEmpty()) return R.success(res);
        //在storage里查询
        LambdaQueryWrapper<FileStorage> storageQueryWrapper = new LambdaQueryWrapper<>();
        storageQueryWrapper.eq(FileStorage::getIdentifier, md5);
        List<FileStorage> storages = fileStorageService.list(storageQueryWrapper);
        if (!storages.isEmpty()) {
            res.setUploaded(true);
            for (FileStorage storage : storages) {
                if (Objects.equals(storage.getFilePath(), filePath) && Objects.equals(storage.getCreateById(), user.userId)) {
                    return R.success(res);
                }
            }
            FileStorage storageCopy = BeanUtil.copyProperties(storages.get(0), FileStorage.class);
            storageCopy.setId(null);
            storageCopy.setCreateBy(user.userName);
            storageCopy.setFilePath(filePath);
            Path path = Paths.get(filePath);
            Path fileName = path.getFileName();
            storageCopy.setRealName(fileName.toString());
            storageCopy.setCreateBy(user.userName);
            storageCopy.setCreateById(user.userId);
            storageCopy.setUpdateBy(user.userName);
            storageCopy.setCreateTime(null);
            storageCopy.setUpdateTime(null);
            fileStorageService.save(storageCopy);
        }
        return R.success(res);
    }

    @PostMapping("/createDir")
    public R<String> createDir(HttpServletRequest request, @RequestBody String dirPath) {
        UserInfo user = getCurrentUser(request);
        if (user == null) {
            return R.error("用户未登录");
        }
        //检查是否已创建
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileStorage::getFileType, "dir");
        queryWrapper.eq(FileStorage::getFilePath, dirPath);
        queryWrapper.eq(FileStorage::getCreateById, user.userId);
        FileStorage storage = fileStorageService.getOne(queryWrapper);
        if (storage != null) return R.success("文件夹已创建");

        Path filePath = Paths.get(dirPath);
        String dirName = filePath.getFileName().toString();
        FileStorage fileStorage = new FileStorage();
        fileStorage.setRealName(dirName);
        fileStorage.setFileType("dir");
        fileStorage.setFilePath(dirPath);
        fileStorage.setCreateBy(user.userName);
        fileStorage.setCreateById(user.userId);
        fileStorage.setUpdateBy(user.userName);
        fileStorageService.save(fileStorage);
        return R.success("文件夹创建成功");
    }

    @DeleteMapping(value = "/delete")
    public R<String> deleteItem(HttpServletRequest request, String deleteId) {
        UserInfo user = getCurrentUser(request);
        if (user == null) {
            return R.error("用户未登录");
        }
        LambdaQueryWrapper<FileStorage> storageQueryWrapper = new LambdaQueryWrapper<>();
        storageQueryWrapper.eq(FileStorage::getId, deleteId);
        FileStorage storage = fileStorageService.getOne(storageQueryWrapper);
        //删除的是文件夹
        if (storage.getIdentifier() == null) {
            String filePath = storage.getFilePath();
            LambdaQueryWrapper<FileStorage> sonQueryWrapper = new LambdaQueryWrapper<>();
            sonQueryWrapper.likeRight(FileStorage::getFilePath, filePath + "/");
            sonQueryWrapper.eq(FileStorage::getCreateById, user.userId);
            List<FileStorage> sonList = fileStorageService.list(sonQueryWrapper);
            if (!sonList.isEmpty()) {
                //递归调用逻辑删除
                for (FileStorage item : sonList) {
                    if (item.getIdentifier() == null) {
                        fileStorageService.removeById(item.getId());
                    } else {
                        deleteFile(item);
                    }
                }
            }
        } else {
            //删除的是不同路径相同文件
            deleteFile(storage);
        }
        fileStorageService.remove(storageQueryWrapper);
        return R.success("删除成功");
    }

    @DeleteMapping(value = "/deleteChunks")
    public R<String> deleteChunks(HttpServletRequest request, String md5) throws IOException {
        UserInfo user = getCurrentUser(request);
        if (user == null) {
            return R.error("用户未登录");
        }
        LambdaQueryWrapper<FileChunk> storageQueryWrapper = new LambdaQueryWrapper<>();
        storageQueryWrapper.eq(FileChunk::getIdentifier, md5);
        boolean delete = fileChunkService.remove(storageQueryWrapper);
        if (delete) {
            Path rootPath = Paths.get(baseFileSavePath);
            String fullFileName = rootPath.toAbsolutePath().resolve(md5).toString();
            Path file = Paths.get(fullFileName);
            if (Files.exists(file)) {
                Files.delete(file);
            }
            return R.success("删除成功");
        } else return R.success("删除失败");
    }

    @PutMapping("/rename")
    public R<String> rename(HttpServletRequest request, String renameId, String newName) {
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileStorage::getId, renameId);
        FileStorage storage = fileStorageService.getOne(queryWrapper);
        storage.setUpdateTime(null);
        Path filePath = Paths.get(storage.getFilePath());
        if (Objects.equals(storage.getFileType(), "dir")) {
            storage.setRealName(newName);
            String oldParentPath = storage.getFilePath();
            if (filePath.getParent() != null) {
                storage.setFilePath(filePath.getParent() + "/" + newName);
                fileStorageService.updateById(storage);
            } else {
                storage.setFilePath(newName);
                fileStorageService.updateById(storage);
            }
            String newParentPath = storage.getFilePath();
            LambdaQueryWrapper<FileStorage> sonQueryWrapper = new LambdaQueryWrapper<>();
            sonQueryWrapper.likeRight(FileStorage::getFilePath, oldParentPath);
            List<FileStorage> list = fileStorageService.list(sonQueryWrapper);
            for (FileStorage sonFileStorage : list) {
                sonFileStorage.setFilePath(sonFileStorage.getFilePath().replaceFirst(oldParentPath, newParentPath));
                fileStorageService.updateById(sonFileStorage);
            }
        } else {
            String suffix = FileUtil.getSuffix(storage.getRealName());
            if (Objects.equals(suffix, "")) {
                storage.setRealName(newName);
            } else {
                storage.setRealName(newName + "." + suffix);
            }
            if (filePath.getParent() != null) {
                storage.setFilePath(filePath.getParent().toString().replace("\\", "/") + "/" + storage.getRealName());
                fileStorageService.updateById(storage);
            } else {
                storage.setFilePath(storage.getRealName());
                fileStorageService.updateById(storage);
            }
        }
        return R.success("修改成功");
    }

    /**
     * 下载接口，这里只做了普通的下载
     *
     * @param request  req
     * @param response res
     * @param id       md5
     * @throws IOException 异常
     */
    @GetMapping("/download")
    public void downloadByIdentifier(HttpServletRequest request, HttpServletResponse response, String id,
                                     @RequestHeader(value = "Range", required = false) String range) throws IOException {
        fileStorageService.downloadByIdentifier(id, request, response, range);
    }

    @Data
    private static class ZipEntity {
        public ZipEntity(String path, List<FileStorage> data) {
            this.path = path;
            this.data = data;
        }

        private String path;
        private List<FileStorage> data;
    }

    @GetMapping("/downloadZip")
    public void downloadZip(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "id") List<String> ids) throws IOException {
        if (ids == null || ids.isEmpty()) {
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String fileName = "打包下载_" + dateFormat.format(new Date()) + ".zip";
        response.setStatus(200);
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
        response.setHeader("Accept-Ranges", "bytes");

        List<FileStorage> storages = fileStorageService.listByIds(ids);
        List<ZipEntity> sonEntries = new ArrayList<>();

        try (ZipOutputStream output = new ZipOutputStream(response.getOutputStream())) {
            for (FileStorage item : storages) {
                if (Objects.equals(item.getFileType(), "dir")) {
                    // 如果是目录，查询该目录下的所有文件
                    LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
                    queryWrapper.likeRight(FileStorage::getFilePath, item.getFilePath());
                    List<FileStorage> sonStorage = fileStorageService.list(queryWrapper);
                    if (sonStorage.isEmpty()) {
                        addDirToZip(item.getRealName(), output);
                    } else {
                        sonEntries.add(new ZipEntity(item.getFilePath(), sonStorage));
                    }
                } else {
                    // 如果是文件，添加到 ZIP 压缩包
                    addFileToZip(item, item.getRealName(), output);
                }
            }

            // 将所有子文件添加到 ZIP 压缩包
            for (ZipEntity sonEntry : sonEntries) {
                String parentPath = sonEntry.getPath();
                Path path = Paths.get(parentPath);
                String parentName = path.getFileName().toString();
                for (FileStorage sonStorage : sonEntry.getData()) {
                    String relativePath = sonStorage.getFilePath().replaceFirst(parentPath, parentName);
                    if (!Objects.equals(sonStorage.getFileType(), "dir")) {
                        addFileToZip(sonStorage, relativePath, output);
                    } else {
                        addDirToZip(relativePath, output);
                    }
                }

            }

        } catch (IOException e) {
            // 异常处理（记录日志，重新抛出等）
            e.printStackTrace();
        }
    }

    private UserInfo getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            return null;
        }
        return (UserInfo) session.getAttribute("userInfo");
    }

    private void addFileToZip(FileStorage fileStorage, String relativePath, ZipOutputStream output) throws IOException {
        var zipEntry = new ZipEntry(relativePath);
        output.putNextEntry(zipEntry);

        // 获取磁盘上存储的文件路径
        String filePath = fileStorage.getFileName();

        // 打开文件并将其内容写入 ZIP 输出流
        try (FileInputStream fis = new FileInputStream(new File(filePath))) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) >= 0) {
                output.write(buffer, 0, length);
            }
        }

        output.closeEntry();
    }

    private void addDirToZip(String relativePath, ZipOutputStream output) throws IOException {
        var zipEntry = new ZipEntry(relativePath + "/");
        output.putNextEntry(zipEntry);
        output.closeEntry();
    }

    private void deleteFile(FileStorage item) {
        try {
            String identifier = item.getIdentifier();
            LambdaQueryWrapper<FileStorage> uniqueQueryWrapper = new LambdaQueryWrapper<>();
            uniqueQueryWrapper.eq(FileStorage::getIdentifier, identifier);
            List<FileStorage> list = fileStorageService.list(uniqueQueryWrapper);
            if (list.size() == 1) {
                String fileName = item.getFileName();
                Path file = Paths.get(fileName);
                if (Files.exists(file)) {
                    Files.delete(file);
                }
                LambdaQueryWrapper<FileChunk> chunkQueryWrapper = new LambdaQueryWrapper<>();
                chunkQueryWrapper.eq(FileChunk::getIdentifier, identifier);
                fileChunkService.remove(chunkQueryWrapper);
            }
            fileStorageService.removeById(item.getId());
        } catch (Exception e) {
            throw new RuntimeException("删除失败");
        }
    }
}

