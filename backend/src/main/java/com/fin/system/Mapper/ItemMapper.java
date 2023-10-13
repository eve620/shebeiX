package com.fin.system.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fin.system.entity.Item;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ItemMapper extends BaseMapper<Item> {
}
