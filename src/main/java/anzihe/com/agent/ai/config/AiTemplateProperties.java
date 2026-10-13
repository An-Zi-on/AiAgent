package anzihe.com.agent.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("ai")
public class AiTemplateProperties {
    /**
     * 系统基本提示词路劲
     */
    public String systemBasePropertiesLocation;

    @Data
    public  static class scenes{
        /**
         * 场景系统提示词路径
         */
        private  String systemPromptLocation;

        /**
         * 用户提示词模版
         */
        private  String userTemplate;
    }
}
