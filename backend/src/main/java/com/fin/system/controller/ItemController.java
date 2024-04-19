package com.fin.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.Item;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.ItemService;
import com.fin.system.service.LabService;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private LabService labService;

    @GetMapping("/page")
    public R<List<Item>> page(HttpServletRequest request, String input, String labName) {
        UserInfo user = (UserInfo) request.getSession().getAttribute("userInfo");
        String userName = user.userName;
        int roleId = user.roleId;
        //构造条件构造器
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.like(StringUtils.isNotEmpty(input), Item::getItemName, input);
        queryWrapper.like(StringUtils.isNotEmpty(labName), Item::getLabName, labName);
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
    @PostMapping("/download")
    public void download(HttpServletResponse response, @RequestBody List<Item> items) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        try (OutputStream outputStream = response.getOutputStream()) {
            EasyExcel.write(outputStream, Item.class).sheet("items").doWrite(items);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @PostMapping("/upload")
    public void upload(HttpServletResponse response, @RequestParam("file") MultipartFile file) throws IOException {
        InputStream inputStream = file.getInputStream();

        Workbook workbook;
        if (file.getOriginalFilename().endsWith(".xlsx")) {
            workbook = new XSSFWorkbook(inputStream);
        } else if (file.getOriginalFilename().endsWith(".xls")) {
            workbook = new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("Unsupported file format. Only .xls and .xlsx files are accepted.");
        }
        Sheet sheet = workbook.getSheetAt(0);
        int rowNum = 0; // 行号
        for (Row row : sheet) {
            // 跳过前两行，即标题行和字段行
            if (rowNum < 2) {
                if (rowNum == 1) {
                    String a = row.getCell(1).getStringCellValue();
                    String b = row.getCell(2).getStringCellValue();
                    String c = row.getCell(3).getStringCellValue();
                    String d = row.getCell(4).getStringCellValue();
                    String e = row.getCell(5).getStringCellValue();
                    String f = row.getCell(6).getStringCellValue();
                    String g = row.getCell(7).getStringCellValue();
                    String h = row.getCell(8).getStringCellValue();
                    String i = row.getCell(9).getStringCellValue();
                    String j = row.getCell(10).getStringCellValue();
                    String k = row.getCell(11).getStringCellValue();
                    String l = row.getCell(12).getStringCellValue();
                    String m = row.getCell(13).getStringCellValue();
                    String n = row.getCell(14).getStringCellValue();
                    String o = row.getCell(15).getStringCellValue();
                    String p = row.getCell(16).getStringCellValue();
                    String q = row.getCell(17).getStringCellValue();
                    System.out.println(a);
                }
                rowNum++;
                continue;
            }
            if (rowNum > 10) break;

            String a = row.getCell(1).getStringCellValue();
            String b = row.getCell(2).getStringCellValue();
            String c = row.getCell(3).getStringCellValue();
            String d = row.getCell(4).getStringCellValue();
            String e = row.getCell(5).getStringCellValue();
            String f = row.getCell(6).getStringCellValue();
            String g = row.getCell(7).getStringCellValue();
            String h = row.getCell(8).getStringCellValue();
            String i = row.getCell(9).getStringCellValue();
            String j = row.getCell(10).getStringCellValue();
            String k = row.getCell(11).getStringCellValue();
            String l = row.getCell(12).getStringCellValue();
            String m = row.getCell(13).getStringCellValue();
            String n = row.getCell(14).getStringCellValue();
            String o = row.getCell(15).getStringCellValue();
            String p = row.getCell(16).getStringCellValue();
            String q = row.getCell(17).getStringCellValue();
            System.out.println("类别:" + a +
                    ",编号:" + b +
                    ",名称:" + c +
                    ",型号:" + d +
                    ",采购人:" + e +
                    ",税额:" + f +
                    ",价值:" + g +
                    ",净值:" + h +
                    ",领用单位:" + i +
                    ",领用人:" + j +
                    ",存放地:" + k +
                    ",出厂号:" + l +
                    ",现状:" + m +
                    ",入库日期:" + n +
                    ",单位管理员备注:" + o +
                    ",财务凭单号:" + p +
                    ",备注:" + q);
//            // 创建学生对象并存入数据库
//            Student student = new Student(name, age, score);
//            studentMapper.insert(student);

            rowNum++;
        }
        try (OutputStream outputStream = response.getOutputStream()) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
