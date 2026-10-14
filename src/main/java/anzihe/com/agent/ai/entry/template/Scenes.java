package anzihe.com.agent.ai.entry.template;

public record Scenes(
        //场景名称
        String name,
        //场景提示词
        String systemPromote,
        //用户提示词模版
        String promptTemplate) {
}
