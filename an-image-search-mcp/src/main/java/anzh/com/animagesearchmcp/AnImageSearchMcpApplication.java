package anzh.com.animagesearchmcp;

import anzh.com.animagesearchmcp.tools.ImageSearchTool;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class AnImageSearchMcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnImageSearchMcpApplication.class, args);
    }


    @Bean
    public ToolCallbackProvider imgSearchTools(ImageSearchTool imageSearchTool){
        return MethodToolCallbackProvider.builder().toolObjects(imageSearchTool).build();
    }

}
