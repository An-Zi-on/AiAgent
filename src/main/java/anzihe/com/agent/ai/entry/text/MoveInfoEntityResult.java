package anzihe.com.agent.ai.entry.text;

public record MoveInfoEntityResult(Integer totalTokens,
                                   Integer promptTokens,
                                   Integer completionTokens ,
                                   Integer finishReason,
                                   MovieInfo result) {
}
