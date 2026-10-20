package com.flowdesk.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowdesk.ai.AiProperties;
import com.flowdesk.ai.AiProvider;
import com.flowdesk.ai.HeuristicAiProvider;
import com.flowdesk.ai.LlmAiProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfig {

    @Bean
    AiProvider aiProvider(AiProperties properties, RestClient.Builder builder, ObjectMapper mapper) {
        return properties.hasApiKey()
                ? new LlmAiProvider(builder, properties, mapper)
                : new HeuristicAiProvider();
    }
}
