package com.fin.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fin.system.Mapper.FileChunkMapper;
import com.fin.system.dto.FileChunkDto;
import com.fin.system.entity.FileChunk;
import com.fin.system.entity.FileStorage;
import com.fin.system.service.FileChunkService;
import com.fin.system.service.FileStorageService;
import com.fin.system.vo.CheckResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文件块存储(FileChunk)表服务实现类
 *
 * @author 散装java
 * @since 2022-11-19 13:23:29
 */
@Service()
public class FileChunkServiceImpl extends ServiceImpl<FileChunkMapper, FileChunk> implements FileChunkService {
    @Override
    public CheckResultVo check(FileChunkDto dto) {
        CheckResultVo vo = new CheckResultVo();
        // 1. 根据 identifier 查找数据是否存在
        List<FileChunk> list = this.list(new LambdaQueryWrapper<FileChunk>()
                .eq(FileChunk::getIdentifier, dto.getIdentifier())
                .orderByAsc(FileChunk::getChunkNumber)
        );
        // 如果是 0 说明文件不存在，则直接返回没有上传
        if (list.isEmpty()) {
            vo.setUploaded(false);
            return vo;
        }
        // 如果不是0，则拿到第一个数据，查看文件是否分片
        // 如果没有分片，那么直接返回已经上穿成功
        FileChunk fileChunk = list.get(0);
        if (fileChunk.getTotalChunks() == 1) {
            vo.setUploaded(true);
            return vo;
        }
        // 处理分片
        ArrayList<Integer> uploadedFiles = new ArrayList<>();
        for (FileChunk chunk : list) {
            uploadedFiles.add(chunk.getChunkNumber());
        }
        if (uploadedFiles.size() == fileChunk.getTotalChunks()) {
            vo.setUploaded(true);
        }
        vo.setUploadedChunks(uploadedFiles);
        return vo;
    }
}

