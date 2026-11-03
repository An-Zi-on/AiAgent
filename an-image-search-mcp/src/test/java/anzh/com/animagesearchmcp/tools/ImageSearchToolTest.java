package anzh.com.animagesearchmcp.tools;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ImageSearchToolTest {
    @Resource
    ImageSearchTool imageSearchTool ;

    @Test
    public void setImageSearchTool(){
        String s = imageSearchTool.searchImage("ocean");
        System.out.println(s);
    }
}