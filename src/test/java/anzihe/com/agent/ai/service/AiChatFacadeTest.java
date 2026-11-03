package anzihe.com.agent.ai.service;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AiChatFacadeTest {

    @Resource
    AiChatFacade aiChatFacade;

    @Test
    void doChatWithMcp() {
        // 1. 构造一个具体的搜图需求
        String query = "帮我找 3 张关于小狗在草地上奔跑的照片。";

        // 2. 传入一个唯一会话 ID（用于 ChatMemory 记录上下文）
        String conversationId = "test-session-" + System.currentTimeMillis();

        System.out.println("========== 开始测试 MCP 搜图 ==========");
        System.out.println("用户提问: " + query);

        // 3. 调用 Facade 方法（假设你已经按之前的建议修改了方法签名）
        String result = aiChatFacade.doChatWithMcp(query,conversationId);

        System.out.println("========== 大模型返回 ==========");
        System.out.println(result);
    }
}