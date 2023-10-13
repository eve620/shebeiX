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
public class Item implements Serializable {
    @TableId(type = IdType.AUTO)
    @ExcelProperty("序号")
    private Integer itemId;
    @ExcelProperty("类别")
    private String itemType;
    @ExcelProperty("编号")
    private String itemNumber;
    @ExcelProperty("名称")
    private String itemName;
    @ExcelProperty("型号")
    private String itemModel;
    @ExcelProperty("价格")
    private String itemPrice;
    @ExcelProperty("净值")
    private String itemNetworth;
    @ExcelProperty("领用人")
    private String userName;
    @ExcelProperty("存放地")
    private String labName;
    @ExcelProperty("现状")
    private String itemStatus;
    @ExcelProperty("备注")
    private String itemNote;
    @ExcelProperty("创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @ExcelProperty("更新时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
