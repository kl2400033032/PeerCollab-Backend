package com.peercollab.backend.service;

import com.peercollab.backend.dto.activity.ActivityLogResponse;
import com.peercollab.backend.dto.common.PagedResponse;
import com.peercollab.backend.entity.ActivityLog;
import com.peercollab.backend.entity.ActivityType;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.repository.ActivityLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final CurrentUserService currentUserService;

    public ActivityLogService(ActivityLogRepository activityLogRepository, CurrentUserService currentUserService) {
        this.activityLogRepository = activityLogRepository;
        this.currentUserService = currentUserService;
    }

    public void log(User user, ActivityType type, String description, String metadata) {
        ActivityLog activityLog = new ActivityLog();
        activityLog.setUser(user);
        activityLog.setType(type);
        activityLog.setDescription(description);
        activityLog.setMetadata(metadata);
        activityLog.setCreatedAt(LocalDateTime.now());
        activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ActivityLogResponse> getCurrentUserActivity(int page, int size) {
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ActivityLogResponse> resultPage = activityLogRepository.findByUser_IdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                .map(this::map);
        return new PagedResponse<>(
                resultPage.getContent(),
                resultPage.getNumber(),
                resultPage.getSize(),
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.isFirst(),
                resultPage.isLast()
        );
    }

    @Transactional(readOnly = true)
    public List<ActivityLogResponse> getRecentActivity() {
        return activityLogRepository.findTop10ByOrderByCreatedAtDesc()
                .stream()
                .map(this::map)
                .toList();
    }

    private ActivityLogResponse map(ActivityLog activityLog) {
        return new ActivityLogResponse(
                activityLog.getId(),
                activityLog.getUser().getName(),
                activityLog.getUser().getEmail(),
                activityLog.getType(),
                activityLog.getDescription(),
                activityLog.getMetadata(),
                activityLog.getCreatedAt()
        );
    }
}
