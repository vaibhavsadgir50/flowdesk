package com.flowdesk.repository;

import com.flowdesk.model.ItemStatus;
import com.flowdesk.model.WorkItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkItemRepository extends JpaRepository<WorkItem, Long> {

    List<WorkItem> findByStatusOrderByCreatedAtDesc(ItemStatus status);

    List<WorkItem> findAllByOrderByCreatedAtDesc();
}
