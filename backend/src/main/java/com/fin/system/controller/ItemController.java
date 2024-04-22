package com.fin.system.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fin.system.commen.R;
import com.fin.system.entity.Item;
import com.fin.system.entity.UserInfo;
import com.fin.system.service.ItemService;
import com.fin.system.service.LabService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/item")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Autowired
    private LabService labService;

    @GetMapping("/page")
    public R<List<Item>> page(HttpServletRequest request, String year, String labName) {
        System.out.println("1111111111111");
        System.out.println(year);
        UserInfo user = (UserInfo) request.getSession().getAttribute("userInfo");
        String userName = user.userName;
        int roleId = user.roleId;
        //构造条件构造器
        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper();
        //添加过滤条件
        queryWrapper.eq(StringUtils.isNotEmpty(year), Item::getCheckYear, year);
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
    @Transactional(rollbackFor = Exception.class)
    public R<String> upload(HttpServletResponse response, @RequestParam("file") MultipartFile file, @RequestParam("year") String year) throws IOException {
        try {
            InputStream inputStream = file.getInputStream();
            Workbook workbook;
            if (file.getOriginalFilename().endsWith(".xlsx")) {
                workbook = new XSSFWorkbook(inputStream);
            } else if (file.getOriginalFilename().endsWith(".xls")) {
                workbook = new HSSFWorkbook(inputStream);
            } else {
                return R.error("文件格式错误");
            }
            Sheet sheet = workbook.getSheetAt(0);
            int rowNum = 0; // 行号
            for (Row row : sheet) {
                //     跳过前两行，即标题行和字段行
                if (rowNum < 2) {
                    if (rowNum == 1) {
                        List<String> expectedHeaders = Arrays.asList(
                                "序号", "类别", "编号", "名称", "型号",
                                "采购人", "税额", "价值", "净值", "领用单位",
                                "领用人", "存放地", "出厂号", "现状", "入库日期",
                                "单位管理员备注", "财务凭单号", "备注"
                        );
                        for (int i = 0; i < row.getLastCellNum(); i++) {
                            Cell cell = row.getCell(i);
                            if (cell == null || cell.getCellType() != CellType.STRING || !cell.getStringCellValue().trim().equals(expectedHeaders.get(i))) {
                                return R.error("Excel格式错误");
                            }
                        }
                    }
                    rowNum++;
                    continue;
                }
                if (rowNum > 10) break;

                String itemType = row.getCell(1).getStringCellValue();
                String itemNumber = row.getCell(2).getStringCellValue();
                String itemName = row.getCell(3).getStringCellValue();
                String itemModel = row.getCell(4).getStringCellValue();
                String itemPurchaser = row.getCell(5).getStringCellValue();
                String itemTax = row.getCell(6).getStringCellValue();
                String itemPrise = row.getCell(7).getStringCellValue();
                String itemNetworth = row.getCell(8).getStringCellValue();
                String itemUnit = row.getCell(9).getStringCellValue();
                String userName = row.getCell(10).getStringCellValue();
                String labName = row.getCell(11).getStringCellValue();
                String itemSerial = row.getCell(12).getStringCellValue();
                String itemState = row.getCell(13).getStringCellValue();
                String itemWarehousing = row.getCell(14).getStringCellValue();
                String itemItemUnitnote = row.getCell(15).getStringCellValue();
                String itemTracking = row.getCell(16).getStringCellValue();
                String itemNote = row.getCell(17).getStringCellValue();
                Item item = new Item();
                item.setItemType(itemType);
                item.setItemNumber(itemNumber);
                item.setItemName(itemName);
                item.setItemModel(itemModel);
                item.setItemPurchaser(itemPurchaser);
                item.setItemTax(itemTax);
                item.setItemPrice(itemPrise);
                item.setItemNetworth(itemNetworth);
                item.setItemUnit(itemUnit);
                item.setUserName(userName);
                item.setLabName(labName);
                item.setItemSerial(itemSerial);
                item.setItemStatus(itemState);
                item.setItemWarehousing(itemWarehousing);
                item.setItemUnitnote(itemItemUnitnote);
                item.setItemTracking(itemTracking);
                item.setItemNote(itemNote);
                item.setCheckYear(year);
                rowNum++;
                System.out.println(item);
                // 存入数据库
                itemService.save(item);
            }
        } catch (DataAccessException e) {
            // 检查具体的异常原因，看是否是唯一索引重复错误
            Throwable rootCause = ExceptionUtils.getRootCause(e);
            if (rootCause instanceof SQLIntegrityConstraintViolationException) {
                // 如果是唯一索引重复错误，抛出自定义异常
                return R.error("重复数据");
            } else {
                // 其他数据库异常处理
                return R.error("数据上传异常");
            }
        } catch (Exception e) {
            return R.error("Excel文件上传失败");
        }
        return R.success("Excel文件上传成功");
    }
}
