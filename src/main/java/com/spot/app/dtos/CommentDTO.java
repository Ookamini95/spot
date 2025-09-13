package com.spot.app.dtos;

import com.spot.app.entities.Comment;

import java.time.LocalDateTime;

public class CommentDTO {

    private Integer id;


    private Integer ownerId;
    private String username;
    private Integer eventId;
    private String text;
    private LocalDateTime date; // assumes iso date format
    private boolean edited;

    public CommentDTO() {
    }

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.ownerId = comment.getOwner().getId();

        this.text = comment.getText();
        this.username = comment.getOwner().getUsername();
        this.eventId = comment.getEvent().getId();
        this.date = comment.getDate();
        this.edited = comment.isEdited();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public boolean isEdited() {
        return this.edited;
    }

    public void setEdited(boolean bEdit) {
        this.edited = bEdit;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
