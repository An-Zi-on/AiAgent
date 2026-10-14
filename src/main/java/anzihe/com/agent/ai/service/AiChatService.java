package anzihe.com.agent.ai.service;

import anzihe.com.agent.ai.advisor.MyLoggerAdvisor;
import anzihe.com.agent.ai.config.AiTemplateProperties;
import anzihe.com.agent.ai.entry.template.Scenes;
import anzihe.com.agent.ai.template.AiTemplateLoader;
import anzihe.com.agent.ai.template.AiTemplateRegister;
import anzihe.com.agent.ai.template.PromoteTemplateRenderer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class AiChatService {
    private final ChatModel chatModel;
    private final AiTemplateRegister templateRegister;
    private final PromoteTemplateRenderer templateRenderer;
    private final AiTemplateProperties templateProperties;
    private final AiTemplateLoader promptLoader;
    private final ChatMemory chatMemory;

    public AiChatService(
            ChatModel chatModel,
            AiTemplateProperties templateProperties,
            AiTemplateRegister templateRegister,
            AiTemplateLoader promptLoader,
            PromoteTemplateRenderer templateRenderer,
            ChatMemory chatMemory) {
        this.chatModel = chatModel;
        this.templateProperties = templateProperties;
        this.templateRegister = templateRegister;
        this.promptLoader = promptLoader;
        this.templateRenderer = templateRenderer;
        this.chatMemory = chatMemory;
    }

    public  String chat(String message,String conversationId){
        return ChatClient.builder(chatModel).build()
                .prompt().user(message)
                //提供一个默认顾问在 对话时找到时机 使用和存储对话
                // ChatMemory 只提供实现的方式,具体执行的时机由这个MessageChatMemoryAdvisors 进行操作
                //a.param(ChatMemory.CONVERSATION_ID, conversationId) 则是对哪一组会话进行操作
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();
    }

    public String doScene(String scene, Map<String, Object> var, String conversationId) {
        Scenes scenes = templateRegister.get(scene);
        //用户提示词渲染
        String userPromo = templateRenderer.userPrompt(scenes.promptTemplate(), var);
        return chunk(scenes)
                .prompt()
                .user(userPromo)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call().content();
    }


    private ChatClient chunk(Scenes scenes) {
        //加载系统基础提示词
        String base = promptLoader.load(templateProperties.getSystemBasePropertiesLocation());
        log.info("加载系统基础提示词文件路径为{}", templateProperties.getSystemBasePropertiesLocation());
        //补充场景提示词
        String scenePrompt = scenes.systemPromote();
        log.info("加载场景提示词");
        String finalSystem = (base == null ? "" : base)
                + "\n\n"
                + (scenePrompt == null ? "" : scenePrompt);

        return ChatClient
                .builder(chatModel)
                .defaultSystem(finalSystem)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build() ,new MyLoggerAdvisor())
                .build();
    }


}
