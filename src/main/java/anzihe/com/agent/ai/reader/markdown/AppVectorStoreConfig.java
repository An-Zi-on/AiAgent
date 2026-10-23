package anzihe.com.agent.ai.reader.markdown;

import anzihe.com.agent.utils.FileUtils;
import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class AppVectorStoreConfig {

    private final MarkdownDocumentReader markdownReader;

    public AppVectorStoreConfig(MarkdownDocumentReader markdownReader) {
        this.markdownReader = markdownReader;
    }

    /**
     * 本地向量存储初始化
     * @param embeddingModel 向量大模型
     * @param batchingStrategy 批量存储策略
     * @return
     */
    @Bean
    public SimpleVectorStore store(EmbeddingModel embeddingModel,
                                   BatchingStrategy batchingStrategy
                                 ) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .batchingStrategy(batchingStrategy).build();
        String currentBasePath = FileUtils.getCurrentBasePath();
        File dir = new File(currentBasePath,"embedding");
        if (!FileUtil.exist(dir)){
            //不存在则创建文件夹
                dir.mkdirs();
        }
        //检查是否存储了向量文件
        File vector = new File(dir,"vector_store.json");
        if (FileUtil.exist(vector)){
            //存在读取
            log.info("资源存在,加载向量文件");
            vectorStore.load(vector);
            log.info("向量文件加载成功");
            return vectorStore;
        }
        return vectorStore;
    }
}