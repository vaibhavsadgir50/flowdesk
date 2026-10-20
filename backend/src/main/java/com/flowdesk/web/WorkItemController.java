package com.flowdesk.web;

import com.flowdesk.dto.WorkItemRequest;
import com.flowdesk.dto.WorkItemResponse;
import com.flowdesk.model.ItemStatus;
import com.flowdesk.service.WorkItemService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/items")
public class WorkItemController {

    private final WorkItemService service;

    public WorkItemController(WorkItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<WorkItemResponse> list(@RequestParam(required = false) ItemStatus status) {
        return service.list(status).stream().map(WorkItemResponse::from).toList();
    }

    @GetMapping("/{id}")
    public WorkItemResponse get(@PathVariable Long id) {
        return WorkItemResponse.from(service.get(id));
    }

    @PostMapping
    public ResponseEntity<WorkItemResponse> create(@Valid @RequestBody WorkItemRequest request) {
        WorkItemResponse created = WorkItemResponse.from(service.create(request));
        return ResponseEntity.created(URI.create("/api/items/" + created.id())).body(created);
    }

    @PutMapping("/{id}")
    public WorkItemResponse update(@PathVariable Long id, @Valid @RequestBody WorkItemRequest request) {
        return WorkItemResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/ai/analyze")
    public WorkItemResponse analyze(@PathVariable Long id) {
        return WorkItemResponse.from(service.analyze(id));
    }
}
