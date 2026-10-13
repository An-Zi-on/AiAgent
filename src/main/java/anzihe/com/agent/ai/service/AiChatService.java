package anzihe.com.agent.ai.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {
    private final ChatModel chatModel;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;

    public  AiChatService(ChatModel chatModel,ChatMemory chatMemory){
        this.chatModel = chatModel;
        this.chatMemory = chatMemory;
        this.chatClient = ChatClient
                .builder(this.chatModel)
                    .defaultAdvisors(
                            MessageChatMemoryAdvisor.builder(chatMemory).build(),
                            new SimpleLoggerAdvisor()
                    )
                .build();
    }

    public  String chat(String message,String conversationId){
        return this.chatClient
                .prompt().user(message)
                //提供一个默认顾问在 对话时找到时机 使用和存储对话
                // ChatMemory 只提供实现的方式,具体执行的时机由这个MessageChatMemoryAdvisors 进行操作
                //a.param(ChatMemory.CONVERSATION_ID, conversationId) 则是对哪一组会话进行操作
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }




}
