package anzihe.com.common_template;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

/**
 * 临时诊断：用随机端口起真实 Web 环境，打完整 /job/test 链路。用完即删。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TmpDiagTest {

    @LocalServerPort
    private int port;

    @Test
    public void hitEndpoint() throws Exception {
        String q = URLEncoder.encode("请联网搜索一下 Spring AI 2.0 什么时候发布的", StandardCharsets.UTF_8);
        URI uri = URI.create("http://localhost:" + port + "/api/job/test?message=" + q);
        HttpResponse<String> resp = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(uri).GET().build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        System.out.println(">>> HTTP " + resp.statusCode());
        System.out.println(">>> BODY: " + resp.body());
    }
}
