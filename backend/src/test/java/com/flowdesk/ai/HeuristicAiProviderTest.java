package com.flowdesk.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowdesk.dto.AiAnalysis;
import org.junit.jupiter.api.Test;

class HeuristicAiProviderTest {

    private final HeuristicAiProvider provider = new HeuristicAiProvider();

    @Test
    void categorisesBugs() {
        AiAnalysis result = provider.analyze("App crash", "The app crashes on startup. Please help.");
        assertThat(result.category()).isEqualTo("Bug");
        assertThat(result.summary()).startsWith("App crash:");
        assertThat(result.draftReply()).contains("investigating");
    }

    @Test
    void categorisesBilling() {
        assertThat(provider.analyze("Refund", "I was charged twice.").category()).isEqualTo("Billing");
    }

    @Test
    void fallsBackToGeneralInquiry() {
        assertThat(provider.analyze("Hello", "Just saying hi.").category()).isEqualTo("General Inquiry");
    }
}
