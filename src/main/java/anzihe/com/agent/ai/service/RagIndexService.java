package anzihe.com.agent.ai.service;

import anzihe.com.agent.ai.reader.markdown.MarkdownDocumentReader;
import cn.hutool.core.io.FileUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 知识库索引服务。
 *
 * <p>把「读 markdown -> 补关键词 -> 向量化 -> 落盘」这段耗时逻辑从启动流程里抽出来，
 * 启动只负责加载已有向量文件，索引既可以启动后自动在后台跑，也可以通过
 * {@code POST /api/rag/reindex} 手动触发。</p>
 */
@Slf4j
@Service
public class RagIndexService {

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SimpleVectorStore vectorStore;
    private final MarkdownDocumentReader markdownReader;
    private final KeywordMetadataEnricher keywordEnricher;
    private final Path vectorFile;
    /** 伴随 vector_store.json 的小元数据文件，重启后照样能报告文档数 / 上次建索引时间。 */
    private final Path metaFile;
    /** 是否在向量化前逐篇调大模型补关键词；关掉可把建索引从十几分钟降到几十秒。 */
    private final boolean enrichKeywords;

    /** 防止并发重复建索引（启动自动跑 + 手动触发可能撞车）。 */
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Getter
    private volatile boolean ready;
    @Getter
    private volatile int docCount;
    @Getter
    private volatile long costMs;
    @Getter
    private volatile String lastError;
    @Getter
    private volatile String lastFinishedAt;

    public RagIndexService(SimpleVectorStore vectorStore,
                           MarkdownDocumentReader markdownReader,
                           KeywordMetadataEnricher keywordMetadataEnricher,
                           @Value("${app.rag.store-dir:embedding}") String storeDir,
                           @Value("${app.rag.enrich-keywords:true}") boolean enrichKeywords) {
        this.vectorStore = vectorStore;
        this.markdownReader = markdownReader;
        this.keywordEnricher = keywordMetadataEnricher;
        this.vectorFile = Path.of(storeDir).toAbsolutePath().resolve("vector_store.json");
        this.metaFile = vectorFile.resolveSibling("vector_store.meta.json");
        this.enrichKeywords = enrichKeywords;
        this.ready = Files.exists(vectorFile);
        if (ready) {
            loadMeta();
        }
    }

    private void loadMeta() {
        try {
            if (Files.exists(metaFile)) {
                JSONObject meta = JSONUtil.readJSONObject(metaFile.toFile(), StandardCharsets.UTF_8);
                docCount = meta.getInt("docCount", 0);
                costMs = meta.getLong("costMs", 0L);
                lastFinishedAt = meta.getStr("lastFinishedAt");
                log.info("读取索引元数据：{} 个文档，上次完成于 {}", docCount, lastFinishedAt);
            }
        } catch (Exception e) {
            log.warn("索引元数据读取失败（不影响使用）：{}", e.getMessage());
        }
    }

    private void saveMeta() {
        try {
            JSONObject meta = new JSONObject();
            meta.set("docCount", docCount);
            meta.set("costMs", costMs);
            meta.set("lastFinishedAt", lastFinishedAt);
            FileUtil.writeUtf8String(meta.toStringPretty(), metaFile.toFile());
        } catch (Exception e) {
            log.warn("索引元数据写入失败（不影响使用）：{}", e.getMessage());
        }
    }

    /** 异步提交一次建索引，立即返回，不阻塞调用方。 */
    public void reindexAsync() {
        CompletableFuture.runAsync(this::reindex);
    }

    /**
     * 同步建索引。
     *
     * @return true 表示本次建索引成功；false 表示已在执行 / 没有文档 / 失败
     */
    public boolean reindex() {
        if (!running.compareAndSet(false, true)) {
            log.warn("索引任务已在执行中，忽略本次触发");
            return false;
        }
        long start = System.currentTimeMillis();
        try {
            List<Document> documents = markdownReader.loadedDocument();
            if (documents.isEmpty()) {
                lastError = "没有读取到任何 markdown 文档，请检查 src/main/resources/document/";
                log.warn(lastError);
                return false;
            }
            log.info("开始建索引：{} 个文档，关键词增强={}", documents.size(), enrichKeywords);

            List<Document> enriched = enrichKeywords ? keywordEnricher.apply(documents) : documents;
            List<Document> prepared = enriched.stream()
                    .map(this::withStableId)
                    .toList();

            vectorStore.add(prepared);
            Files.createDirectories(vectorFile.getParent());
            vectorStore.save(vectorFile.toFile());

            docCount = prepared.size();
            ready = true;
            lastError = null;
            lastFinishedAt = LocalDateTime.now().format(TIME_FORMAT);
            costMs = System.currentTimeMillis() - start;
            saveMeta();
            log.info("索引完成：{} 个文档，耗时 {} ms，已写入 {}", docCount, costMs, vectorFile);
            return true;
        } catch (Exception e) {
            costMs = System.currentTimeMillis() - start;
            lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
            log.error("建索引失败（耗时 {} ms）", costMs, e);
            return false;
        } finally {
            running.set(false);
        }
    }

    /**
     * 用内容哈希当 id：重复建索引时是覆盖而不是无限追加。
     */
    private Document withStableId(Document document) {
        String text = document.isText() ? document.getText() : document.getId();
        return document.mutate().id(DigestUtil.sha256Hex(text)).build();
    }

    public boolean isRunning() {
        return running.get();
    }

    public File getVectorFile() {
        return vectorFile.toFile();
    }
}
