# 发票智能处理 Agent 系统 v2.0

基于多 Agent 协同与流程编排机制，实现发票从识别、校验到分类的自动化闭环处理系统。

## 系统架构

```
┌─────────────────────────────────────────────────────────┐
│                    API Layer (REST)                       │
│              POST /api/v1/invoice/process                 │
│              POST /api/v1/invoice/batch                   │
└─────────────────────┬───────────────────────────────────┘
                      │
┌─────────────────────▼───────────────────────────────────┐
│              Agent Orchestrator (流程编排器)               │
│         链路追踪 · 异常处理 · 状态管理 · 批量调度          │
└─────────────────────┬───────────────────────────────────┘
                      │
        ┌─────────────┼─────────────┐
        ▼             ▼             ▼
┌──────────────┐ ┌──────────┐ ┌──────────────┐
│  OCR Agent   │ │ Validation│ │Classification│
│  (识别提取)   │ │  Agent    │ │    Agent     │
│              │ │ (业务校验) │ │  (智能分类)   │
└──────────────┘ └──────────┘ └──────────────┘
```

## 技术栈

- Java 17 + Spring Boot 3.2
- 多 Agent 协同 + Pipeline 编排模式
- 异步批量处理（CompletableFuture + 线程池）
- 链路追踪（traceId）
- 统一异常处理与响应封装

## 核心特性

### 1. 多 Agent 协同
- **OCR Agent**: 发票信息提取（支持 Mock/腾讯云/百度 OCR 切换）
- **Validation Agent**: 多维度业务规则校验（金额、税号、一致性）
- **Classification Agent**: 基于规则的智能分类（可扩展为 LLM 驱动）

### 2. 流程编排器（Orchestrator）
- 基于 `getOrder()` 自动排序执行
- 异常中断与错误传播
- 处理状态全程追踪
- 支持批量异步并发处理

### 3. 链路追踪
每次处理生成唯一 `traceId`，记录完整处理步骤：
- 每个 Agent 的执行结果
- 校验详情（逐字段）
- 处理耗时统计

### 4. 可扩展设计
- Agent 接口统一，新增 Agent 只需实现接口并注册为 Spring Bean
- OCR 提供商通过配置切换
- 分类规则配置化，支持热更新
- 预留 LLM 接入能力

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.8+

### 启动
```bash
mvn spring-boot:run
```

### API 调用

#### 单张发票处理
```bash
curl -X POST "http://localhost:8080/api/v1/invoice/process?fileUrl=https://example.com/invoice.pdf"
```

#### 批量处理
```bash
curl -X POST http://localhost:8080/api/v1/invoice/batch \
  -H "Content-Type: application/json" \
  -d '{"fileUrls": ["url1", "url2", "url3"]}'
```

### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "traceId": "a1b2c3d4e5f6g7h8",
    "fileUrl": "https://example.com/invoice.pdf",
    "invoiceData": {
      "invoiceNo": "INV-1714300000000",
      "amount": 8500.00,
      "taxAmount": 1105.00,
      "totalAmount": 9605.00,
      "buyerName": "北京创新科技有限公司",
      "sellerName": "上海数字服务有限公司",
      "category": "IT服务"
    },
    "status": "SUCCESS",
    "duration": 52,
    "steps": [...],
    "validationResults": [...]
  },
  "timestamp": "2026-04-29T10:00:00"
}
```

## 配置说明

```yaml
agent:
  ocr:
    provider: mock          # mock / tencent / baidu
  validation:
    max-amount: 9999999.99  # 金额上限
    tax-no-length: 15       # 税号最小长度
  classification:
    rules:                  # 分类关键词映射
      科技: IT服务
      咨询: 咨询服务
      餐饮: 餐饮服务

orchestrator:
  thread-pool-size: 4       # 批量处理线程数
  process-timeout: 60000    # 处理超时(ms)
```

## 演进路线

- [x] MVP: 规则驱动 + Mock OCR
- [ ] 接入真实 OCR 服务（腾讯云/百度）
- [ ] 接入 LLM 实现语义分类
- [ ] 条件分支（不同发票类型走不同流程）
- [ ] 异常重试与回滚机制
- [ ] 持久化存储（处理记录入库）
- [ ] 审批流对接
- [ ] 监控告警（处理成功率、耗时等）
