package com.fin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.FileInfo;
import com.fin.system.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/file")
public class FileController {
    @Autowired
    private FileService fileService;
    @GetMapping("/list")
    public R<List<FileInfo>> list(String parent) {
        //构造条件构造器
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(FileInfo::getParent, parent);
        List<FileInfo> fileInfoList = fileService.list(queryWrapper);
        return R.success(fileInfoList);
    }

    @PostMapping
    public R<String> createDir(String parent) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setContentType("dir");
        fileInfo.setOwner("");
        fileInfo.setName("");
        fileInfo.setParent(parent);
        fileService.save(fileInfo);
        return R.success("新增成功");
    }

}
