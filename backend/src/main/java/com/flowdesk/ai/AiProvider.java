package com.flowdesk.ai;

import com.flowdesk.dto.AiAnalysis;

/** Produces a summary, category and draft reply for a work item. */
public interface AiProvider {

    AiAnalysis analyze(String title, String description);
}
