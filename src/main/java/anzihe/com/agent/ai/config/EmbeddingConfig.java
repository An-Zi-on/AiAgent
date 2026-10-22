package anzihe.com.agent.ai.config;


import com.knuddels.jtokkit.api.EncodingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class EmbeddingConfig {

    @Bean
    public BatchingStrategy customBatchingStrategy(){
        log.info("开始配置TokenCountBatchingStrategy配置");
        return new  TokenCountBatchingStrategy(
                EncodingType.CL100K_BASE, // 编码类型
                8000,    //最大token数
                0.1   // 预留 10% 缓冲
        );
    }
}
