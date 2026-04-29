package com.example.agent.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 发票核心数据模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceData {

    /** 文件URL */
    private String fileUrl;

    /** 发票号码 */
    private String invoiceNo;

    /** 发票代码 */
    private String invoiceCode;

    /** 金额（不含税） */
    private BigDecimal amount;

    /** 税额 */
    private BigDecimal taxAmount;

    /** 价税合计 */
    private BigDecimal totalAmount;

    /** 购方名称 */
    private String buyerName;

    /** 购方税号 */
    private String buyerTaxNo;

    /** 销方名称 */
    private String sellerName;

    /** 销方税号 */
    private String sellerTaxNo;

    /** 开票日期 */
    private LocalDate invoiceDate;

    /** 发票类型: 增值税专用发票、增值税普通发票、电子发票等 */
    private String invoiceType;

    /** 分类结果 */
    private String category;

    /** 备注 */
    private String remark;
}
