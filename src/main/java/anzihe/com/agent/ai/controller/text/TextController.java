package anzihe.com.agent.ai.controller.text;

import anzihe.com.agent.ai.app.App;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TextController {
    @Resource
    private App app;

    @GetMapping("/text")
    public  String text(@RequestParam("message") String message){
        return app.doChat(message, "1234");
    }

}
