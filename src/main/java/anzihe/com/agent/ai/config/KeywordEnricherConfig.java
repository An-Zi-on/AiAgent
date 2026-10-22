package anzihe.com.agent.ai.config;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 为分割的文章补充原信息
 */
@Configuration
public class KeywordEnricherConfig {

    @Bean
    public KeywordMetadataEnricher myKeywordEnricher(ChatModel chatModel) {
        return new KeywordMetadataEnricher(chatModel, 5);
    }
}
