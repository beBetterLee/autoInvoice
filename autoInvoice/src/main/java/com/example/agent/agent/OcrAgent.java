package com.example.agent.agent;

import com.example.agent.model.InvoiceData;
import com.example.agent.model.ProcessContext;
import com.example.agent.model.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OCR识别Agent
 * 负责从发票图片/PDF中提取结构化数据
 * 当前为Mock实现，支持扩展接入腾讯云OCR/百度OCR/阿里云OCR
 */
@Slf4j
@Component
public class OcrAgent implements InvoiceAgent {

    @Value("${agent.ocr.provider:mock}")
    private String provider;

    @Override
    public String getName() {
        return "OCR识别Agent";
    }

    @Override
    public int getOrder() {
        return 10;
    }

    @Override
    public ProcessContext process(ProcessContext context) {
        log.info("[{}] 开始处理, provider={}, fileUrl={}", getName(), provider, context.getFileUrl());
        context.setStatus(ProcessStatus.OCR_PROCESSING);

        try {
            InvoiceData data = switch (provider) {
                case "tencent" -> processByTencent(context.getFileUrl());
                case "baidu" -> processByBaidu(context.getFileUrl());
                default -> processByMock(context.getFileUrl());
            };

            context.setInvoiceData(data);
            context.addStep(getName(), "OCR识别", true, "识别成功，发票号: " + data.getInvoiceNo());
            log.info("[{}] 处理完成, invoiceNo={}", getName(), data.getInvoiceNo());
        } catch (Exception e) {
            log.error("[{}] 处理失败: {}", getName(), e.getMessage(), e);
            context.setStatus(ProcessStatus.FAILED);
            context.setErrorMessage("OCR识别失败: " + e.getMessage());
            context.addStep(getName(), "OCR识别", false, e.getMessage());
        }

        return context;
    }

    /**
     * Mock OCR实现 - 模拟识别结果
     */
    private InvoiceData processByMock(String fileUrl) {
        return InvoiceData.builder()
                .fileUrl(fileUrl)
                .invoiceNo("INV-" + System.currentTimeMillis())
                .invoiceCode("044001900111")
                .amount(new BigDecimal("8500.00"))
                .taxAmount(new BigDecimal("1105.00"))
                .totalAmount(new BigDecimal("9605.00"))
                .buyerName("北京创新科技有限公司")
                .buyerTaxNo("91110108MA01X")
                .sellerName("上海数字服务有限公司")
                .sellerTaxNo("91310115MA1K4")
                .invoiceDate(LocalDate.now())
                .invoiceType("增值税专用发票")
                .build();
    }

    /**
     * 腾讯云OCR - 预留接口
     */
    private InvoiceData processByTencent(String fileUrl) {
        // TODO: 接入腾讯云OCR API
        log.warn("腾讯云OCR尚未实现，使用Mock数据");
        return processByMock(fileUrl);
    }

    /**
     * 百度OCR - 预留接口
     */
    private InvoiceData processByBaidu(String fileUrl) {
        // TODO: 接入百度OCR API
        log.warn("百度OCR尚未实现，使用Mock数据");
        return processByMock(fileUrl);
    }
}
