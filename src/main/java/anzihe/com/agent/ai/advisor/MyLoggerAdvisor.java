package anzihe.com.agent.ai.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import reactor.core.publisher.Flux;

@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    /** 日志内容截断长度 */
    private static final int MAX_LOG_LENGTH = 200;

    @Override
    public String getName() {
        return "MyLoggerAdvisor";
    }

    @Override
    public int getOrder() {
        // 设为最高优先级，确保能包裹整个链路
        return Integer.MIN_VALUE + 100;
    }

    // ==================== 非流式 ====================

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // ① 请求前
        long start = System.currentTimeMillis();
        String convId = extractConversationId(request);
        logRequest(convId, request);

        // ② 调下一个
        ChatClientResponse response;
        try {
            response = chain.nextCall(request);
        } catch (Exception e) {
            log.error("[AI-请求异常] convId={} 耗时={}ms", convId,
                    System.currentTimeMillis() - start, e);
            throw e;
        }

        // ③ 响应后
        long cost = System.currentTimeMillis() - start;
        logResponse(convId, response, cost);
        return response;
    }

    // ==================== 流式 ====================

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest request, StreamAdvisorChain chain) {
        long start = System.currentTimeMillis();
        String convId = extractConversationId(request);
        logRequest(convId, request);

        Flux<ChatClientResponse> stream = chain.nextStream(request);

        // 用聚合器在"完整响应"时打日志
        return new ChatClientMessageAggregator().aggregateChatClientResponse(
                stream,
                response -> logResponse(convId, response, System.currentTimeMillis() - start)
        );
    }

    // ==================== 工具方法 ====================

    /** 从 context 取 conversationId */
    private String extractConversationId(ChatClientRequest request) {
        Object id = request.context().get(ChatMemory.CONVERSATION_ID);
        return id == null ? "unknown" : id.toString();
    }

    /** 打请求日志 */
    private void logRequest(String convId, ChatClientRequest request) {
        String userText = request.prompt().getUserMessage().getText();
        log.info("[AI-请求] convId={} | user={}", convId, abbreviate(userText));
    }

    /** 打响应日志 */
    private void logResponse(String convId, ChatClientResponse response, long costMs) {
        try {
            ChatResponse chatResponse = response.chatResponse();
            if (chatResponse == null || chatResponse.getResult() == null) {
                log.warn("[AI-响应] convId={} 耗时={}ms | 响应为空", convId, costMs);
                return;
            }
            String text = chatResponse.getResult().getOutput().getText();
            log.info("[AI-响应] convId={} 耗时={}ms | assistant={}",
                    convId, costMs, abbreviate(text));
        } catch (Exception e) {
            log.warn("[AI-响应] convId={} 耗时={}ms | 解析失败: {}",
                    convId, costMs, e.getMessage());
        }
    }

    /** 截断过长内容 */
    private String abbreviate(String text) {
        if (text == null) return "null";
        String oneLine = text.replaceAll("\\s+", " ").trim();
        if (oneLine.length() <= MAX_LOG_LENGTH) {
            return oneLine;
        }
        return oneLine.substring(0, MAX_LOG_LENGTH) + "...(共" + oneLine.length() + "字)";
    }
}