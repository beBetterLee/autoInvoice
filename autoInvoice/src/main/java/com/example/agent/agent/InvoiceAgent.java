package com.example.agent.agent;

import com.example.agent.model.ProcessContext;

/**
 * Agent统一接口
 * 所有Agent基于ProcessContext进行处理，实现链式调用
 */
public interface InvoiceAgent {

    /**
     * 获取Agent名称
     */
    String getName();

    /**
     * 执行处理
     *
     * @param context 处理上下文
     * @return 处理后的上下文
     */
    ProcessContext process(ProcessContext context);

    /**
     * 获取Agent执行顺序（数值越小越先执行）
     */
    default int getOrder() {
        return 0;
    }
}
