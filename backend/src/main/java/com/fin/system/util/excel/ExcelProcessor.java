package com.fin.system.util.excel;

import com.alibaba.excel.EasyExcel;
import com.fin.system.entity.Item;
import com.fin.system.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExcelProcessor {
    @Autowired
    private ItemService itemService;
    public List<Item> parseExcel(MultipartFile file, String year) {
        // 使用EasyExcel读取数据
        List<Item> items = new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream())
                    .head(ItemReadModel.class) // 使用模型映射
                    .sheet(0)
                    .registerReadListener(new ItemExcelListener(itemService))
                    .doRead();
        } catch (IOException e) {
            throw new RuntimeException("解析Excel失败", e);
        }
        return items;
    }
}
