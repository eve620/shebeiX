package com.fin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fin.system.entity.Item;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemService extends IService<Item> {

    @Transactional
    void save(List<Item> items);
}
