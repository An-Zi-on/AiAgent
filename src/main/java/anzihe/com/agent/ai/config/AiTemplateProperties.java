package anzihe.com.agent.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Data
@Component
@ConfigurationProperties(prefix = "ai")
public class AiTemplateProperties {
    /**
     * 系统基本提示词路劲
     */
    public String systemBasePropertiesLocation;

    /**
     * yml 文件加载
     */
    public HashMap<String, AiScenes> scenes = new HashMap<>();

    @Data
    public static class AiScenes {
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
