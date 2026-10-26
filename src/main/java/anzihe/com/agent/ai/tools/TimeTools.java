package anzihe.com.agent.ai.tools;


import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;


@Component
/**
 * 联网搜索
 */
public class TimeTools{

    @Tool
    public  String getCurrentUserTime(HttpServletRequest request){
        return  "";
    }

}