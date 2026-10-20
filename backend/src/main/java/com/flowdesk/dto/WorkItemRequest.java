package com.flowdesk.dto;

import com.flowdesk.model.ItemStatus;
import com.flowdesk.model.Priority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkItemRequest(
        @NotBlank @Size(max = 200) String title,
        @NotBlank @Size(max = 8000) String description,
        @Size(max = 120) String requester,
        Priority priority,
        ItemStatus status) {
}
