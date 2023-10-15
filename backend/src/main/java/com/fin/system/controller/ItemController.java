package com.fin.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.Item;
import com.fin.system.entity.User;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.ItemService;
import com.fin.system.service.LabService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private LabService labService;

    @GetMapping("/page")
    public R<List<Item>> page(HttpServletRequest request, String input) {
        UserInfo user = (UserInfo) request.getSession().getAttribute("userInfo");
        String userName = user.userName;
        int roleId = user.roleId;
        //构造条件构造器
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(input), Item::getItemName, input);
        //添加排序条件
        queryWrapper.orderByDesc(Item::getUpdateTime);
        if (roleId == 0) {
            //添加查询条件
            queryWrapper.eq(Item::getUserName, userName);
        }
        //执行查询
        List<Item> items = itemService.list(queryWrapper);
        return R.success(items);
    }

    @GetMapping("/{id}")
    public R<Item> getById(@PathVariable Long id) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getItemId, id);
        Item item = itemService.getOne(queryWrapper);
        if (item != null) {
            return R.success(item);
        }
        return R.error("没有查询到对应资产信息");
    }

    //新增资产
    @PostMapping
    public R<String> save(HttpServletRequest request, @RequestBody Item item) {
        itemService.save(item);
        return R.success("新增资产成功");
    }

    //删除资产
    @DeleteMapping("/{id}")
    public R<String> deleteById(@PathVariable Long id) {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Item::getItemId, id);
        Boolean res = itemService.remove(queryWrapper);
        if (res) {
            return R.success("设备删除成功");
        }
        return R.error("设备删除失败");
    }

    //编辑信息
    @PutMapping
    public R<String> update(HttpServletRequest request, @RequestBody Item item) {
        itemService.updateById(item);
        return R.success("信息修改成功");
    }

    //导出Excel
    @GetMapping("download")
    public void download(HttpServletResponse response, String name) throws IOException {
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Item::getItemName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Item::getUpdateTime);

        List<Item> data = itemService.list(queryWrapper);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easy excel没有关系
        String fileName = URLEncoder.encode("测试", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Item.class).sheet("在库资产").doWrite(data);
    }

    @GetMapping("userdownload")
    public void userdownload(HttpServletRequest request, HttpServletResponse response, String name) throws IOException {
        User user = (User) request.getSession().getAttribute("userInfo");
        String userName = user.getUserName();

        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(name), Item::getItemName, name);
        //添加查询条件
        queryWrapper.eq(Item::getUserName, userName);
        //添加排序条件
        queryWrapper.orderByDesc(Item::getUpdateTime);

        List<Item> data = itemService.list(queryWrapper);
        // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
        String fileName = URLEncoder.encode("测试", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
        EasyExcel.write(response.getOutputStream(), Item.class).sheet("在库资产").doWrite(data);
    }
}
