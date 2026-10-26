package anzihe.com.agent.ai.service;

import anzihe.com.agent.ai.config.AiTemplateProperties;
import anzihe.com.agent.ai.config.ToolRegistrationConfig;
import anzihe.com.agent.ai.template.AiTemplateLoader;
import anzihe.com.agent.ai.template.AiTemplateRegister;
import anzihe.com.agent.ai.template.PromptTemplateRenderer;
import anzihe.com.agent.ai.tools.FetchUrlTools;
import anzihe.com.agent.utils.TimeUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Slf4j
@Service
public class AiChatService {
    private final ChatModel chatModel;
    private final AiTemplateRegister templateRegister;
    private final AiTemplateProperties templateProperties;
    private final AiTemplateLoader promptLoader;
    private final ChatMemory chatMemory;
    private  final VectorStore vectorStore;
    private final ChatClient chatClient  ;
    private final ToolRegistrationConfig toolRegistration;

    @Autowired
    private FetchUrlTools fetchUrlTools;

    /**
     * 初始化依赖
     * 配置默认的ChatClient  tools advisor system提示词
     * @param chatModel
     * @param templateProperties
     * @param templateRegister
     * @param promptLoader
     * @param templateRenderer
     * @param chatMemory
     * @param vectorStore
     */
    public AiChatService(
            @Qualifier("openAiChatModel") ChatModel chatModel,
            AiTemplateProperties templateProperties,
            AiTemplateRegister templateRegister,
            AiTemplateLoader promptLoader,
            PromptTemplateRenderer templateRenderer,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            ToolRegistrationConfig toolRegistration) {
        this.chatModel = chatModel;
        this.templateProperties = templateProperties;
        this.templateRegister = templateRegister;
        this.promptLoader = promptLoader;
        this.chatMemory = chatMemory;
        this.vectorStore =vectorStore;
        this.toolRegistration = toolRegistration;
        this.chatClient = ChatClient
                .builder(this.chatModel)
                .defaultSystem(String.format("Current time is %s", TimeUtils.getCurrentTime()))
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools((Object) this.toolRegistration.callbacks())
                .build();
    }

    public String chat(String message, String conversationId) {
//        RetrievalAugmentationAdvisor ragAdvisor = RetrievalAugmentationAdvisor.builder()
//                .documentRetriever(VectorStoreDocumentRetriever.builder()
//                        .vectorStore(vectorStore).build())
//                .build();
        return this.chatClient
                .prompt()
                .user(message)
                .advisors()
                //提供一个默认顾问在 对话时找到时机 使用和存储对话
                // ChatMemory 只提供实现的方式,具体执行的时机由这个MessageChatMemoryAdvisors 进行操作
                //a.param(ChatMemory.CONVERSATION_ID, conversationId) 则是对哪一组会话进行操作
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools()
                //开启RAG功能
//                .advisors(QuestionAnswerAdvisor.builder(this.vectorStore).build(),ragAdvisor)
                .call()
                .content();
    }
    public Flux<String> chatStream(String message, String conversationId) {
        return this.chatClient
                .prompt()
                .user(message)
                .advisors()
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .tools()
                .stream()
                .content();
    }

    /**
     * Rag 功能
     */
//
//    public String doScene(String scene, Map<String, Object> var, String conversationId) {
//        Scenes scenes = templateRegister.get(scene);
//        String resultFormat = scenes.resultFormat();
//        BeanOutputConverter<MovieInfo> converter = new BeanOutputConverter<>(MovieInfo.class);
//        String format = converter.getFormat();
//        //用户提示词渲染
//        String userPromo = templateRenderer
//                .userPrompt(scenes.promptTemplate(), var);
//        String formatString = templateRenderer.resultFormat(resultFormat, Map.of("format", format));
//        String userContent = userPromo + "\n\n---\n\n" + formatString;
//
//        ChatClient.CallResponseSpec call = chunk(scenes)
//                .prompt()
//                .user(userContent)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call();
//        ChatResponse chatResponse = call.chatResponse();
//        String text = chatResponse.getResult().getOutput().getText();
//        log.error(text);
//        converter.convert(text);
//        return call.content();
//    }
//
//
//    private ChatClient chunk(Scenes scenes) {
//        //加载系统基础提示词
//        String base = promptLoader.load(templateProperties.getSystemBasePropertiesLocation());
//        log.info("加载系统基础提示词文件路径为{}", templateProperties.getSystemBasePropertiesLocation());
//        //补充场景提示词
//        String scenePrompt = scenes.systemPromote();
//        log.info("加载场景提示词");
//        String finalSystem = (base == null ? "" : base)
//                + "\n\n"
//                + (scenePrompt == null ? "" : scenePrompt);
//
//        return ChatClient
//                .builder(chatModel)
//                .defaultSystem(finalSystem)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(),
//                        new MyLoggerAdvisor(),
//                        new org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor())
//                .build();
//    }
//
//    /**
//     * 使用结构化输出  entity() 方法
//     *
//     * @param message
//     * @param conversationId
//     * @return
//     */
//    public MovieInfo chatMovie(String message, String conversationId) {
//        return ChatClient.builder(chatModel)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build()
//                .prompt().user(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .entity(MovieInfo.class, ChatClient.EntityParamSpec::useProviderStructuredOutput);
//    }
//
//    /**
//     * 结构化输出泛型对象使用entity()方法
//     *
//     * @param message
//     * @param conversationId
//     * @return
//     */
//    public List<MovieInfo> chatMovies(String message, String conversationId) {
//        return ChatClient.builder(chatModel)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build()
//                .prompt().user(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .entity(new ParameterizedTypeReference<>() {
//                });
//    }
//
//    /**
//     *
//     * @param message
//     * @param conversationId
//     * @return
//     */
//    public ResponseEntity<ChatResponse, MovieInfo> chatGetRespond(String message, String conversationId) {
//        return ChatClient.builder(chatModel)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build()
//                .prompt().user(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .responseEntity(MovieInfo.class);
//    }
//
//    /**
//     *
//     * @param message
//     * @param conversationId
//     * @return
//     */
//    public ResponseEntity<ChatResponse, MovieInfo> chatGetRespond1(String message, String conversationId) {
//        return ChatClient.builder(chatModel)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
//                .build()
//                .prompt().user(message)
//                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
//                .call()
//                .responseEntity(new BeanOutputConverter<>(MovieInfo.class));
//    }
}
