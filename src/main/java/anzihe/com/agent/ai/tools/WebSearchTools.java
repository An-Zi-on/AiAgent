package anzihe.com.agent.ai.tools;

import ai.z.openapi.ZhipuAiClient;
import ai.z.openapi.service.web_search.WebSearchRequest;
import ai.z.openapi.service.web_search.WebSearchResponse;
import ai.z.openapi.service.web_search.WebSearchService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
/**
 * 联网搜索
 */
public class WebSearchTools{

    private final WebSearchService webSearchService;

    public WebSearchTools() {
        ZhipuAiClient client = ZhipuAiClient.builder().ofZHIPU().build();
        this.webSearchService = client.webSearch();
    }

    @Tool(name = "web-search",
            description = "Search the web for real-time information. " +
                    "Use this tool when the user asks about current events, recent news, " +
                    "live data, or any question that requires up-to-date " +
                    "information beyond the model's knowledge cutoff. " +
                    "Returns a list of relevant search results with titles and snippets.")
    public String search(
            @ToolParam(required = true,
                    description = "The search query text, e.g. 'latest AI news 2025'. " +
                            "Use concise keywords or a natural-language question.")
            String query) {

        WebSearchRequest request = WebSearchRequest.builder()
                .searchEngine("search_pro")
                .searchQuery(query)
                .count(5)
                .contentSize("medium")
                .searchRecencyFilter("noLimit")
                .build();

        WebSearchResponse response = webSearchService.createWebSearch(request);
        return response.toString();
    }
}