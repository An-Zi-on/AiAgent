package anzihe.com.agent.ai.loader;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 资源模版加载器
 */
@Component
@Slf4j
public class AiTemplateLoader {
    private  final ResourceLoader resourceLoader;

    public  AiTemplateLoader(ResourceLoader resourceLoader){
        this.resourceLoader = resourceLoader;
    }

    public  String load(String location){
        if (StrUtil.isBlank(location)){
            return "";
        }
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()){
            log.error("资源不存在,不存在的路径为{}",location);
        }
        try {
            //读取内容并返回
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
