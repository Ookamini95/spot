package com.spot.app.services;

import com.spot.app.entities.Notification;
import com.spot.app.enums.NotificationType;
import com.spot.app.repositories.NotificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private NotificationRepository repo;

    public NotificationService(NotificationRepository repo) {
        this.repo = repo;
    }

    public void sendFollowNotification(Integer toUserId, String followerUsername) {
        Notification n = new Notification();
        n.setUserId(toUserId);
        n.setType(NotificationType.FOLLOW);
        n.setMessage(followerUsername + " started following you.");
        n.setRead(false);
        repo.save(n);
    }

    public void sendInvitationNotification(Integer toUserId, String inviterUsername, String resourceName) {
        Notification n = new Notification();
        n.setUserId(toUserId);
        n.setType(NotificationType.INVITATION);

        String message = inviterUsername + " invited you to: " + resourceName;
        n.setMessage(message);

        n.setRead(false);
        repo.save(n);
    }

    public List<Notification> getUnreadNotifications(Integer userId) {
        return repo.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    } 

    // Mark as "unread" -- not implemented further
    public boolean hasUnread(Integer userId) {
        return repo.existsByUserIdAndReadFalse(userId);
    }

    public void markAllAsRead(Integer userId) {
        List<Notification> list = repo.findByUserIdOrderByCreatedAtDesc(userId);
        for (Notification n : list) {
            n.setRead(true);
        }
        repo.saveAll(list);
    }
}
