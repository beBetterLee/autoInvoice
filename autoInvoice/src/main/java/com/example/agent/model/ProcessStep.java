package com.example.agent.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

/**
 * 处理步骤记录
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessStep {

    /** Agent名称 */
    private String agentName;

    /** 执行动作 */
    private String action;

    /** 是否成功 */
    private boolean success;

    /** 描述信息 */
    private String message;

    /** 执行时间 */
    private LocalDateTime timestamp;
}
