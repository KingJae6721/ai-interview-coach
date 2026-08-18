package com.aiinterview.ai.provider;

public interface AiProvider {

    String complete(AiCompletionRequest request);

    String getModel();
}
