package com.spot.app.dtos;

import java.time.LocalDateTime;

import com.spot.app.entities.Notification;

public class NotificationDTO {

    private Integer id;

    private String message;
    private LocalDateTime createdAt;
    private boolean read;

   public NotificationDTO(Notification n) {
        this.id = n.getId();
        this.message = n.getMessage();
        this.createdAt = n.getCreatedAt();
        this.read = n.isRead();
    }

    public Integer getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isRead() {
        return read;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRead(boolean read) {
        this.read = read;
    }
}
