package com.spot.app.repositories;

import com.spot.app.entities.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserIdOrderByCreatedAtDesc(Integer userId);
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(Integer userId);
    boolean existsByUserIdAndReadFalse(Integer userId);
}
