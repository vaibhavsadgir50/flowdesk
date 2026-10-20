package com.flowdesk.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowdesk.dto.AiAnalysis;
import java.util.List;
import java.util.Map;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/** Calls an LLM messages API and asks for a structured JSON analysis. */
public class LlmAiProvider implements AiProvider {

    static final List<String> CATEGORIES =
            List.of("Bug", "Feature Request", "Billing", "Account", "General Inquiry");

    private static final String SYSTEM_PROMPT = """
            You are an assistant inside a business workflow tool. You receive one work item \
            (a title and a description) and must return ONLY a JSON object with these keys:
            "summary": one or two sentences summarising the request,
            "category": exactly one of %s,
            "draftReply": a polite, concise reply to the requester, ready to send.
            Do not include any text outside the JSON object.""".formatted(CATEGORIES);

    private final RestClient client;
    private final AiProperties properties;
    private final ObjectMapper mapper;

    public LlmAiProvider(RestClient.Builder builder, AiProperties properties, ObjectMapper mapper) {
        this.properties = properties;
        this.mapper = mapper;
        this.client = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("x-api-key", properties.apiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();
    }

    @Override
    public AiAnalysis analyze(String title, String description) {
        Map<String, Object> body = Map.of(
                "model", properties.model(),
                "max_tokens", properties.maxTokens(),
                "system", SYSTEM_PROMPT,
                "messages", List.of(Map.of(
                        "role", "user",
                        "content", "Title: " + title + "\n\nDescription:\n" + description)));

        JsonNode response;
        try {
            response = client.post()
                    .uri("/v1/messages")
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException e) {
            throw new AiException("LLM request failed", e);
        }
        return parse(response);
    }

    AiAnalysis parse(JsonNode response) {
        String text = response == null ? null : response.path("content").path(0).path("text").asText(null);
        if (text == null || text.isBlank()) {
            throw new AiException("LLM returned an empty response");
        }
        try {
            JsonNode json = mapper.readTree(extractJson(text));
            String summary = json.path("summary").asText("").trim();
            String draft = json.path("draftReply").asText("").trim();
            if (summary.isEmpty() || draft.isEmpty()) {
                throw new AiException("LLM response is missing required fields");
            }
            return new AiAnalysis(summary, normaliseCategory(json.path("category").asText("")), draft);
        } catch (java.io.IOException e) {
            throw new AiException("LLM response was not valid JSON", e);
        }
    }

    /** Models sometimes wrap JSON in code fences or prose; take the outermost object. */
    static String extractJson(String text) {
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        return (start >= 0 && end > start) ? text.substring(start, end + 1) : text;
    }

    static String normaliseCategory(String raw) {
        return CATEGORIES.stream()
                .filter(c -> c.equalsIgnoreCase(raw.trim()))
                .findFirst()
                .orElse("General Inquiry");
    }
}
