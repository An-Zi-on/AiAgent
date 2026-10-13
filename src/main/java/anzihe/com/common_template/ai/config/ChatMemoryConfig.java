package anzihe.com.common_template.ai.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatMemoryConfig {

    @Bean
    //ChatMemory 是一个带有具体存储窗口大小,并绑定了具体存储实现过程的对象
    public ChatMemory chatMemory (JdbcChatMemoryRepository jdbcChatMemoryRepository){
        //MessageWindowChatMemory  只关心 存储窗口的大小 和 具体使用哪种实现存储过的类型
        return MessageWindowChatMemory
                .builder()
                .maxMessages(20)
                //实际实现真正在存储过程的为 实现ChatMemoryRepository  接口的 具体类
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .build();
    }

}
