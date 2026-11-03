package anzihe.com.agent.ai.controller;

import anzihe.com.agent.ai.service.AiChatFacade;
import anzihe.com.agent.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class AiController {
    @Resource
    private AiChatFacade aiChatFacade;
    @Resource
    private UserService userService;

    @GetMapping("/questsion")
    public String talk(@RequestParam("message" ) String message,HttpServletRequest request){
        return aiChatFacade.doChat(message,request);
    }

    @GetMapping( path = "/question/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> talkStream(@RequestParam("message" ) String message, HttpServletRequest request){
        return aiChatFacade.doChatStream(message,request);
    }
}
