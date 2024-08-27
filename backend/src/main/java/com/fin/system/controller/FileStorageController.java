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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
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

    @GetMapping("/list")
    public R<List<FileStorage>> list(String parent) {
        //构造条件构造器
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        List<FileStorage> fileInfoList = fileStorageService.list(queryWrapper);
        List<FileStorage> matchedFiles = fileInfoList.stream()
                // 先进行你之前的匹配逻辑
                .filter(file -> matchStringWithoutSlash(file.getFilePath(), parent))
                // 然后进行排序，确保 fileType 为 "dir" 的排在前面
                .sorted(Comparator.comparing((FileStorage f) -> f.getFileType().equals("dir") ? 0 : 1)
                        .thenComparing(FileStorage::getFilePath)) // 可选：在此基础上按 filePath 排序以保持其他顺序一致性
                .collect(Collectors.toList());

        return R.success(matchedFiles);
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            return R.error("用户未登录");
        }
        UserInfo user = (UserInfo) session.getAttribute("userInfo");
        dto.setCreateBy(user.userName);
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
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            return R.error("用户未登录");
        }
        UserInfo user = (UserInfo) session.getAttribute("userInfo");
        LambdaQueryWrapper<FileChunk> chunkQueryWrapper = new LambdaQueryWrapper<>();
        chunkQueryWrapper.eq(FileChunk::getIdentifier, md5);
        List<FileChunk> uploadedChunks = fileChunkService.list(chunkQueryWrapper);
        List<Integer> chunkNums = uploadedChunks.stream()
                .map(FileChunk::getChunkNumber)
                .toList();
        CheckResultVo res = new CheckResultVo();
        res.setUploaded(false);
        res.setUploadedChunks(chunkNums);
        if (chunkNums.isEmpty()) return R.success(res);
        LambdaQueryWrapper<FileStorage> storageQueryWrapper = new LambdaQueryWrapper<>();
        storageQueryWrapper.eq(FileStorage::getIdentifier, md5);
        List<FileStorage> storages = fileStorageService.list(storageQueryWrapper);
        if (!storages.isEmpty()) {
            res.setUploaded(true);
            for (FileStorage storage : storages) {
                if (Objects.equals(storage.getFilePath(), filePath)) {
                    return R.success(res);
                }
            }
            FileStorage storageCopy = BeanUtil.copyProperties(storages.get(0), FileStorage.class);
            storageCopy.setId(null);
            storageCopy.setCreateBy(user.userName);
            storageCopy.setFilePath(filePath);
            storageCopy.setCreateTime(null);
            storageCopy.setUpdateTime(null);
            fileStorageService.save(storageCopy);
        }
        return R.success(res);
    }

    @PostMapping("/createDir")
    public R<String> createDir(HttpServletRequest request, @RequestBody String dirPath) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
            return R.error("用户未登录");
        }
        //检查是否已创建
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileStorage::getFileType, "dir");
        queryWrapper.eq(FileStorage::getFilePath, dirPath);
        FileStorage storage = fileStorageService.getOne(queryWrapper);
        if (storage != null) return R.success("文件夹已创建");
        UserInfo user = (UserInfo) session.getAttribute("userInfo");

        Path filePath = Paths.get(dirPath);
        String dirName = filePath.getFileName().toString();
        FileStorage fileStorage = new FileStorage();
        fileStorage.setRealName(dirName);
        fileStorage.setFileType("dir");
        fileStorage.setFilePath(dirPath);
        fileStorage.setCreateBy(user.userName);
        fileStorageService.save(fileStorage);
        return R.success("文件夹创建成功");
    }

    @DeleteMapping(value = "/delete")
    public R<String> deleteItem(HttpServletRequest request, String deleteId) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userInfo") == null) {
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
        } else //删除的是不同路径相同文件
        {
            deleteFile(storage);
        }
        fileStorageService.remove(storageQueryWrapper);
        return R.success("删除成功");
    }

    @PutMapping("/rename")
    public R<String> rename(String renameId, String newName) {
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileStorage::getId, renameId);
        FileStorage storage = fileStorageService.getOne(queryWrapper);
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
                System.out.println(filePath.getParent());
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
     * @param request    req
     * @param response   res
     * @param identifier md5
     * @throws IOException 异常
     */
    @GetMapping("/download/{identifier}")
    public void downloadByIdentifier(HttpServletRequest request, HttpServletResponse response, @PathVariable("identifier") String identifier) throws IOException {
        fileStorageService.downloadByIdentifier(identifier, request, response);
    }

    @GetMapping("/downloadZip")
    public void downloadZip(HttpServletRequest request, HttpServletResponse response, @RequestParam(name = "id") List<String> ids) throws IOException {
        System.out.println(ids);
//        fileStorageService.downloadByIdentifier(identifier, request, response);
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

