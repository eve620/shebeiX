package com.fin.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fin.system.dto.FileChunkDto;
import com.fin.system.entity.FileChunk;
import com.fin.system.vo.CheckResultVo;

/**
 * 文件块存储(FileChunk)表服务接口
 *
 * @author 散装java
 * @since 2022-11-19 13:23:29
 */
public interface FileChunkService extends IService<FileChunk> {
    /**
     * 校验文件
     * @param dto 入参
     * @return obj
     */
    CheckResultVo check(FileChunkDto dto);
}

