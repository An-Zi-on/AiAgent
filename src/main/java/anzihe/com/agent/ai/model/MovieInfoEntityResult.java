package anzihe.com.agent.ai.model;

public record MovieInfoEntityResult(Integer totalTokens,
                                    Integer promptTokens,
                                    Integer completionTokens ,
                                    Integer finishReason,
                                    MovieInfo result) {
}
