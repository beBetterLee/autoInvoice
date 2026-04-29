package com.example.agent.model;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 校验结果
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResult {

    /** 校验字段 */
    private String field;

    /** 是否通过 */
    private boolean passed;

    /** 校验信息 */
    private String message;
}
