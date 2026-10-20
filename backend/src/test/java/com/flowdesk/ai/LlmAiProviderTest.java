package com.flowdesk.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowdesk.dto.AiAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class LlmAiProviderTest {

    private MockRestServiceServer server;
    private LlmAiProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        AiProperties props = new AiProperties("test-key", "http://llm.test", "test-model", 500);
        provider = new LlmAiProvider(builder, props, new ObjectMapper());
    }

    private static String envelope(String text) {
        return "{\"content\":[{\"type\":\"text\",\"text\":" + new ObjectMapper().valueToTree(text) + "}]}";
    }

    @Test
    void parsesStructuredResponse() {
        String json = "{\"summary\":\"User cannot log in\",\"category\":\"account\",\"draftReply\":\"Hi, we are on it.\"}";
        server.expect(requestTo("http://llm.test/v1/messages"))
                .andExpect(header("x-api-key", "test-key"))
                .andRespond(withSuccess(envelope(json), MediaType.APPLICATION_JSON));

        AiAnalysis result = provider.analyze("Login", "Cannot log in");

        assertThat(result.summary()).isEqualTo("User cannot log in");
        assertThat(result.category()).isEqualTo("Account");
        assertThat(result.draftReply()).isEqualTo("Hi, we are on it.");
        server.verify();
    }

    @Test
    void toleratesCodeFencesAndUnknownCategories() {
        String text = "```json\n{\"summary\":\"S\",\"category\":\"Weird\",\"draftReply\":\"D\"}\n```";
        server.expect(requestTo("http://llm.test/v1/messages"))
                .andRespond(withSuccess(envelope(text), MediaType.APPLICATION_JSON));

        assertThat(provider.analyze("t", "d").category()).isEqualTo("General Inquiry");
    }

    @Test
    void rejectsIncompleteResponses() {
        server.expect(requestTo("http://llm.test/v1/messages"))
                .andRespond(withSuccess(envelope("{\"summary\":\"only summary\"}"), MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> provider.analyze("t", "d")).isInstanceOf(AiException.class);
    }

    @Test
    void wrapsHttpFailures() {
        server.expect(requestTo("http://llm.test/v1/messages")).andRespond(withServerError());

        assertThatThrownBy(() -> provider.analyze("t", "d")).isInstanceOf(AiException.class);
    }
}
