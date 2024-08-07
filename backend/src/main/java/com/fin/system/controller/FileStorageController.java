package com.fin.system.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.dto.FileChunkDto;
import com.fin.system.entity.FileStorage;
import com.fin.system.service.FileChunkService;
import com.fin.system.service.FileStorageService;
import com.fin.system.vo.CheckResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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


    public static boolean matchStringWithoutSlash(String input, String patternStr) {
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

    /**
     * 本接口为校验接口，即上传前，先根据本接口查询一下 服务器是否存在该文件
     *
     * @param dto 入参
     * @return vo
     */
    @GetMapping("/upload")
    public R<CheckResultVo> checkUpload(FileChunkDto dto) {
        String filePath = dto.getRelativePath();
        if (!Objects.equals(dto.getDirPath(), "")) {
            filePath = dto.getDirPath() + "/" + dto.getRelativePath();
        }
        LambdaQueryWrapper<FileStorage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileStorage::getIdentifier, dto.getIdentifier());
        FileStorage storage = fileStorageService.getOne(queryWrapper);
        if (storage != null) {
            if (!Objects.equals(storage.getFilePath(), filePath)) {
                storage.setFilePath(filePath);
                storage.setId(null);
                fileStorageService.save(storage);
            }
        }
        return R.success(fileChunkService.check(dto));
    }

    /**
     * 本接口为实际上传接口
     *
     * @param dto      入参
     * @param response response 配合前端返回响应的状态码
     * @return boolean
     */
    @PostMapping("/upload")
    public R<Boolean> upload(FileChunkDto dto, HttpServletResponse response) {
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

    /**
     * 下载接口，这里只做了普通的下载
     *
     * @param request    req
     * @param response   res
     * @param identifier md5
     * @throws IOException 异常
     */
    @GetMapping(value = "/download/{identifier}")
    public void downloadByIdentifier(HttpServletRequest request, HttpServletResponse response, @PathVariable("identifier") String identifier) throws IOException {
        fileStorageService.downloadByIdentifier(identifier, request, response);
    }
}

