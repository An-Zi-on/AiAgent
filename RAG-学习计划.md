# RAG 相关知识点吃透计划（14 天版）

> 面向这个项目：Spring Boot 4.1.1 + Spring AI 2.0.1 + MySQL + Java 17
> 起点：能跑通 RAG，但启动被建索引堵死过一次（2026-10-22）
> 目标：**同样的坑不再踩第二次，并且 `RagIndexService` 这种代码能自己从零写出来**

---

## 0. 判定标准（唯一标准）

不看笔记、不看我给的代码，能独立完成下面三件事，才算这个计划毕业：

1. 从空白文件写出一个"启动不阻塞 + 失败可见 + 可手动重建"的索引器
2. 任何人问"这段代码什么时候执行、失败谁知道、跑第二遍会怎样"，你能一秒答出
3. 遇到新的"XX 卡住了"，第一反应是抓线程栈 / 加时间戳日志，而不是重启试试

---

## 1. 14 天总览（每天约 90 分钟：30 读 + 45 动手 + 15 记笔记）

| Day | 主题 | 动手实验 | 通过标准 |
|---|---|---|---|
| 1 | 事故复盘 | 写一页事故报告 | 能画出"启动 → 卡住"的调用链 |
| 2–3 | Bean 生命周期 | 三个 Bean 的启动顺序实验 | 说出 5 个初始化回调的先后 |
| 4 | `@Bean` 返回类型语义 | 复原 `List<Document>` 那个错误 Bean | 解释为什么它是"数据当 Bean" |
| 5 | 启动链路与 Web 容器 | 用时间戳证明 Tomcat 在 Bean 之后启动 | 说出"什么时候算启动完成" |
| 6–7 | 并发基础 | 用 `boolean` 制造双跑，再用 CAS 修好 | 解释 `volatile` 保护了什么 |
| 8 | 线程池 | 把 `runAsync` 换成自定义 Executor 并观察线程名 | 说出 commonPool 的坑 |
| 9 | 幂等 | 去掉 sha256 id，连续 reindex 两次 | 用文件大小证明"翻倍" |
| 10 | 可观测 | 给 reindex 加进度日志（每 50 篇一条） | 跑的时候能看出"还剩多少" |
| 11 | HTTP 超时 | 把 base-url 改成不可达地址 | 量出"最长卡多久" |
| 12 | 重试与失败 | 观察失败后 `/status` 应该长什么样 | 失败能在接口里看见 |
| 13 | RAG 检索参数 | 调 `topK` / `similarityThreshold` | 说出参数对答案的影响 |
| 14 | 重写验收 | 删掉 `RagIndexService` 自己重写 | 不看原文件写出来 |

---

## 2. 阶段一（Day 1–5）：Spring 到底在什么时候执行你的代码

### Day 1｜事故复盘（90 分钟）

**不写代码**，只写文档。新建 `docs/incident-001.md`，回答：

- 时间线：几点启动 → 卡了多久 → 我怎么发现的
- 根因：哪一行代码、在哪个阶段执行、为什么那个阶段不能调远程
- 证据：`jstack` 里 main 线程停在哪一帧（把你抓到的栈贴进去）
- 修复：改了什么，为什么这样就够了
- 预防：下次怎么 30 秒内判断"是卡在启动还是卡在请求"

> 这一天的价值比后面 13 天都大：**你以后再遇到"卡住"，会先抓栈，而不是先怀疑网络。**

### Day 2–3｜Bean 生命周期实验

在 `src/test/java` 下写一个临时测试类（不要放 main），定义：

```java
@Component
class ProbeA {
    @PostConstruct void init() { log("A @PostConstruct"); sleep(5000); }
    ProbeA() { log("A 构造"); }
}

@Component
class ProbeB implements InitializingBean {
    @Override public void afterPropertiesSet() { log("B afterPropertiesSet"); }
}

@Bean
ApplicationRunner probeRunner() {
    return args -> log("Runner 执行");
}
```

观察并记录：谁先谁后、总共耗时几秒、Tomcat 的 `Started ... in X seconds` 出现在第几行。

**通过标准**：能默写出这个顺序
`构造 → 依赖注入 → @PostConstruct → InitializingBean → BeanPostProcessor → ApplicationRunner → 启动完成`

**追问**：如果 `@PostConstruct` 里 sleep 5 秒，`Started in 7.3 seconds` 里的 7.3 会不会包含这 5 秒？

### Day 4｜`@Bean` 返回类型语义（把这天当成"破案"）

把最初那个错误签名的 Bean 复原：

```java
@Bean
public List<Document> setMyKeyWord(List<Document> documents) { ... }
```

然后**别急着改**，先观察 Spring 到底怎么处理它：启动报什么？参数 `List<Document>` 是从哪来的？会不会触发自引用？

**通过标准**：用自己的话讲清楚
- `@Bean` 方法的**返回类型**就是注册到容器的 Bean 类型
- 返回 `List<Document>` = 把"一次处理的结果"注册成了容器级依赖
- 返回 `KeywordMetadataEnricher` = 注册"一个能干活的工具"，创建它不花钱

### Day 5｜启动链路

用时间戳日志证明：`store()` 里的日志、`Tomcat started on port` 的日志、`Started CommonTemplateApplication` 的日志，谁在前谁在后。

**通过标准**：一句话说清"什么叫启动完成"——**Tomcat 开始监听端口，且 ApplicationRunner 跑完**。在这之前的所有阻塞，都表现为"服务起不来"。

---

## 3. 阶段二（Day 6–9）：并发、线程池、幂等

### Day 6–7｜可见性与原子性

**实验**：把 `RagIndexService` 的 `AtomicBoolean` 换成普通 `boolean`，在 `reindex()` 开头加：

```java
if (running) { log("已经在跑"); return false; }
running = true;
```

然后启动服务，**连续快速点两次** `POST /api/rag/reindex`，观察日志有没有出现"两个任务同时在跑"。

再换回 `AtomicBoolean.compareAndSet` 对比。

**通过标准**：
- 解释 `running` 为什么必须是 `volatile` 或原子的（后台线程写、Tomcat 线程读）
- 解释 `compareAndSet` 的"比较并交换"为什么能挡住第二个线程
- 说出 `volatile` **不**解决什么（它不保证 `i++` 原子）

### Day 8｜线程池

`CompletableFuture.runAsync(...)` 不传 Executor 时用的是 `ForkJoinPool.commonPool()`——一个全 JVM 共享、线程数 ≈ CPU 核数 - 1 的池子。

**实验**：在 `reindex()` 第一行打印 `Thread.currentThread().getName()`，看是不是 `onPool-worker-N`。然后换成一个固定大小的 Executor，再打印一次。

**通过标准**：能说出"为什么在 Web 应用里不该依赖 commonPool"（它会被其他 `parallelStream` 之类抢，而且没法优雅关闭）。

### Day 9｜幂等

**实验**：把 `withStableId()` 去掉（恢复默认随机 UUID 的 id），连续 `POST /reindex` 两次，然后对比：

```powershell
Get-Item embedding\vector_store.json | Select-Object Length
```

看文件大小、以及用编辑器数一下顶层 key 的数量变化。

**通过标准**：
- 说出 `SimpleVectorStore` 内部是 `Map<id, content>`，所以 **id 相同 = 覆盖**
- 说出为什么"内容哈希"比"文件名 + 序号"更适合当 id（文档改了，旧 id 自然失效）
- 举一个反例：什么情况下哈希 id 会误判（两段完全相同的文本）

---

## 4. 阶段三（Day 10–12）：可观测与超时

### Day 10｜可观测

给 `reindex()` 加"每处理 N 篇打一条日志"，把 499 篇的进度变成可看见的。

**通过标准**：运行 `POST /reindex` 后，单看日志就能回答：现在第几篇、还剩多久、有没有失败。

### Day 11｜超时（这天要量，不要猜）

**实验 A**：把 `spring.ai.openai.base-url` 改成 `https://10.255.255.1`（不可路由地址），启动并 `POST /reindex`，用秒表记录多久才报错。

**实验 B**：去 `~/.m2` 里翻 `openai-java-core-4.49.0.jar`，用 `javap` 或解压看 `ClientOptions` 的默认 timeout 和 maxRetries。

**通过标准**：能给出一个数字——"一次请求最坏卡 X 分钟"，并说清这个数字来自 SDk 默认值还是你自己的配置。

> 这就是当初"卡 8 分钟"的真正原因：**没有兜底的超时，等于把启动交给了别人家的服务器。**

### Day 12｜失败可见

把 `reindex()` 里的 `catch` 改成只 `log`，看看 `/api/rag/status` 返回什么。再改成写 `lastError`，对比。

**通过标准**：说出"用户点了重建、失败了、但他不知道"这种事故，应该由哪一层来防。

---

## 5. 阶段四（Day 13–14）：RAG 本体 + 验收

### Day 13｜检索参数

在 `AiChatService` 里把 `QuestionAnswerAdvisor` 换成显式 `SearchRequest`：

```java
QuestionAnswerAdvisor.builder(vectorStore)
    .searchRequest(SearchRequest.builder().topK(4).similarityThreshold(0.5).build())
    .build()
```

**实验**：`topK` 分别取 1 / 4 / 10，`similarityThreshold` 取 0.0 / 0.5 / 0.8，同一个问题问三遍，对比答案。

**通过标准**：能解释"为什么 topK 调大，答案不一定更好"（噪声、上下文长度、模型注意力）。

### Day 14｜重写验收

1. 把 `RagIndexService.java` 和 `RagController.java` 移到回收站
2. 只保留 `AppVectorStoreConfig` 里那个"只加载"的 store
3. **从零重写**，要求：启动不阻塞、失败可见、可手动重建、重复重建不翻倍
4. 写完再和原文件对比

**通过标准**：你的版本通过第 0 节的三个判定标准。差哪些行，就说明哪些知识点还没吃透——**差的那些行，就是你真正要补的**。

---

## 6. 自测题（全部不看代码回答）

1. `@Bean` 方法的返回类型和容器里注册的 Bean 是什么关系？
2. `@PostConstruct`、`InitializingBean`、`ApplicationRunner` 三者执行顺序？
3. 为什么 `@Bean` 里调用远程接口会让"启动卡住"？
4. `volatile` 在这里保证了什么？没保证什么？
5. `AtomicBoolean.compareAndSet` 为什么能挡住第二次触发？
6. `CompletableFuture.runAsync` 不传线程池时用哪个池？有什么风险？
7. `@Async` 为什么在"同类方法内部调用"时会失效？
8. `SimpleVectorStore` 的 id 冲突时，是覆盖还是追加？
9. 一次大模型请求最坏会卡多久？这个数字从哪来？
10. `topK` 和 `similarityThreshold` 分别控制什么？

---

## 7. 阅读清单（精确到能对着看的地方）

| 内容 | 看哪里 | 时长 |
|---|---|---|
| Bean 生命周期 | Spring 官方 docs "The IoC Container → Bean Lifecycle" | 40 min |
| `@Bean` 方法调用的拦截机制 | `ConfigurationClassEnhancer$BeanMethodInterceptor`（可反编译） | 30 min |
| 启动流程 | `ServletWebServerApplicationContext.onRefresh()` / `AbstractApplicationContext.refresh()` | 40 min |
| 并发基础 | 《Java 并发编程实战》第 3 章（可见性、volatile、happens-before） | 2 h |
| CAS | 同书第 15 章（非阻塞同步） | 40 min |
| 线程池 | 同书第 8 章 | 1 h |
| Spring AI RAG | 官方 docs "Retrieval Augmented Generation" | 40 min |
| `QuestionAnswerAdvisor` 参数 | 反编译 `spring-ai-vector-store-advisor` 看 `SearchRequest` 默认值 | 30 min |
| OkHttp 超时 | OkHttp docs "Timeouts" + `openai-java` 的 `ClientOptions` | 40 min |

> 你的 `node.md` 里已经在做"对照 `~/.m2` 里的源码核过"，这个习惯**继续保持**，它是这个计划里唯一不需要教的东西。

---

## 8. 笔记模板（每天 5 行）

```markdown
## Day N｜主题
- 今天验证了什么：（一句话）
- 关键证据：（日志 / 线程栈 / 文件大小 / 耗时数字）
- 我原来以为：…… 实际是：……
- 如果重来，我会怎么做：
- 还没搞懂的：（留到下一天）
```

---

## 9. 四问清单（贴显示器上）

每次写"有副作用"的代码前问自己：

1. **这段代码在哪个阶段执行？**（Bean 初始化 / 请求处理 / 后台任务）
2. **它慢了，谁被堵住？**
3. **它失败了，除了日志，还有谁看得见？**
4. **它跑第二遍会怎样？**

> 这四个问题，就是 `RagIndexService` 那 170 行存在的全部理由。
