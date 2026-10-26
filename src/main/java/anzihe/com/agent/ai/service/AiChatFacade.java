package anzihe.com.agent.ai.service;

import anzihe.com.agent.ai.model.MovieInfo;
import anzihe.com.agent.ai.model.MovieInfoEntityResult;
import anzihe.com.agent.model.VO.user.UserVO;
import anzihe.com.agent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class AiChatFacade {
    private  final AiChatService aiChatService;

    @Resource
    private UserService userService;
    public  AiChatFacade(AiChatService aiChatService){

        this.aiChatService = aiChatService;

    }

    public  String doChat(String message, HttpServletRequest request) {
        UserVO userVO = userService.currentUser(request);
        return aiChatService.chat(message, String.valueOf(userVO.getId()));
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
//    public MovieInfoEntityResult doChatMoviesRespond(String message, String userId) {
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
//        return new MovieInfoEntityResult(totalTokens,promptTokens,completionTokens,completionTokens,responseEntity.getEntity());
//    }2
}
