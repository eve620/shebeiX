package com.fin.system.util.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ItemReadModel implements Serializable {
    @ExcelProperty("类别")
    private String itemType;
    @ExcelProperty("编号")
    private String itemNumber;
    @ExcelProperty("名称")
    private String itemName;
    @ExcelProperty("型号")
    private String itemModel;
    @ExcelProperty("采购人")
    private String itemPurchaser;
    @ExcelProperty("税额")
    private String itemTax;
    @ExcelProperty("价值")
    private String itemPrice;
    @ExcelProperty("净值")
    private String itemNetworth;
    @ExcelProperty("领用单位")
    private String itemUnit;
    @ExcelProperty("领用人")
    private String userName;
    @ExcelProperty("存放地")
    private String labName;
    @ExcelProperty("出厂号")
    private String itemSerial;
    @ExcelProperty("现状")
    private String itemStatus;
    @ExcelProperty("入库日期")
    private String itemWarehousing;
    @ExcelProperty("单位管理员备注")
    private String itemUnitnote;
    @ExcelProperty("财务凭单号")
    private String itemTracking;
    @ExcelProperty("备注")
    private String itemNote;
}
