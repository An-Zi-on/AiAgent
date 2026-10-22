package anzihe.com.agent.ai.reader.markdown;

import anzihe.com.agent.ai.service.RagIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.BatchingStrategy;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.nio.file.Path;

@Slf4j
@Configuration
public class AppVectorStoreConfig {

    /**
     * 只负责「有文件就加载」，绝不做远程调用，保证启动不被阻塞。
     */
    @Bean
    public SimpleVectorStore store(EmbeddingModel embeddingModel,
                                   BatchingStrategy batchingStrategy,
                                   @Value("${app.rag.store-dir:embedding}") String storeDir) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel)
                .batchingStrategy(batchingStrategy).build();
        File file = Path.of(storeDir).toAbsolutePath().resolve("vector_store.json").toFile();
        if (file.exists()) {
            try {
                vectorStore.load(file);
                log.info("已加载向量文件：{}", file);
            } catch (Exception e) {
                log.error("向量文件读取失败（{}），将按空库启动并重建索引", file, e);
            }
        } else {
            log.warn("暂无向量文件（{}），先空库启动；稍后自动后台建索引，或手动 POST /api/rag/reindex", file);
        }
        return vectorStore;
    }

    /**
     * 启动后触发一次后台建索引（默认开启），失败只记日志，不影响服务可用性。
     */
    @Bean
    ApplicationRunner ragIndexRunner(RagIndexService indexService,
                                     @Value("${app.rag.index-on-startup:true}") boolean enabled) {
        return args -> {
            if (!enabled) {
                log.info("app.rag.index-on-startup=false，跳过自动建索引");
                return;
            }
            if (indexService.isReady()) {
                log.info("向量库已就绪，跳过自动建索引（需要重建请 POST /api/rag/reindex）");
                return;
            }
            log.info("向量库为空，提交后台建索引任务");
            indexService.reindexAsync();
        };
    }
}
