package anzihe.com.agent.ai.controller;

import anzihe.com.agent.ai.service.AiChatFacade;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {
    @Resource
    private AiChatFacade aiChatFacade;

    @GetMapping("/questsion")
    public String talk(@RequestParam("message") String message){
        return aiChatFacade.doChat(message, "4564554");
    }
}
