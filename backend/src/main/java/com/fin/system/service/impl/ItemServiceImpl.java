package com.fin.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fin.system.Mapper.ItemMapper;
import com.fin.system.entity.Item;
import com.fin.system.service.ItemService;
import com.fin.system.util.excel.ItemReadModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements ItemService {
    @Autowired
    private ItemService itemService;

    @Override
    @Transactional
    public void save(List<Item> items) {
        if (!items.isEmpty()) {
            itemService.saveBatch(items);
        }
    }
}
