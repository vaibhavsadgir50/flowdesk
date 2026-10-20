package com.flowdesk.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.flowdesk.ai.AiProvider;
import com.flowdesk.dto.AiAnalysis;
import com.flowdesk.dto.WorkItemRequest;
import com.flowdesk.model.ItemStatus;
import com.flowdesk.model.Priority;
import com.flowdesk.model.WorkItem;
import com.flowdesk.repository.WorkItemRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkItemServiceTest {

    @Mock WorkItemRepository repository;
    @Mock AiProvider aiProvider;
    @InjectMocks WorkItemService service;

    private WorkItem item() {
        WorkItem item = new WorkItem();
        item.setTitle("Login fails");
        item.setDescription("I get an error when I log in.");
        return item;
    }

    @Test
    void createDefaultsPriorityToMedium() {
        when(repository.save(any(WorkItem.class))).thenAnswer(i -> i.getArgument(0));

        WorkItem created = service.create(new WorkItemRequest("  Title ", " Body ", null, null, null));

        assertThat(created.getTitle()).isEqualTo("Title");
        assertThat(created.getDescription()).isEqualTo("Body");
        assertThat(created.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(created.getStatus()).isEqualTo(ItemStatus.OPEN);
    }

    @Test
    void updateChangesStatus() {
        WorkItem existing = item();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));

        WorkItem updated = service.update(1L,
                new WorkItemRequest("New", "Body", "ann", Priority.HIGH, ItemStatus.RESOLVED));

        assertThat(updated.getStatus()).isEqualTo(ItemStatus.RESOLVED);
        assertThat(updated.getPriority()).isEqualTo(Priority.HIGH);
    }

    @Test
    void getThrowsWhenMissing() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(9L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void analyzeStoresAiOutput() {
        WorkItem existing = item();
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(aiProvider.analyze("Login fails", "I get an error when I log in."))
                .thenReturn(new AiAnalysis("Summary", "Bug", "Draft"));
        when(repository.save(existing)).thenReturn(existing);

        WorkItem result = service.analyze(1L);

        assertThat(result.getAiSummary()).isEqualTo("Summary");
        assertThat(result.getCategory()).isEqualTo("Bug");
        assertThat(result.getAiDraftReply()).isEqualTo("Draft");
        verify(repository).save(existing);
    }
}
