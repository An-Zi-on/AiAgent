package anzihe.com.agent.ai.app;

import anzihe.com.agent.ai.entry.text.MoveInfoEntityResult;
import anzihe.com.agent.ai.entry.text.MovieInfo;
import anzihe.com.agent.ai.service.AiChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class App {
    private  final AiChatService aiChatService;

    public  App(AiChatService aiChatService){

        this.aiChatService = aiChatService;

    }

    public  String doChat(String message,String userId) {
        return aiChatService.chat(message, userId);
    }

//    public String doScene(String conversionId, String question) {
//        return aiChatService.doScene("knowledge", Map.of("question", question), conversionId);
//    }
//
//    public  MovieInfo doChatMovie(String message,String userId){
//        return aiChatService.chatMovie(message, userId);
//    }
//    public List<MovieInfo> doChatMovies(String message, String userId){
//        return aiChatService.chatMovies(message, userId);
//    }
//
//    public MoveInfoEntityResult doChatMoviesRespond(String message, String userId) {
//        ResponseEntity<ChatResponse, MovieInfo> responseEntity = aiChatService.chatGetRespond(message, userId);
//        //包含响应的元素数据
//        ChatResponseMetadata metadata = responseEntity.getResponse().getMetadata();
//        //模型名
//        String model =metadata.getModel();
//        Usage usage = metadata.getUsage();
//        //总共的token
//        Integer totalTokens = usage.getTotalTokens();
//        //提示词的token数
//        Integer promptTokens = usage.getPromptTokens();
//        //完成响应的token数
//        Integer completionTokens = usage.getCompletionTokens();
//        //结束原因
//         String finishReason = responseEntity.getResponse().getResult().getMetadata().getFinishReason();
//         if (finishReason.equals("length")){
//             log.warn(responseEntity.getResponse().getResult().getOutput().getText());
//         }
//        //响应原始文本
//        String text = responseEntity.getResponse().getResult().getOutput().getText();
//        MovieInfo entity = responseEntity.getEntity();
//        log.info("模型名: {}", model);
//        log.info("Token 用量 -> prompt: {}, completion: {}, total: {}",
//                promptTokens, completionTokens, totalTokens);
//        log.info("结束原因: {}", finishReason);
//        log.info("解析结果 MovieInfo: {}", entity);
//        log.info("原始响应文本: {}", text);
//        return new MoveInfoEntityResult(totalTokens,promptTokens,completionTokens,completionTokens,responseEntity.getEntity());
//    }2
}
