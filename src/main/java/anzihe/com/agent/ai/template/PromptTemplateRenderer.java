package anzihe.com.agent.ai.template;

import cn.hutool.core.util.StrUtil;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 渲染模版
 */
@Component
public class PromptTemplateRenderer {
    //返回渲染好的SpringAi提示词模版
    public  String userPrompt(String template, Map<String,Object> var){
        return render(template,var);
    }

    public  String resultFormat(String template, Map<String,Object> var ){
        return PromptTemplate
                .builder()
                .template(template)  //"用户问题：{question}"
                .build()
                .render(var == null?Map.of():var); //{question: "你好你好......."}
    }

    private  String render(String template , Map<String,Object> var){
        if (StrUtil.isBlank(template)){
            return "";
        }
        //返回 用户问题：{你好你好.......}
        return PromptTemplate
                .builder()
                .template(template)  //"用户问题：{question}"
                .build()
                .render(var == null?Map.of():var); //{question: "你好你好......."}
    }
}
