package anzihe.com.agent.ai.controller;

import anzihe.com.agent.ai.app.App;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AiController {
    @Resource
    private App app;

    @GetMapping("/questsion")
    public String talk(@RequestParam("message") String message){
        return app.doChat(message, "4564554");
    }
}
