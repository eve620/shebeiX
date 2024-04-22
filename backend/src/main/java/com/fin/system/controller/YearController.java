package com.fin.system.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.Year;
import com.fin.system.service.YearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.function.Function;

@RestController
@RequestMapping("/year")
public class YearController {
    @Autowired
    private YearService yearService;

    //查询审查列表
    @GetMapping("/list")
    public R<List<String>> list() {
        //构造条件构造器
        LambdaQueryWrapper<Year> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.select(Year::getYear);
        Function<Object, String> f = (o -> o.toString());
        List<String> checkList = yearService.listObjs(queryWrapper, f);
        return R.success(checkList);
    }


    //新增审查
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Year year) {
        yearService.save(year);
        return R.success("新增审查成功");
    }

    //删除审查
    @DeleteMapping("/{year}")
    public R<String> deleteById(@PathVariable String year) {
        LambdaQueryWrapper<Year> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Year::getYear, year);
        Boolean res = yearService.remove(queryWrapper);
        if (res) {
            return R.success("审查删除成功");
        }
        return R.error("审查删除失败");
    }

    //编辑审查
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Year year) {
        yearService.updateById(year);
        return R.success("信息修改成功");
    }

}
