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


    //查询审查列表
    @GetMapping("/list")
    public R<List<FileInfo>> list(String parent) {
        System.out.println(parent);
        //构造条件构造器
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(FileInfo::getParent, parent);
        List<FileInfo> fileInfoList = fileService.list(queryWrapper);
        return R.success(fileInfoList);
    }


    //新增审查
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody FileInfo year) {
        fileService.save(year);
        return R.success("新增审查成功");
    }

    //删除审查
    @DeleteMapping("/{year}")
    @Transactional(rollbackFor = Exception.class)
    public R<String> deleteById(@PathVariable String year) {
        LambdaQueryWrapper<FileInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileInfo::getName, year);
        Boolean res = fileService.remove(queryWrapper);
        if (res) {
            return R.success("审查删除成功");
        }
        return R.error("审查删除失败");
    }

    //编辑审查
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody FileInfo year) {
        fileService.updateById(year);
        return R.success("信息修改成功");
    }

}
