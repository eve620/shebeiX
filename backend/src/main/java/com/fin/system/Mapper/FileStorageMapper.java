package com.fin.system.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fin.system.entity.FileStorage;
import org.apache.ibatis.annotations.Mapper;


/**
 * 文件存储表(FileStorage)表数据库访问层
 *
 * @author 散装java
 * @since 2022-11-15 17:49:41
 */
@Mapper
public interface FileStorageMapper extends BaseMapper<FileStorage> {

}

