package anzihe.com.common_template.ai.app;

import anzihe.com.common_template.ai.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class App {
    private  final AiChatService aiChatService;

    public  App(AiChatService aiChatService){
        this.aiChatService = aiChatService;
    }

    public  String doChat(String message,String userId){
        return aiChatService.chat(message, userId);
    }
}
