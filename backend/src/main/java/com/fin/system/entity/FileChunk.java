package com.fin.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文件块存储(FileChunk)表实体类
 *
 * @author 散装java
 * @since 2022-11-19 14:59:08
 */
@Data
public class FileChunk implements Serializable {
    /**主键**/
    @TableId(type = IdType.AUTO)
    private Long id;
    /**文件名**/
    private String fileName;
    /**当前分片，从1开始**/
    private Integer chunkNumber;
    /**分片大小**/
    private Long chunkSize;
    /**当前分片大小**/
    private Long currentChunkSize;
    /**文件总大小**/
    private Long totalSize;
    /**总分片数**/
    private Integer totalChunks;
    /**文件标识 md5校验码**/
    private String identifier;
    /**相对路径**/
    private String relativePath;
    /**创建者**/
    private String createBy;
    /**创建者ID**/
    private Integer createById;
    /**创建时间**/
    private LocalDateTime createTime;
    /**更新人**/
    private String updateBy;
    /**更新时间**/
    private LocalDateTime updateTime;
}

