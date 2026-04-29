package com.example.agent.orchestrator;

import com.example.agent.agent.InvoiceAgent;
import com.example.agent.model.ProcessContext;
import com.example.agent.model.ProcessStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Agent流程编排器
 * 负责将多个Agent按顺序串联成处理链路：OCR → 校验 → 分类 → 输出
 * 支持：
 * - 有序执行（基于Agent.getOrder()）
 * - 异常中断与错误传播
 * - 链路追踪（traceId）
 * - 批量异步处理
 */
@Slf4j
@Service
public class AgentOrchestrator {

    private final List<InvoiceAgent> agents;
    private final ExecutorService executorService;

    @Value("${orchestrator.process-timeout:60000}")
    private long processTimeout;

    public AgentOrchestrator(List<InvoiceAgent> agents,
                             @Value("${orchestrator.thread-pool-size:4}") int threadPoolSize) {
        // 按order排序
        this.agents = agents.stream()
                .sorted(Comparator.comparingInt(InvoiceAgent::getOrder))
                .collect(Collectors.toList());
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);

        log.info("Agent编排器初始化完成，注册Agent数量: {}, 执行顺序: {}",
                this.agents.size(),
                this.agents.stream().map(InvoiceAgent::getName).collect(Collectors.joining(" → ")));
    }

    /**
     * 单个发票处理
     */
    public ProcessContext process(String fileUrl) {
        String traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        log.info("[Orchestrator] 开始处理, traceId={}, fileUrl={}", traceId, fileUrl);

        ProcessContext context = ProcessContext.builder()
                .traceId(traceId)
                .fileUrl(fileUrl)
                .status(ProcessStatus.PENDING)
                .startTime(LocalDateTime.now())
                .build();

        // 依次执行Agent链路
        for (InvoiceAgent agent : agents) {
            try {
                log.debug("[Orchestrator] 执行Agent: {}", agent.getName());
                context = agent.process(context);

                // 如果状态为失败，中断链路
                if (context.getStatus() == ProcessStatus.FAILED) {
                    log.warn("[Orchestrator] Agent[{}]执行失败，中断链路, traceId={}",
                            agent.getName(), traceId);
                    break;
                }

                // 校验失败时，仍继续分类（业务需求：即使校验不通过也要分类归档）
                // 如需严格中断，可在此处添加判断

            } catch (Exception e) {
                log.error("[Orchestrator] Agent[{}]执行异常: {}", agent.getName(), e.getMessage(), e);
                context.setStatus(ProcessStatus.FAILED);
                context.setErrorMessage("Agent[" + agent.getName() + "]异常: " + e.getMessage());
                context.addStep(agent.getName(), "异常", false, e.getMessage());
                break;
            }
        }

        // 设置最终状态
        if (context.getStatus() != ProcessStatus.FAILED
                && context.getStatus() != ProcessStatus.VALIDATION_FAILED) {
            context.setStatus(ProcessStatus.SUCCESS);
        }

        context.setEndTime(LocalDateTime.now());
        context.setDuration(Duration.between(context.getStartTime(), context.getEndTime()).toMillis());

        log.info("[Orchestrator] 处理完成, traceId={}, status={}, duration={}ms",
                traceId, context.getStatus(), context.getDuration());

        return context;
    }

    /**
     * 批量异步处理
     */
    public List<CompletableFuture<ProcessContext>> processBatch(List<String> fileUrls) {
        log.info("[Orchestrator] 批量处理开始, 数量: {}", fileUrls.size());

        return fileUrls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> process(url), executorService))
                .collect(Collectors.toList());
    }
}
