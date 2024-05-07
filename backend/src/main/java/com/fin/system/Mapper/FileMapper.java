package com.fin.system.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fin.system.entity.FileInfo;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface FileMapper extends BaseMapper<FileInfo> {
}
