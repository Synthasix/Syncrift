package com.hexplatoon.syncrift_backend.repository;

import com.hexplatoon.syncrift_backend.entity.Notification;
import com.hexplatoon.syncrift_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    List<Notification> findByUserAndIsReadFalse(User user);
}

