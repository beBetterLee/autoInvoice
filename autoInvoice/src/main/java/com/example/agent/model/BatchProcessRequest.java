package com.example.agent.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 批量处理请求
 */
@Data
public class BatchProcessRequest {

    @NotEmpty(message = "文件URL列表不能为空")
    private List<String> fileUrls;
}
