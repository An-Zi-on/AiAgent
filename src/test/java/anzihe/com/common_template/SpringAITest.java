package anzihe.com.common_template;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

@SpringBootTest
public class SpringAITest {


    /**
     * 项目通过 spring.ai.openai.* 对接 DeepSeek 的 OpenAI 兼容接口，
     * 因此这里注入的是 OpenAiChatModel（原先的 DashScopeChatModel 属于
     * spring-ai-alibaba，本项目并未引入该依赖）。
     */
    @Resource
    private OpenAiChatModel chatModel;

    @Test
    public void testChat() {
        // 同步调用
        String response = chatModel.call("你好，请介绍一下你自己");
        System.out.println(response);

        // 流式调用
        Flux<ChatResponse> stream = chatModel.stream(
            new Prompt("用一句话介绍 Spring AI")
        );
        // blockLast 让流式输出在测试结束前完成
        stream.doOnNext(chunk ->
            System.out.print(chunk.getResult().getOutput().getText())
        ).blockLast();
    }

}
