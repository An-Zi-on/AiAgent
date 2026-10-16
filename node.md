# Spring AI 2.0.1 结构化输出实践笔记

> 面向本项目 `backend/agent`（Spring Boot 4.1.1 + Spring AI 2.0.1 + Jackson 3）。
> 所有结论均对照本地 `~/.m2` 的 **2.0.1 真实源码**核过，不是凭印象。

---

## 0. 环境与源码位置

| 项 | 值 |
|---|---|
| Spring AI | 2.0.1 |
| Spring Boot | 4.1.1（Boot 4） |
| Jackson | **3**（`tools.jackson.*`，不是 `com.fasterxml.jackson`） |
| Java | 17 |
| 端口 / 前缀 | 8123 / `/api` |
| 模型 | 智谱 `glm-5.3`，走 **OpenAI 兼容协议**（`open.bigmodel.cn`） |
| 用的 ChatModel | `OpenAiChatModel`（starter 注入） |

**源码 jar（自己对着读，比看文档准）：**

```
C:\Users\qq529\.m2\repository\org\springframework\ai\spring-ai-client-chat\2.0.1\spring-ai-client-chat-2.0.1-sources.jar
  org/springframework/ai/chat/client/ChatClient.java              CallResponseSpec 在 236 行，EntityParamSpec 在 202 行
  org/springframework/ai/chat/client/DefaultChatClient.java       doResponseEntity 505，doSingleWithBeanOutputConverter 570+，
                                                                  resolveAdvisorChain 在 EntityParamSpec 消费处
  org/springframework/ai/chat/client/ResponseEntity.java
  org/springframework/ai/chat/client/ChatClientAttributes.java
  org/springframework/ai/chat/client/advisor/ChatModelCallAdvisor.java        augmentWithFormatInstructions 66，关键在 94
  org/springframework/ai/chat/client/advisor/StructuredOutputValidationAdvisor.java

C:\Users\qq529\.m2\repository\org\springframework\ai\spring-ai-model\2.0.1\spring-ai-model-2.0.1-sources.jar
  org/springframework/ai/converter/BeanOutputConverter.java       convert 190，getJsonMapper 201，getFormat 214，getJsonSchema 231
  org/springframework/ai/converter/MarkdownCodeBlockCleaner.java
  org/springframework/ai/model/tool/StructuredOutputChatOptions.java
  org/springframework/ai/chat/model/ChatResponse.java
  org/springframework/ai/chat/prompt/Prompt.java                  augmentUserMessage 261
  org/springframework/ai/chat/metadata/ChatResponseMetadata.java  getUsage / getModel
  org/springframework/ai/chat/metadata/Usage.java                 getPromptTokens / getCompletionTokens / getTotalTokens
  org/springframework/ai/chat/metadata/ChatGenerationMetadata.java getFinishReason

C:\Users\qq529\.m2\repository\org\springframework\ai\spring-ai-openai\2.0.1\spring-ai-openai-2.0.1-sources.jar
  org/springframework/ai/openai/OpenAiChatOptions.java            57 行：implements ... , StructuredOutputChatOptions
  org/springframework/ai/openai/OpenAiChatModel.java              772 行起：ResponseFormat.JSON_SCHEMA 的请求映射
```

---

## 1. 2.0.1 可用的结构化输出 API 面

| 能力 | 2.0.1 的真实入口 |
|---|---|
| 转成对象 | `entity(Class<T>)` / `entity(ParameterizedTypeReference<T>)` / `entity(StructuredOutputConverter<T>)`，各有带 `Consumer<EntityParamSpec>` 的重载 |
| 拿原始 + 解析 | `responseEntity(...)` → `record ResponseEntity<R, E>(response, entity)` |
| **2.0 新增**：原生结构化输出 | `EntityParamSpec.useProviderStructuredOutput()` |
| **2.0 新增**：schema 校验重试 | `EntityParamSpec.validateSchema()` 或独立 `StructuredOutputValidationAdvisor`（默认重试 3 次） |
| 转换器 | `BeanOutputConverter` / `ListOutputConverter` / `MapOutputConverter`，`getFormat()` + `getJsonSchema()` |
| 文本清洗 | `MarkdownCodeBlockCleaner` / `ThinkingTagCleaner` / `WhitespaceCleaner` / `CompositeResponseTextCleaner` |
| 流式 | `StreamResponseSpec` **只有** `content()` / `chatResponse()` / `chatClientResponse()` —— **没有 `entity()`** |

---

## 2. L1–L7 分级需求与进度

| 级别 | 主题 | 状态 |
|---|---|---|
| L1 | `entity(Class)` 最小闭环 | ✅ 已完成 |
| L2 | `ParameterizedTypeReference` 泛型 | ✅ 已完成 |
| L3 | `responseEntity()` 拿回 ChatResponse | ⚠️ 部分通过，有 2 个必改 bug |
| L4 | 手动两段式：拆开 `entity()` 黑盒 | ⚠️ 骨架完成，缺最后一步 |
| L5 | 原生结构化输出 `useProviderStructuredOutput()` | 未开始 |
| L6 | schema 校验与自动重试 | 未开始 |
| L7 | 接回 `knowledge` 场景（`AnswerVO`） | 未开始 |

### L1 / L2 结论

- `record MovieInfo(String name, Integer age, List<String> movies)` —— 字段设计够用。
- **L1**：默认 `entity()` 是**提示词工程** —— 框架把 JSON Schema 当文本追加进 prompt，再解析回答。不是 API 级强约束。
- **L2**：`entity(List.class)` 拿不到泛型（JVM 擦除），`ParameterizedTypeReference` 靠匿名子类保住 `Type` 才绕过。

---

## 3. L3 验收结果

### 要求

`GET /movie/raw` 返回：解析的 `MovieInfo` + 原始文本 + prompt/completion/total tokens + model + finishReason，全部取自 `responseEntity()`。
外加：`finishReason == "length"` 时 `log.warn`；显式传一次 `BeanOutputConverter`。

### 逐条对照

| 要求 | 结果 |
|---|---|
| 解析出的 `MovieInfo` | ✅ |
| 原始文本 | ❌ 只 `log.info` 了，没放进返回对象 |
| 三个 token 数 | ✅ 值正确 |
| `model` | ❌ 只 `log.info` 了，record 里根本没这个字段 |
| `finishReason` | ⚠️ 字段有，**值传错** |
| 截断告警 | ⚠️ 写了，有 NPE 风险 |
| 显式 `BeanOutputConverter` | ❌ 死代码，零调用点 |

### 🔴 Bug 1：`finishReason` 位置传的是 `completionTokens`

`App.java:69`

```java
return new MoveInfoEntityResult(totalTokens, promptTokens, completionTokens, completionTokens, responseEntity.getEntity());
//                              ↑totalTokens  ↑promptTokens  ↑completion      ↑finishReason ← 传错
```

record 里 `finishReason` 类型写成了 `Integer`，而 `completionTokens` 恰好也是 `Integer`
→ **编译过、不报错、静默错值**。
`finishReason` 应该是 `String`。

### 🔴 Bug 2：`finishReason.equals("length")` 会 NPE

`App.java:56`。`ChatGenerationMetadata.getFinishReason()` 返回 `@Nullable String`，
provider 没回 `finish_reason` 时是 `null`。应写 `"length".equals(finishReason)`。

### 其他

- `responseEntity.getResponse()` / `.getResult()` / `.getEntity()` 都是 `@Nullable`，链式调用无保护。
- `getEntity()` 可以是 `null`（这正是 L3 的观察点）—— 直接塞进 record 返回，前端拿到 `"result": null`，
  **分不清"模型没吐内容"还是"吐了但解析失败"**。返回对象里该带个标记。
- `chatMovies` 里 `new ParameterizedTypeReference<>() {}` 改成了菱形推断。能编译，但**能推断成功只因为外层 `return List<MovieInfo>` 提供了目标类型**；一旦被重构挪走（比如赋给 `var`）会静默推出别的类型。建议改回显式 `ParameterizedTypeReference<List<MovieInfo>>(){}`。
- `chatGetRespond1`（用了显式 converter）全项目零调用点 —— 验收要求的"跑通"没达成。最省事的验证：把 `/movie/raw` 改成调它。因为 `responseEntity(Class)` 本来就等于 `responseEntity(new BeanOutputConverter<>(type))`，两边结果应完全一致。

---

## 4. L4 验收结果

### 要求

不用 `entity()` / `responseEntity()`，手写：拿 `getFormat()` → `{format}` 进 yml 场景模板 → 渲染 →
拿原始字符串 → **自己 `convert()`** → 返回强类型对象。

### 做对的部分

- ✅ `BeanOutputConverter<MovieInfo>` + `getFormat()`
- ✅ `result-format` 从 `AiTemplateProperties.AiScenes` → `Scenes` → `AiTemplateRegister` 整条链路打通
- ✅ `PromoteTemplateRenderer.resultFormat(template, Map.of("format", format))` 用 `{format}` 占位
- ✅ yml `knowledge.result-format` 配上了
- ✅ **`{` 分隔符的坑绕过去了**：schema 是作为 Map 的**值**传进去的，不是写在模板源码里，`StTemplateRenderer` 不会解析它。

### 🔴 Bug 3（严重）：`/api/status` 每次请求调了两次模型

`AiChatService.java:76-79`

```java
ChatResponse chatResponse = call.chatResponse();   // ← 第 1 次真实调用
String text = chatResponse.getResult().getOutput().getText();
log.error(text);
return call.content();                             // ← 第 2 次真实调用
```

`DefaultChatClient$DefaultCallResponseSpec.content()` 的实现是：

```java
public @Nullable String content() {
    ChatResponse chatResponse = doGetObservableChatClientResponse(this.request).chatResponse();
    return getContentFromChatResponse(chatResponse);
}
```

而 `doGetObservableChatClientResponse` 里就是 **`advisorChain.nextCall(chatClientRequest)`** —— **没有缓存**。

后果：
- 每次 `/api/status` **付两份 token**。
- **`MessageChatMemoryAdvisor` 把同一轮对话往 JDBC 里写两次**，20 条窗口被白占一半。

修法：直接用已拿到的 `chatResponse`，别再回头调 `call.content()`：

```java
return chatResponse.getResult().getOutput().getText();
```

### 未完成的部分

| 要求 | 结果 |
|---|---|
| 拿模型返回的原始字符串 | ✅ `text` 拿到了 |
| **自己调 `converter.convert(text)`** | ❌ 完全没调，`converter` 只用了 `getFormat()` |
| **接口返回强类型对象** | ❌ `doScene` 仍 `return call.content()` 返回 String |

`AiChatService.java:77-79` 里 `text` 拿到手、打个日志、然后就扔了 —— 它本该是 `converter.convert(text)` 的入参。

### 其他

- `log.error(text)`（`AiChatService.java:78`）：正常业务输出用 error 级别不对，且 `MyLoggerAdvisor` 已打过响应，重复。改 debug 或删掉。
- `PromoteTemplateRenderer.resultFormat`（第 19 行）**没有** `StrUtil.isBlank` 保护（`render` 第 28 行有）。
  `interview` 场景没配 `result-format` → `null` → `PromptTemplate.builder().template(null)` 直接 Assert 挂掉。
  现在只因 `App.doScene` 写死 `"knowledge"` 才没炸，等 L7 切场景就会踩。
- yml 里手抄的 `getFormat()` 原文与框架不完全一致（多了 4 个空格缩进、结尾多了个 `;`）——
  **这正说明手抄 format 文本是坏主意**，框架改文案就静默漂移。
  想干净：删掉 yml 的 `result-format`，代码里直接 `userPromo + "\n\n---\n\n" + converter.getFormat()`。
- 框架追加 format 用的是 `System.lineSeparator()`（**一个换行**），本项目用的是 `"\n\n---\n\n"`。
  不影响正确性，但复刻 `entity()` 时知道差在哪。

---

## 5. `entity()` 到底帮你做了哪两件事

要连着看三个地方。

### ① `DefaultChatClient.doSingleWithBeanOutputConverter`（`entity()` 的内部）

```java
if (StringUtils.hasText(outputConverter.getFormat())) {
    this.request.context().put(ChatClientAttributes.OUTPUT_FORMAT.getKey(), outputConverter.getFormat());
}
// ... 真正调模型 ...
var stringResponse = getContentFromChatResponse(chatResponse);
if (stringResponse == null) return null;
return outputConverter.convert(stringResponse);
```

### ② `ChatModelCallAdvisor.augmentWithFormatInstructions`（真正把文本塞进 prompt 的地方）

```java
68:  String outputFormat = (String) chatClientRequest.context().get(ChatClientAttributes.OUTPUT_FORMAT.getKey());
...
92:  Prompt augmentedPrompt = chatClientRequest.prompt()
93:      .augmentUserMessage(userMessage -> userMessage.mutate()
94:          .text(userMessage.getText() + System.lineSeparator() + outputFormat)   // ← 就这儿
95:          .build());
```

`Prompt.augmentUserMessage`（`Prompt.java:261`）的语义：**从后往前找，改的是最后一条 user message**；
没有 user message 就补一条空的。

### 答案

> **第一件**：把 `converter.getFormat()` 那段文本，**追加到最后一条 user message 的末尾**（是 user，不是 system）。
> **第二件**：模型回来之后，把字符串喂给 `converter.convert(text)`，转成强类型对象。

### 这个方法的分支结构（L5 要用）

| 行 | 分支 | 行为 |
|---|---|---|
| 73-75 | 两个都没有 | 原样返回，什么都不做 |
| 80-90 | **原生模式**（`useProviderStructuredOutput()`） | 走 `chatOptions.outputSchema(schema)`，**完全不动 user message** |
| 92-95 | 提示词模式（默认） | 把 format 文本拼进 user message（94 行） |

> 80-90 那个分支就是「`base.txt` 说要用 JSON + 原生 schema 同时存在谁赢」的答案所在：**原生模式赢了，且完全不碰 user message。**

---

## 6. 绕开 `entity()` 之后多拿到什么控制权

1. **format 文本放哪儿、放几次，你说了算。** 框架写死追加到**最后一条 user message 末尾**。
   你可以放 system、放最前面、或者**根本不放**（L5 原生模式就不需要）。
2. **你能改 format 文本本身。** 框架那 5 行是硬编码英文；换中文说明，中文模型往往更听话。
3. **你能换清洗器。** 走三参构造 `new BeanOutputConverter<>(type, jsonMapper, myCleaner)`。
4. **失败时你能兜底。** 自己 `convert()` 就能 try/catch：重试、降级返回原始文本、正则抠 JSON 都行。

### 但说实话：什么时候**不该**手写

L4 手写一遍的目的是**知道黑盒里是什么**，不是以后都手写。

真实工程里默认还是 `entity()` / `responseEntity()` —— 尤其 L5 的 `useProviderStructuredOutput()`、
L6 的 `validateSchema()`，**只有通过 `entity(Class, Consumer<EntityParamSpec>)` 这条路才拿得到**，
手写两段式反而够不着。

手写真正有价值的场景只有一个：**你要在 format 文本上做框架不让你做的事**（换语言、换位置、不放）。
除此之外 `responseEntity()` 已经覆盖了"拿元数据"的需求。

---

## 7. `converter.convert(text)` 在干什么

`BeanOutputConverter.java:190-195`，整个方法就两行：

```java
public T convert(String text) {
    text = this.textCleaner.clean(text);                                        // ① 清洗
    return (T) this.jsonMapper.readValue(text, this.jsonMapper.constructType(this.type));  // ② 反序列化
}
```

**最要紧的一句话：它不调模型。** 模型早就调完了，`text` 是已经躺在手里的字符串。**纯本地**操作。

### ① 清洗

走构造时定好的链：

```
WhitespaceCleaner → ThinkingTagCleaner → MarkdownCodeBlockCleaner → WhitespaceCleaner
```

模型有时会回 ` ```json ... ``` `，`MarkdownCodeBlockCleaner` 负责剥掉前后反引号。没这步 Jackson 直接失败。

### ② 反序列化

- `this.type` 是**构造时就捕获好的**（第 140 行 `this.type = type;`）。
  `BeanOutputConverter(MovieInfo.class)` 存 `Class`；`BeanOutputConverter(ParameterizedTypeReference<T>)` 存带泛型的 `ParameterizedType`。
- `constructType(this.type)` 转成 Jackson 的 `JavaType`，`readValue` 拿它构造对象。

**这里是 L2 的回报**：`List<MovieInfo>` 之所以能解析出真正的 `MovieInfo` 元素而不是一堆 `LinkedHashMap`，
全靠 `this.type` 是 `ParameterizedType` 而没被擦除。

### 失败图谱（重要）

| 情况 | `entity()` 的结局 |
|---|---|
| 模型根本没吐内容 | 返回 `null` |
| 吐了但 JSON 格式坏 | **抛异常**（`readValue` 抛 `JacksonException`，`convert()` 不吞） |
| 吐了、JSON 合法、缺字段 | 正常返回对象，缺的字段是 `null` |
| 吐了、JSON 合法、多字段 | 正常返回，多余字段被忽略（`disable(FAIL_ON_UNKNOWN_PROPERTIES)`） |

→ 这就是「模型没给」和「格式错」的区分方式：**一个给 null，一个给异常**。

### 坑：这个 JsonMapper 不读你的 Spring 配置

```java
protected JsonMapper getJsonMapper() {
    return JsonMapper.builder()
        .addModules(JacksonUtils.instantiateAvailableModules())
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();
}
```

`application.yml` 里的 `spring.jackson.date-format` / `time-zone` **对它不生效** ——
那是 Spring Boot 给 Web 层的 mapper，这里是 `BeanOutputConverter` 自己 new 的独立实例。

现在 `MovieInfo` 没有日期字段感觉不到；等 L7 的 `AnswerVO` 或任何 schema 里出现 `LocalDateTime` / `Date`，
**解析格式会跟预期不一样**。想控制得走三参构造自己传 `JsonMapper`。

---

## 8. `responseEntity()` vs `entity()`

```java
public <T> @Nullable T entity(Class<T> type) {                    // DefaultChatClient:567
    Assert.notNull(type, "type cannot be null");
    return doSingleWithBeanOutputConverter(new BeanOutputConverter<>(type));
}

public <T> ResponseEntity<ChatResponse, T> responseEntity(Class<T> type) {   // :464
    Assert.notNull(type, "type cannot be null");
    return doResponseEntity(new BeanOutputConverter<>(type));
}
```

**`Class` 重载只是语法糖**，都等价于显式传 `new BeanOutputConverter<>(type)`。

### 关键差异

`doResponseEntity` 内部：

```java
this.request.context().put(ChatClientAttributes.OUTPUT_FORMAT.getKey(), outputConverter.getFormat());

if (Boolean.TRUE.equals(this.request.context().get(ChatClientAttributes.STRUCTURED_OUTPUT_NATIVE.getKey()))) {
    this.request.context().put(ChatClientAttributes.STRUCTURED_OUTPUT_SCHEMA.getKey(), outputConverter.getJsonSchema());
}

var chatResponse = doGetObservableChatClientResponse(this.request, advisorChain).chatResponse();
var responseContent = getContentFromChatResponse(chatResponse);
if (responseContent == null) {
    return new ResponseEntity<>(chatResponse, null);   // wrapper 非 null，entity 可空
}
T entity = outputConverter.convert(responseContent);
return new ResponseEntity<>(chatResponse, entity);
```

对比 `doSingleWithBeanOutputConverter` 的 `if (stringResponse == null) return null;`：

- **`responseEntity()` 永远给你一个非空 wrapper** —— `getResponse()` 一定能拿到 `ChatResponse`（token、模型名、finishReason）。
- **`entity()` 把 `ChatResponse` 整个扔掉**，两种情况都塌成同一个 `null`。

### 两个坑

1. **`org.springframework.ai.chat.client.ResponseEntity` ≠ `org.springframework.http.ResponseEntity`**，同名两个东西。
   它的定义是 `public record ResponseEntity<R, E>(R response, E entity)`。
2. **别把它当 controller 出参直接返回** —— Spring MVC 会把它当普通 `@ResponseBody` 值交给 Jackson，
   于是 `ChatResponse` 被整个序列化进去，报错或者吐一大坨。要包一层自己的 record。

---

## 9. L5 / L6 / L7 待做

### L5 — 原生结构化输出（最有探索价值）

```java
.entity(MovieInfo.class, spec -> spec.useProviderStructuredOutput())
```

**判定链路（已核实）：**
1. `resolveAdvisorChain` 里 `spec.isEnableNative()` 为真 → context 塞 `STRUCTURED_OUTPUT_NATIVE=true`
2. `doResponseEntity` 看到 flag 才把 `getJsonSchema()` 塞进 `STRUCTURED_OUTPUT_SCHEMA`
3. `ChatModelCallAdvisor`（80-90 行）判定：native && schema 有 && `prompt.getOptions() instanceof StructuredOutputChatOptions`
   → 走 API 级；**否则静默降级回"追加文本到 user message"**

**本项目不会静默降级**：`DefaultChatClientUtils:103` 是
`ChatOptions.Builder<?> builder = inputRequest.getChatModel().getOptions().mutate();`
prompt 的 options 来自 `chatModel.getOptions()` → `OpenAiChatOptions`，它 `implements ..., StructuredOutputChatOptions`。

**实际发出的报文**（`OpenAiChatModel` 的 `JSON_SCHEMA` 分支）：

```json
"response_format": {
  "type": "json_schema",
  "json_schema": { "name": "json_schema", "strict": true, "schema": { ... } }
}
```

`name` 硬编码 `"json_schema"`，`strict` 默认 `true`。

**待验证的问题：智谱的 OpenAI 兼容层认不认 `json_schema`？**
国内大部分厂商只支持 `json_object`。这是这一级最值得实测的点。

**另一个已知限制**：OpenAI Structured Outputs **不接受顶层数组 schema**，`List<T>` 会直接失败，
必须包一层容器 record。验证在智谱身上是否成立。

### L6 — 校验与自动重试

两条路都走：内联 `spec -> spec.validateSchema()` / 独立 `StructuredOutputValidationAdvisor.builder()`。

**三个非显然的坑（已核实）：**

1. **`maxRepeatAttempts(3)` 意味着模型总共被调 4 次**，不是 3 次。
   循环是 `for (var currentAttemptNumber = 1 + this.maxRepeatAttempts; currentAttemptNumber > 0; ...)`
   —— 这是**重试次数**。Builder 注释：「Zero means no retries; the model is called exactly once」。

2. **校验器不清洗文本，converter 清洗。**
   `validateOutputSchema` 直接拿 `getOutput().getText()` 去 `jsonMapper.readTree(json)`，
   **完全绕过了 `WhitespaceCleaner → ThinkingTagCleaner → MarkdownCodeBlockCleaner` 链**。
   → 模型回 ` ```json ` 包裹内容时，**`validateSchema()` 报失败，而同响应 `convert()` 本来能解析成功**，
   触发一次完全没必要的重试，白烧 token。

3. 用 `networknt` 校验器，方言写死 `SpecificationVersion.DRAFT_2020_12`，
   只认 `required` / `enum` / `min-max` 这类 JSON Schema 约束。
   Java 类型上的业务校验注解（`@Min` 等）**不会**被转成 schema 约束。

**其他事实：**
- 失败时是 **`augmentUserMessage`**，错误文本 `"Output JSON validation failed because of: " + 详情` 追加到 **user message 尾部**。
- `adviseStream` 直接 `Flux.error(new UnsupportedOperationException(...))` —— **不支持流式**。
- **内联版没有任何旋钮**：`resolveAdvisorChain` 里是
  `.builder().outputJsonSchema(converter.getJsonSchema()).build()` ——
  order 固定 `LOWEST_PRECEDENCE - 2000`、重试固定 3 次。要调就走独立 advisor。

### L7 — 接回 `knowledge` 场景

`App.doScene` → `GET /api/status` 返回 `AnswerVO(String answer, List<String> keyPoints, Double confidence, List<String> sources)`。
`base.txt` 里那句「返回的结果必须使用JSON格式返回」到这里才真正被兑现。
场景提示词（`.st`）和 yml 用户模板都不用改。
进阶：开 `ChatMemory` 再测一遍 —— schema 说明会被写进 JDBC 对话历史吗？20 条窗口会被挤占吗？

---

## 10. 待修清单

按优先级：

1. **`AiChatService.java:79`** — 删掉 `return call.content()`，改返回已有的 `chatResponse`，止住双倍调用。
2. **`App.java:69`** — `MoveInfoEntityResult.finishReason` 改 `String`，第 4 个参数传 `finishReason`。
3. **`App.java:56`** — 改成 `"length".equals(finishReason)`。
4. **`MoveInfoEntityResult`** — 补 `model` 和原始文本两个字段，`App.java` 里填上。
5. **`AiChatService.java:77-79`** — 调 `converter.convert(text)`，`doScene` 返回类型改成强类型对象。
6. **`AiChatService.java:156`** — 让 `/movie/raw` 走 `chatGetRespond1`，把 L3.3 跑通。
7. 杂项：`log.error(text)` 降级或删；`resultFormat` 补空值保护；`ParameterizedTypeReference` 改回显式泛型。

---

## 11. 贯穿全程的踩坑清单（自测题）

1. `StreamResponseSpec` **只有** `content()` / `chatResponse()` / `chatClientResponse()`，没有 `entity()`。
   流式场景怎么做结构化输出？
2. 解析失败时 `entity()` 返回 null 还是抛异常？怎么区分「模型没给」和「JSON 格式错」？（见 §7 失败图谱）
3. `ChatMemory` 存进 JDBC —— schema 说明会一起被存吗？20 条窗口会被挤占吗？
4. Lombok 的 `@Data` getter 和 `record` accessor，哪个能被 `JsonSchemaGenerator` 正确推导出 schema？
5. `MyLoggerAdvisor.getOrder()` 是 `Integer.MIN_VALUE + 100`（最外层），校验 advisor 默认 `LOWEST_PRECEDENCE - 2000`。
   **谁先执行？重试的中间轮次 logger 看得到吗？**（去 `DefaultAroundAdvisorChain` 确认 `push` 与排序的关系）
6. `base.txt` 说「用 JSON 返回」，`useProviderStructuredOutput` 又用 API 传 schema —— 谁赢？（见 §5 分支表）

### 模板相关的坑

`PromptTemplate` 默认 renderer 是 `StTemplateRenderer`，**默认以 `{` 和 `}` 作分隔符**
（`DEFAULT_START_DELIMITER_TOKEN = '{'`）。

→ `{format}` 作为**变量名**写进 yml 是对的（schema 作为 Map 的值传入，不会被二次解析）。
→ 但若把格式说明文本**直接手写进模板源码**（里面有 JSON 花括号），StringTemplate 会把 `{` 当变量开头，炸。
