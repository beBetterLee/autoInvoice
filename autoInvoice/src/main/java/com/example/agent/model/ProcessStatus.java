package com.example.agent.model;

/**
 * 处理状态枚举
 */
public enum ProcessStatus {

    /** 待处理 */
    PENDING("待处理"),

    /** OCR识别中 */
    OCR_PROCESSING("OCR识别中"),

    /** 校验中 */
    VALIDATING("校验中"),

    /** 分类中 */
    CLASSIFYING("分类中"),

    /** 处理成功 */
    SUCCESS("处理成功"),

    /** 校验失败 */
    VALIDATION_FAILED("校验失败"),

    /** 处理失败 */
    FAILED("处理失败");

    private final String description;

    ProcessStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
