package anzihe.com.agent.ai.template;

import anzihe.com.agent.ai.config.AiTemplateProperties;
import anzihe.com.agent.ai.entry.template.Scenes;
import anzihe.com.agent.exception.BusinessException;
import anzihe.com.agent.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class AiTemplateRegister {

    /**
     * 缓存所有场景
     */
    public HashMap<String, Scenes> currentAllScenes = new HashMap<>();

    public AiTemplateRegister(AiTemplateProperties properties, AiTemplateLoader loader) {
        properties.scenes.forEach((name, item) -> {
            //场景提示词
            String systemPromote = loader.load(item.getSystemPromptLocation());
            //用户提示词模版
            String userTemplate = item.getUserTemplate();
            Scenes scenes = new Scenes(name, systemPromote, userTemplate);
            currentAllScenes.put(name, scenes);
        });
    }

    public Scenes get(String name) {
        Scenes scenes = currentAllScenes.get(name);
        if (scenes == null) {
            log.warn("{}场景不存在", name);
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "未知场景");
        }
        return scenes;
    }
}
