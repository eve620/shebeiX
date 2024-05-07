package com.fin.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("files")
public class FileInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;

    private Integer size;

    private String contentType;

    private String owner;

    private String etag;

    private String uploadId;

    public String parent;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime created;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime lastModified;
    ;
}
