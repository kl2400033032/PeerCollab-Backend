package com.peercollab.backend.service;

import com.peercollab.backend.dto.common.PagedResponse;
import com.peercollab.backend.dto.notification.NotificationResponse;
import com.peercollab.backend.dto.notification.NotificationSummaryResponse;
import com.peercollab.backend.entity.Notification;
import com.peercollab.backend.entity.NotificationType;
import com.peercollab.backend.entity.User;
import com.peercollab.backend.exception.ResourceNotFoundException;
import com.peercollab.backend.repository.NotificationRepository;
import java.time.LocalDateTime;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final CurrentUserService currentUserService;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            CurrentUserService currentUserService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.currentUserService = currentUserService;
        this.messagingTemplate = messagingTemplate;
    }

    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationResponse createNotification(User recipient, NotificationType type, String title, String message, String link) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setLink(link);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = map(saved);
        messagingTemplate.convertAndSend("/topic/users/" + recipient.getId() + "/notifications", response);
        return response;
    }

    @Transactional(readOnly = true)
    public PagedResponse<NotificationResponse> getCurrentUserNotifications(int page, int size) {
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<NotificationResponse> resultPage = notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(currentUser.getId(), pageable)
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
    public NotificationSummaryResponse getNotificationSummary() {
        User currentUser = currentUserService.getCurrentUser();
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "createdAt"));
        return new NotificationSummaryResponse(
                notificationRepository.countByRecipient_IdAndReadFalse(currentUser.getId()),
                notificationRepository.findByRecipient_IdOrderByCreatedAtDesc(currentUser.getId(), pageable)
                        .map(this::map)
                        .getContent()
        );
    }

    @CacheEvict(value = "notifications", allEntries = true)
    public NotificationResponse markAsRead(Long id) {
        User currentUser = currentUserService.getCurrentUser();
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found."));
        if (!notification.getRecipient().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Notification not found.");
        }
        notification.setRead(true);
        return map(notificationRepository.save(notification));
    }

    private NotificationResponse map(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getLink(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
