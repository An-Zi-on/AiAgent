package anzh.com.animagesearchmcp.entry;

public record Img(
        String image_url,
        String description ,
        String source_image_url,
        Integer width,
        Integer height,
        String orientation) {
}
