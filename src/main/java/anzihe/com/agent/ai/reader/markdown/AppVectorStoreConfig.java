package anzihe.com.agent.ai.reader.markdown;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.zhipuai.ZhiPuAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
@Configuration
public class AppVectorStoreConfig {
    private  final MarkdownDocumentReader markdownReader ;

    public  AppVectorStoreConfig(MarkdownDocumentReader markdownReader){
        this.markdownReader = markdownReader;
    }

    @Bean
    public SimpleVectorStore store(ZhiPuAiEmbeddingModel embeddingModel){
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        List<Document> documents = markdownReader.loadedDocument();
        vectorStore.add(documents);
        return vectorStore;
    }
}
