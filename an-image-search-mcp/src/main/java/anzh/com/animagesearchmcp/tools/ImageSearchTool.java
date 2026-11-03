package anzh.com.animagesearchmcp.tools;

import anzh.com.animagesearchmcp.entry.Img;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ImageSearchTool {
    private static final String BASE_IMG_SEARCH_URL = "https://api.pexafy.com";
    private final RestClient restClient;

    public ImageSearchTool(@Value("${PEXAFY_API_KEY}") String apiKey){
        restClient = RestClient.builder()
                .baseUrl(BASE_IMG_SEARCH_URL)
                .defaultHeader("x-api-key", apiKey)
                .build();
    }
    @Tool(description = "Search for high-quality, free stock photos using natural language. " +
            "Provide a detailed, descriptive sentence of the scene you want to find " +
            "(e.g., 'a serene ocean landscape at sunset' or 'a busy modern city street at night'). " +
            "Returns a list of matching images with their URLs, descriptions, and metadata.")
    public String searchImage(@ToolParam( description = "Search query skeyword") String query){
        Map body = restClient.get().uri(uriBuilder -> uriBuilder
                        .path("/api/v1/search/photos")
                        .queryParam("q", query)
                        .queryParam("per_page", 10)
                        .build()
                )
                .retrieve()
                .body(Map.class);
        List list = clearData(body);
        return JSONUtil.parse(list).toString();
    }
    public List<Img> clearData(Map<String, Object> response) {
        List<Img> imgs = new ArrayList<>();

        if (response == null || response.isEmpty() || !Boolean.TRUE.equals(response.get("success"))){
            return imgs;
        }

        Object dataObj = response.get("data");
        if (!(dataObj instanceof List)){
            return imgs;
        }

        List<Map<String, Object>> dataList = (List<Map<String, Object>>) dataObj;
        for (Map<String, Object> item : dataList) {
            // 安全获取字符串（如果是 null，Convert 会返回 null 或你指定的默认值）
            String imageUrl = Convert.toStr(item.get("image_url"));

            // 如果 imageUrl 为空，直接跳过这条脏数据
            if (StrUtil.isBlank(imageUrl)) {
                continue;
            }

            // 优先取 description，如果没有再取 alt_description
            String description = Convert.toStr(item.get("description"));
            if (StrUtil.isBlank(description)) {
                description = Convert.toStr(item.get("alt_description"), "");
            }

            String sourceImageUrl = Convert.toStr(item.get("source_image_url"));

            // 安全获取数字
            Integer width = Convert.toInt(item.get("width"), 0);
            Integer height = Convert.toInt(item.get("height"), 0);

            String orientation = Convert.toStr(item.get("orientation"), "unknown");

            imgs.add(new Img(imageUrl, description, sourceImageUrl, width, height, orientation));
        }
        return imgs;
    }
}
