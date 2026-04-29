package com.example.agent.agent;

import com.example.agent.model.InvoiceData;
import com.example.agent.model.ProcessContext;
import com.example.agent.model.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 分类Agent
 * 负责根据发票内容进行智能分类归档
 * 当前基于规则匹配，支持扩展为LLM驱动的语义分类
 */
@Slf4j
@Component
public class ClassificationAgent implements InvoiceAgent {

    @Value("#{${agent.classification.rules:{}}}")
    private Map<String, String> classificationRules;

    @Override
    public String getName() {
        return "分类Agent";
    }

    @Override
    public int getOrder() {
        return 30;
    }

    @Override
    public ProcessContext process(ProcessContext context) {
        log.info("[{}] 开始分类, invoiceNo={}", getName(), context.getInvoiceData().getInvoiceNo());
        context.setStatus(ProcessStatus.CLASSIFYING);

        try {
            InvoiceData data = context.getInvoiceData();
            String category = classify(data);
            data.setCategory(category);

            context.addStep(getName(), "智能分类", true, "分类结果: " + category);
            log.info("[{}] 分类完成: {}", getName(), category);
        } catch (Exception e) {
            log.error("[{}] 分类失败: {}", getName(), e.getMessage(), e);
            context.getInvoiceData().setCategory("未分类");
            context.addStep(getName(), "智能分类", false, "分类异常: " + e.getMessage());
        }

        return context;
    }

    /**
     * 基于规则的分类逻辑
     * 优先匹配销方名称，其次匹配备注信息
     */
    private String classify(InvoiceData data) {
        String matchText = buildMatchText(data);

        // 遍历分类规则进行匹配
        for (Map.Entry<String, String> rule : classificationRules.entrySet()) {
            String keyword = rule.getKey();
            if ("default".equals(keyword)) continue;
            if (matchText.contains(keyword)) {
                return rule.getValue();
            }
        }

        // 返回默认分类
        return classificationRules.getOrDefault("default", "其他");
    }

    /**
     * 构建用于匹配的文本（合并多个字段）
     */
    private String buildMatchText(InvoiceData data) {
        StringBuilder sb = new StringBuilder();
        if (data.getSellerName() != null) sb.append(data.getSellerName());
        if (data.getBuyerName() != null) sb.append(data.getBuyerName());
        if (data.getRemark() != null) sb.append(data.getRemark());
        if (data.getInvoiceType() != null) sb.append(data.getInvoiceType());
        return sb.toString();
    }
}
