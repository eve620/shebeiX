package com.fin.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.Lab;
import com.fin.system.entity.User;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.LabService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

@RestController
@RequestMapping("/lab")
public class LabController {
    @Autowired
    private LabService labService;

    @GetMapping("/page")
    public R<List<Lab>> page(HttpServletRequest request, String input) {
        UserInfo user = (UserInfo) request.getSession().getAttribute("userInfo");
        String userAccount = user.userAccount;
        int roleId = user.roleId;
        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(input), Lab::getLabName, input);
        //添加排序条件
        queryWrapper.orderByDesc(Lab::getUpdateTime);
        if (roleId == 0) {
            //添加查询条件
            queryWrapper.eq(Lab::getUserAccount, userAccount);
        }
        //执行查询
        List<Lab> labs = labService.list(queryWrapper);
        return R.success(labs);
    }

    //查询实验室列表
    @GetMapping("/list")
    public R<List<Lab>> list() {
        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper<>();
        List<Lab> list = labService.list(queryWrapper);
        return R.success(list);
    }

    @GetMapping("/userlist")
    public R<List<Lab>> userlist(HttpServletRequest request) {
        User user = (User) request.getSession().getAttribute("userInfo");
        String userAccount = user.getUserAccount();
        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper<>();
        //添加查询条件
        queryWrapper.eq(Lab::getUserAccount, userAccount);
        List<Lab> list = labService.list(queryWrapper);
        return R.success(list);
    }


    //根据id查询管理员信息
    @GetMapping("/{id}")
    public R<Lab> getById(@PathVariable Long id) {
        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Lab::getLabId, id);
        Lab lab = labService.getOne(queryWrapper);
        if (lab != null) {
            return R.success(lab);
        }
        return R.error("没有查询到对应实验室信息");
    }

    //新增实验室
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Lab lab) {
        labService.save(lab);
        return R.success("新增实验室成功");
    }

    //删除实验室
    @DeleteMapping("/{id}")
    public R<String> deleteById(@PathVariable Long id) {
        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Lab::getLabId, id);
        Boolean res = labService.remove(queryWrapper);
        if (res) {
            return R.success("数据删除成功");
        }
        return R.error("数据删除失败");
    }

    //编辑信息
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Lab lab) {
        labService.updateById(lab);
        return R.success("信息修改成功");
    }

    //导出Excel
    @GetMapping("download")
    public void download(HttpServletResponse response, String name) throws IOException {
        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Lab::getLabName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Lab::getUpdateTime);

        List<Lab> data = labService.list(queryWrapper);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Lab.class).sheet("实验室").doWrite(data);
    }

    @GetMapping("userdownload")
    public void userdownload(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {
        User user = (User) request.getSession().getAttribute("userInfo");
        String userAccount = user.getUserAccount();

        LambdaQueryWrapper<Lab> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Lab::getLabName, name);
        //添加查询条件
        queryWrapper.eq(Lab::getUserAccount, userAccount);
        //添加排序条件
        queryWrapper.orderByDesc(Lab::getUpdateTime);

        List<Lab> data = labService.list(queryWrapper);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Lab.class).sheet("实验室").doWrite(data);
    }
}
