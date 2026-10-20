package com.flowdesk.dto;

/** Result of running the AI assistant over a work item. */
public record AiAnalysis(String summary, String category, String draftReply) {
}
