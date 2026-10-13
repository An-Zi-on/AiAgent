package anzihe.com.agent.ai.register;

import anzihe.com.agent.ai.entry.template.Scenes;
import anzihe.com.agent.ai.loader.AiTemplateLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@Slf4j
public class AiTemplateRegister {
    private  final  AiTemplateRegister templateRegister;

    private  final AiTemplateLoader templateLoader;

    /**
     * 缓存所有场景
     */
    public HashMap<String, Scenes> currentAllScenes = new HashMap<>();

    public AiTemplateRegister (AiTemplateRegister templateRegister,AiTemplateLoader templateLoader){
        this.templateRegister = templateRegister;
        this.templateLoader = templateLoader;
    }
}
