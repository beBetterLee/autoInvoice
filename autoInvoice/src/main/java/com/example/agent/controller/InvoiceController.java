package com.example.agent.controller;

import com.example.agent.model.ApiResponse;
import com.example.agent.model.BatchProcessRequest;
import com.example.agent.model.ProcessContext;
import com.example.agent.orchestrator.AgentOrchestrator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 发票处理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final AgentOrchestrator orchestrator;

    /**
     * 单张发票处理
     *
     * @param fileUrl 发票文件URL
     * @return 处理结果（含链路追踪信息）
     */
    @PostMapping("/process")
    public ApiResponse<ProcessContext> process(
            @RequestParam @NotBlank(message = "fileUrl不能为空") String fileUrl) {
        log.info("收到发票处理请求: {}", fileUrl);
        ProcessContext result = orchestrator.process(fileUrl);
        return ApiResponse.success(result);
    }

    /**
     * 批量发票处理
     *
     * @param request 批量处理请求
     * @return 批量处理结果
     */
    @PostMapping("/batch")
    public ApiResponse<List<ProcessContext>> processBatch(@RequestBody @Valid BatchProcessRequest request) {
        log.info("收到批量处理请求, 数量: {}", request.getFileUrls().size());

        List<CompletableFuture<ProcessContext>> futures = orchestrator.processBatch(request.getFileUrls());

        // 等待所有任务完成
        List<ProcessContext> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        return ApiResponse.success(results);
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Invoice Agent System is running");
    }
}
