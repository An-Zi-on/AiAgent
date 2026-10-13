package anzihe.com.agent.ai.app;

import anzihe.com.agent.ai.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
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
