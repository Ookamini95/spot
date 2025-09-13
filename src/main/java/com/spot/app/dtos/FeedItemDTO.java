package com.spot.app.dtos;

import com.spot.app.entities.Event;
import com.spot.app.entities.Spot;
import com.spot.app.enums.FeedType;

import java.time.LocalDateTime;

public class FeedItemDTO {

    private Integer id;
    
    private FeedType type;
    private String title;
    private String description;
    private LocalDateTime date;
    private Integer ownerId;
    private String ownerUsername;

    public FeedItemDTO(FeedType type, Integer id, String title, String description,
                       LocalDateTime date, Integer ownerId, String ownerUsername) {
        this.type = type;
        this.id = id;
        this.title = title;
        this.description = description;
        this.date = date;
        this.ownerId = ownerId;
        this.ownerUsername = ownerUsername;
    }

    public static FeedItemDTO fromEvent(Event event) {
        return new FeedItemDTO(
                FeedType.EVENT,
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getDate(),
                event.getOwner().getId(),
                event.getOwner().getUsername()
        );
    }

    public static FeedItemDTO fromSpot(Spot spot) {
        return new FeedItemDTO(
                FeedType.SPOT,
                spot.getId(),
                spot.getName(),
                spot.getDescription(),
                null,
                spot.getOwner().getId(),
                spot.getOwner().getUsername()
        );
    }

     public FeedType getType() {
        return type;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public String getOwnerUsername() {
        return ownerUsername;
    }

    public void setType(FeedType type) {
        this.type = type;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerUsername(String ownerUsername) {
        this.ownerUsername = ownerUsername;
    }
}
