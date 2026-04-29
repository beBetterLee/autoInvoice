package com.example.agent.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 处理上下文 - 贯穿整个处理链路，记录状态与追踪信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessContext {

    /** 处理唯一ID（链路追踪） */
    private String traceId;

    /** 文件URL */
    private String fileUrl;

    /** 发票数据 */
    private InvoiceData invoiceData;

    /** 当前处理状态 */
    private ProcessStatus status;

    /** 处理开始时间 */
    private LocalDateTime startTime;

    /** 处理结束时间 */
    private LocalDateTime endTime;

    /** 处理耗时(ms) */
    private Long duration;

    /** 校验结果列表 */
    @Builder.Default
    private List<ValidationResult> validationResults = new ArrayList<>();

    /** 处理步骤日志 */
    @Builder.Default
    private List<ProcessStep> steps = new ArrayList<>();

    /** 错误信息 */
    private String errorMessage;

    /**
     * 添加处理步骤
     */
    public void addStep(String agentName, String action, boolean success, String message) {
        steps.add(ProcessStep.builder()
                .agentName(agentName)
                .action(action)
                .success(success)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build());
    }

    /**
     * 添加校验结果
     */
    public void addValidation(String field, boolean passed, String message) {
        validationResults.add(ValidationResult.builder()
                .field(field)
                .passed(passed)
                .message(message)
                .build());
    }

    /**
     * 是否所有校验通过
     */
    public boolean isAllValidationPassed() {
        return validationResults.stream().allMatch(ValidationResult::isPassed);
    }
}
