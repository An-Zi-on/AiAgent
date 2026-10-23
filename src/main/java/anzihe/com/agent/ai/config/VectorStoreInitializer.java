package anzihe.com.agent.ai.config;

import anzihe.com.agent.ai.reader.markdown.MarkdownDocumentReader;
import anzihe.com.agent.utils.FileUtils;
import cn.hutool.core.io.FileUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/**
 * 本地向量存储初始化
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VectorStoreInitializer implements ApplicationRunner {

    private final SimpleVectorStore vectorStore;
    private final MarkdownDocumentReader markdownReader;
    private final KeywordMetadataEnricher enricher;
    @Override
    public void run(@NonNull ApplicationArguments args) throws Exception {
        String current = FileUtils.getCurrentBasePath();
        File vector = new File(new File(current, "embedding"), "vector_store.json");
        if (FileUtil.exist(vector)) {
            return;
        }
        log.warn("向量文件不存在,开始文本加载 文件地址为:{}",vector.getCanonicalPath());
        try {
            List<Document> documents = markdownReader.loadedDocument();
            List<Document> apply = enricher.apply(documents);
            vectorStore.add(apply);
            vectorStore.save(vector);
            log.info("向量文件加载存储成功");
        }catch (Exception e){
            log.error(e.getMessage(),"知识库加载失败,知识库为空");
        }

    }
}
