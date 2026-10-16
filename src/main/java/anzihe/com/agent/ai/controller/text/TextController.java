package anzihe.com.agent.ai.controller.text;

import anzihe.com.agent.ai.app.App;
import anzihe.com.agent.ai.entry.text.MoveInfoEntityResult;
import anzihe.com.agent.ai.entry.text.MovieInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TextController {
    @Resource
    private App app;

    @GetMapping("/text")
    public String text(@RequestParam("message") String message) {
        return app.doChat(message, "1234");
    }


    @GetMapping("/status")
    public String scene(@RequestParam("question") String question) {
        return app.doScene("123456", question);
    }

    @GetMapping("/movie")
    public MovieInfo movie(@RequestParam("message") String message) {
        return app.doChatMovie(message, "1234");
    }

    @GetMapping("/movies")
    public List<MovieInfo> movieInfos(@RequestParam("message") String message) {
        return app.doChatMovies(message, "12345");
    }

    @GetMapping("/movie/raw")
    public MoveInfoEntityResult movieRespond(@RequestParam("message") String message) {
        return app.doChatMoviesRespond(message, "12345");
    }

}
