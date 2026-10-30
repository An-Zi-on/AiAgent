package anzihe.com.agent.ai.tools;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class FetchUrlTools {
    private  final ObjectMapper mapper = new ObjectMapper();

    private  static final OkHttpClient HTTP_CLIENT  = new OkHttpClient.Builder()
            .connectTimeout(5,TimeUnit.SECONDS)
            .readTimeout(10,TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .followRedirects(true)
            .build();

    @Value("${an.search.api-key}")
    private  String apiKey = "";

    /**
     * 访问网址
     * @param url
     * @return
     */
    @Tool(name = "fetch-url",
            description = "Fetch and read the content of a specific web page by its URL. " +
                    "Use this tool when the user provides a URL and asks you to read, summarize, " +
                    "or answer questions based on that page's content. " +
                    "Returns the plain text extracted from the page.")
    public String fetchUrl(
            @ToolParam(required = true, description = "The full URL of the web page to read, e.g. 'https://example.com/article'")
            String url) {
        try {
            Request req = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 ...")
                    .url(url)
                    .build();
            Response resp = HTTP_CLIENT.newCall(req).execute();
            if (!resp.isSuccessful()) {
                log.error("{}code{}", resp.message(), resp.code());
                throw  new IOException("Http"+resp.code());
            }
            if (resp.body() == null){
                throw  new IOException("body is null");
            }
            String html = resp.body().toString();
            Document doc = Jsoup.parse(html, url);
            doc.select("script, style, nav, footer, header").remove();
            String text = doc.body().text();
            return text.length() > 8000 ? text.substring(0, 8000) + "...(truncated)" : text;
        } catch (IOException e) {
            return "Failed to fetch URL: " + e.getMessage();
        }
    }

    /**
     * 联网搜索
     * @param query
     * @return
     */
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
        HttpClient httpClient = HttpClient.newBuilder()
                //设置链接时间
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        //设置读取时间
        requestFactory.setReadTimeout(5);
        // 调用 SearchApi.io 接口，返回 JSON 字符串
        RestClient restClient = RestClient.builder()
                .requestFactory(requestFactory).build();
        String response = restClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("www.searchapi.io")
                        .path("/api/v1/search")
                        .queryParam("engine", "google")
                        .queryParam("q", query)
                        .queryParam("api_key", apiKey)
                        .build()
                )
                .retrieve()
                .body(String.class);

        // 用 Jackson 解析 JSON，提取 organic_results
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

    /**
     * 自行实现的jsoup爬取网页内容
     */
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
