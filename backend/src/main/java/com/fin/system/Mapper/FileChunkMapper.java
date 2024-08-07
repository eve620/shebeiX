package com.fin.system.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fin.system.entity.FileChunk;
import org.apache.ibatis.annotations.Mapper;


/**
 * 文件块存储(FileChunk)表数据库访问层
 *
 * @author 散装java
 * @since 2022-11-19 13:23:29
 */
@Mapper
public interface FileChunkMapper extends BaseMapper<FileChunk> {

}

