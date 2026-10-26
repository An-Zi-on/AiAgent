package anzihe.com.agent.ai.config;

import anzihe.com.agent.ai.tools.FetchUrlTools;
import anzihe.com.agent.ai.tools.FileStorageTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolRegistrationConfig {
        private  final FetchUrlTools fetchUrlTools;
        private  final FileStorageTools fileStorageTools;
        public ToolRegistrationConfig( FetchUrlTools fetchUrlTools ,
                                       FileStorageTools fileStorageTools
                                       ){
            this.fetchUrlTools = fetchUrlTools;
            this.fileStorageTools = fileStorageTools;
        }
        @Bean
        public ToolCallback[] callbacks(){

            return ToolCallbacks.from(
                    this.fetchUrlTools,
                    this.fileStorageTools
                    );
        }

}
