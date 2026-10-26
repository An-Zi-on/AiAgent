package anzihe.com.agent.ai.config;

import anzihe.com.agent.ai.tools.FetchUrlTools;
import anzihe.com.agent.ai.tools.FileStorageTools;
import anzihe.com.agent.ai.tools.WebSearchTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistrationConfig {

        @Bean
        public ToolCallback[] callbacks(){
            FetchUrlTools fetchUrlTools = new FetchUrlTools();
            FileStorageTools fileStorageTools = new FileStorageTools();
            WebSearchTools webSearchTools = new WebSearchTools();
            return ToolCallbacks.from(
                    fetchUrlTools,
                    fileStorageTools,
                    webSearchTools);
        }

}
