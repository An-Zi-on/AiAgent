package anzihe.com.agent.ai.reader.markdown;


import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MarkdownDocumentReader {

    private final ResourcePatternResolver resolver;


    public MarkdownDocumentReader(ResourcePatternResolver resolver){
        this.resolver = resolver;
    }

    public List<Document> loadedDocument(){
        List<Document> documentList = new ArrayList<>();
        try {
            Resource[] resources = resolver.getResources("classpath*:document/*.md");
            for (Resource resource : resources){
                MarkdownDocumentReaderConfig config  = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", "code.md")
                        .build();
                org.springframework.ai.reader.markdown.MarkdownDocumentReader documentReader  = new org.springframework.ai.reader.markdown.MarkdownDocumentReader(resource,config);
                documentList.addAll(documentReader.get());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return documentList;
    }
}
