package com.aiinterview.ai.provider;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AiProviderSelectionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OpenAiProvider.class, GroqAiProvider.class);

    @Test
    void selectsGroqProvider() {
        contextRunner.withPropertyValues("ai.provider=groq")
                .run(context -> assertThat(context).hasSingleBean(GroqAiProvider.class)
                        .doesNotHaveBean(OpenAiProvider.class));
    }

    @Test
    void selectsOpenAiProvider() {
        contextRunner.withPropertyValues("ai.provider=openai")
                .run(context -> assertThat(context).hasSingleBean(OpenAiProvider.class)
                        .doesNotHaveBean(GroqAiProvider.class));
    }
}
