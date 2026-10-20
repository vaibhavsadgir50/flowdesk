package com.flowdesk.dto;

import com.flowdesk.model.ItemStatus;
import com.flowdesk.model.Priority;
import com.flowdesk.model.WorkItem;
import java.time.Instant;

public record WorkItemResponse(
        Long id,
        String title,
        String description,
        String requester,
        ItemStatus status,
        Priority priority,
        String category,
        String aiSummary,
        String aiDraftReply,
        Instant createdAt,
        Instant updatedAt) {

    public static WorkItemResponse from(WorkItem item) {
        return new WorkItemResponse(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getRequester(),
                item.getStatus(),
                item.getPriority(),
                item.getCategory(),
                item.getAiSummary(),
                item.getAiDraftReply(),
                item.getCreatedAt(),
                item.getUpdatedAt());
    }
}
