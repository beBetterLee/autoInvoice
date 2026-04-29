package com.example.agent.agent;

import com.example.agent.model.InvoiceData;
import com.example.agent.model.ProcessContext;
import com.example.agent.model.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 校验Agent
 * 负责对OCR识别结果进行业务规则校验
 * 包括：金额校验、税号格式校验、必填字段校验、逻辑一致性校验
 */
@Slf4j
@Component
public class ValidationAgent implements InvoiceAgent {

    @Value("${agent.validation.max-amount:9999999.99}")
    private BigDecimal maxAmount;

    @Value("${agent.validation.tax-no-length:15}")
    private int taxNoLength;

    @Override
    public String getName() {
        return "校验Agent";
    }

    @Override
    public int getOrder() {
        return 20;
    }

    @Override
    public ProcessContext process(ProcessContext context) {
        log.info("[{}] 开始校验, invoiceNo={}", getName(), context.getInvoiceData().getInvoiceNo());
        context.setStatus(ProcessStatus.VALIDATING);

        InvoiceData data = context.getInvoiceData();

        // 1. 必填字段校验
        validateRequired(context, data);

        // 2. 金额校验
        validateAmount(context, data);

        // 3. 税号格式校验
        validateTaxNo(context, data);

        // 4. 金额逻辑一致性校验（不含税 + 税额 = 价税合计）
        validateAmountConsistency(context, data);

        // 判断校验结果
        if (context.isAllValidationPassed()) {
            context.addStep(getName(), "业务校验", true, "全部校验通过");
            log.info("[{}] 校验通过", getName());
        } else {
            context.setStatus(ProcessStatus.VALIDATION_FAILED);
            long failCount = context.getValidationResults().stream()
                    .filter(r -> !r.isPassed()).count();
            String msg = "校验未通过，共 " + failCount + " 项不合规";
            context.addStep(getName(), "业务校验", false, msg);
            context.setErrorMessage(msg);
            log.warn("[{}] {}", getName(), msg);
        }

        return context;
    }

    private void validateRequired(ProcessContext context, InvoiceData data) {
        if (isBlank(data.getInvoiceNo())) {
            context.addValidation("invoiceNo", false, "发票号码不能为空");
        } else {
            context.addValidation("invoiceNo", true, "发票号码校验通过");
        }

        if (isBlank(data.getSellerName())) {
            context.addValidation("sellerName", false, "销方名称不能为空");
        } else {
            context.addValidation("sellerName", true, "销方名称校验通过");
        }

        if (isBlank(data.getBuyerName())) {
            context.addValidation("buyerName", false, "购方名称不能为空");
        } else {
            context.addValidation("buyerName", true, "购方名称校验通过");
        }
    }

    private void validateAmount(ProcessContext context, InvoiceData data) {
        if (data.getAmount() == null) {
            context.addValidation("amount", false, "金额不能为空");
            return;
        }

        if (data.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            context.addValidation("amount", false, "金额必须大于0");
        } else if (data.getAmount().compareTo(maxAmount) > 0) {
            context.addValidation("amount", false, "金额超出上限: " + maxAmount);
        } else {
            context.addValidation("amount", true, "金额校验通过");
        }
    }

    private void validateTaxNo(ProcessContext context, InvoiceData data) {
        if (isBlank(data.getSellerTaxNo())) {
            context.addValidation("sellerTaxNo", false, "销方税号不能为空");
        } else if (data.getSellerTaxNo().length() < taxNoLength) {
            context.addValidation("sellerTaxNo", false,
                    "销方税号长度不足（要求至少" + taxNoLength + "位）");
        } else {
            context.addValidation("sellerTaxNo", true, "销方税号校验通过");
        }

        if (isBlank(data.getBuyerTaxNo())) {
            context.addValidation("buyerTaxNo", false, "购方税号不能为空");
        } else if (data.getBuyerTaxNo().length() < taxNoLength) {
            context.addValidation("buyerTaxNo", false,
                    "购方税号长度不足（要求至少" + taxNoLength + "位）");
        } else {
            context.addValidation("buyerTaxNo", true, "购方税号校验通过");
        }
    }

    private void validateAmountConsistency(ProcessContext context, InvoiceData data) {
        if (data.getAmount() != null && data.getTaxAmount() != null && data.getTotalAmount() != null) {
            BigDecimal expected = data.getAmount().add(data.getTaxAmount());
            if (expected.compareTo(data.getTotalAmount()) != 0) {
                context.addValidation("totalAmount", false,
                        "价税合计不一致: 不含税(" + data.getAmount() + ") + 税额(" +
                                data.getTaxAmount() + ") ≠ 合计(" + data.getTotalAmount() + ")");
            } else {
                context.addValidation("totalAmount", true, "价税合计一致性校验通过");
            }
        }
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }
}
