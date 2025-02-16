package com.fin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fin.system.Mapper.ItemMapper;
import com.fin.system.entity.Item;
import com.fin.system.util.excel.ItemReadModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ItemService extends IService<Item> {

    @Transactional
    void saveBatch(List<ItemReadModel> items);
}
