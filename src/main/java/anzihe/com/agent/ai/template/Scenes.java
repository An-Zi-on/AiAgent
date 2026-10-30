package anzihe.com.agent.ai.template;

public record Scenes(
        //场景名称
        String name,
        //场景提示词
        String systemPrompt,
        //用户提示词模版
        String promptTemplate,
        //
        String resultFormat
) {
}
