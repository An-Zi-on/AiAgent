package anzihe.com.agent.ai.tools;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FetchUrlTools {

    @Tool(name = "fetch-url",
            description = "Fetch and read the content of a specific web page by its URL. " +
                    "Use this tool when the user provides a URL and asks you to read, summarize, " +
                    "or answer questions based on that page's content. " +
                    "Returns the plain text extracted from the page.")
    public String fetchUrl(
            @ToolParam(required = true, description = "The full URL of the web page to read, e.g. 'https://example.com/article'")
            String url) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(10_000)
                    .get();
            // 移除脚本和样式，只保留正文
            doc.select("script, style, nav, footer, header").remove();
            String text = doc.body().text();
            // 截断过长内容，避免超出模型上下文
            return text.length() > 8000 ? text.substring(0, 8000) + "...(truncated)" : text;
        } catch (IOException e) {
            return "Failed to fetch URL: " + e.getMessage();
        }
    }
}
