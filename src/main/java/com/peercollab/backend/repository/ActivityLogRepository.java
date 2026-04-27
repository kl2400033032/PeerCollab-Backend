package com.peercollab.backend.repository;

import com.peercollab.backend.entity.ActivityLog;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {

    Page<ActivityLog> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<ActivityLog> findTop10ByOrderByCreatedAtDesc();
}
