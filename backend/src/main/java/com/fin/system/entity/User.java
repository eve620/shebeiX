package com.fin.system.entity;

import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class User implements Serializable {
    @TableId(type = IdType.AUTO)
    @ExcelProperty("序号")
    private Integer userId;
    @ExcelProperty("账号")
    private String userAccount;
    @ExcelProperty("密码")
    private String userPassword;
    @ExcelProperty("姓名")
    private String userName;
    @ExcelProperty("权限id")
    private Integer roleId;
    @ExcelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ExcelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
