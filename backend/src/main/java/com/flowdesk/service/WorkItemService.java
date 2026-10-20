package com.flowdesk.service;

import com.flowdesk.ai.AiProvider;
import com.flowdesk.dto.AiAnalysis;
import com.flowdesk.dto.WorkItemRequest;
import com.flowdesk.model.ItemStatus;
import com.flowdesk.model.Priority;
import com.flowdesk.model.WorkItem;
import com.flowdesk.repository.WorkItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class WorkItemService {

    private final WorkItemRepository repository;
    private final AiProvider aiProvider;

    public WorkItemService(WorkItemRepository repository, AiProvider aiProvider) {
        this.repository = repository;
        this.aiProvider = aiProvider;
    }

    @Transactional(readOnly = true)
    public List<WorkItem> list(ItemStatus status) {
        return status == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Transactional(readOnly = true)
    public WorkItem get(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException(id));
    }

    public WorkItem create(WorkItemRequest request) {
        WorkItem item = new WorkItem();
        apply(item, request);
        return repository.save(item);
    }

    public WorkItem update(Long id, WorkItemRequest request) {
        WorkItem item = get(id);
        apply(item, request);
        return item;
    }

    public void delete(Long id) {
        repository.delete(get(id));
    }

    /**
     * Runs the AI assistant and stores its output on the item. This runs outside a transaction so
     * a slow LLM response does not hold a database connection; the final save is its own transaction.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public WorkItem analyze(Long id) {
        WorkItem item = get(id);
        AiAnalysis analysis = aiProvider.analyze(item.getTitle(), item.getDescription());
        item.setAiSummary(analysis.summary());
        item.setCategory(analysis.category());
        item.setAiDraftReply(analysis.draftReply());
        return repository.save(item);
    }

    private void apply(WorkItem item, WorkItemRequest request) {
        item.setTitle(request.title().strip());
        item.setDescription(request.description().strip());
        item.setRequester(request.requester());
        item.setPriority(request.priority() != null ? request.priority() : Priority.MEDIUM);
        if (request.status() != null) {
            item.setStatus(request.status());
        }
    }
}
