package com.flowdesk.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "flowdesk.ai")
public record AiProperties(String apiKey, String baseUrl, String model, int maxTokens) {

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }
}
