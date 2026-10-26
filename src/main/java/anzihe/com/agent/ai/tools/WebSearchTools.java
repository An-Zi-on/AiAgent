package anzihe.com.agent.ai.tools;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.web_search.WebSearchRequest;
import ai.z.openapi.service.web_search.WebSearchResponse;
import ai.z.openapi.service.web_search.WebSearchService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
/**
 * 联网搜索
 */
public class WebSearchTools{

    private final RestClient restClient = RestClient.create();

    @Tool(name = "web-search",
            description = "Search the web for real-time information using SearchApi.io (Google engine). " +
                    "Use this tool when the user asks about current events, recent news, live data, " +
                    "or any question that requires up-to-date information beyond the model's knowledge cutoff. " +
                    "Returns a list of organic search results, each containing a title, a URL, and a snippet. " +
                    "Prefer calling this tool instead of answering from memory when the question implies freshness.")
    public List<Map<String, String>> searchBySelf(
            @ToolParam(description = "The search query text, e.g. 'latest AI news 2025' or 'Spring Boot 4 release notes'. " +
                            "Use concise keywords or a natural-language question. " +
                            "Do not include quotes, operators, or extra formatting unless the user explicitly asks for them.") String query) {

        String apiKey = "MxFUqfEBFHhG4DhtD6XbcYS5"; // 建议放到配置/环境变量里

        // 调用 SearchApi.io 接口，返回 JSON 字符串
        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.searchapi.io")
                        .path("/api/v1/search")
                        .queryParam("engine", "google")
                        .queryParam("q", query)
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .body(String.class);

        // 用 Jackson 解析 JSON，提取 organic_results
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(response);
        JsonNode organicResults = root.path("organic_results");

        List<Map<String, String>> results = new ArrayList<>();
        for (JsonNode item : organicResults) {
            Map<String, String> result = new HashMap<>();
            result.put("title", item.path("title").asText());
            result.put("url", item.path("link").asText());
            result.put("snippet", item.path("snippet").asText());
            results.add(result);
        }
        return results;
    }

//    @Tool(name = "web-search",
//            description = "Search the web for real-time information. " +
//                    "Use this tool when the user asks about current events, recent news, " +
//                    "live data, or any question that requires up-to-date " +
//                    "information beyond the model's knowledge cutoff. " +
//                    "Returns a list of relevant search results with titles and snippets.")
//    public List<Map<String, String>> searchBySelf(
//            @ToolParam(required = true,
//                    description = "The search query text, e.g. 'latest AI news 2025'. " +
//                            "Use concise keywords or a natural-language question.")
//            String query) {
//        List<Map<String, String>> results = new ArrayList<>();
//        String url = String.format("https://cn.bing.com/search?q=%s",query);
//        try {
//            Document doc = Jsoup.connect(url)
//                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
//                    .get();
//
//            Elements resultItems = doc.select("li.b_algo");
//
//            for (Element item : resultItems) {
//                Map<String, String> result = new HashMap<>();
//                Element titleElement = item.selectFirst("h2 a");
//                if (titleElement != null) {
//                    result.put("title", titleElement.text());
//                    result.put("url", titleElement.attr("href"));
//                }
//                Element snippetElement = item.selectFirst(".b_caption p");
//                if (snippetElement != null) {
//                    result.put("snippet", snippetElement.text());
//                }
//                if (!result.isEmpty()) {
//                    results.add(result);
//                }
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        return results;
//    }
//
}