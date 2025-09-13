package com.spot.app.dtos;

import com.spot.app.entities.Event;
import com.spot.app.entities.User;
import com.spot.app.enums.Privacy;

import java.util.List;

public class EventDTO {

    private Integer id;
    private Integer ownerId;

    private Integer spotId;
    private String title;
    private String description;
    private Privacy privacy;
    private String date; // assumes iso
    private List<Integer> invitedUsers;

    public EventDTO() {}

    public EventDTO(Event e) {
        this.id = e.getId();
        this.title = e.getTitle();
        this.description = e.getDescription();
        this.privacy = e.getPrivacy();
        this.date = e.getDate().toString();
        this.ownerId = e.getOwner().getId();
        this.spotId = e.getSpot().getId();
        this.invitedUsers = e.getInvitedUsers().stream()
                .map(User::getId)
                .toList();
    }

    public EventDTO(Integer id, String title, String description, Privacy privacy,
            String date, Integer ownerId, Integer spotId,
            List<Integer> invitedUsers) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.privacy = privacy;
        this.date = date;
        this.ownerId = ownerId;
        this.spotId = spotId;
        this.invitedUsers = invitedUsers;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Integer ownerId) {
        this.ownerId = ownerId;
    }

    public Integer getSpotId() {
        return spotId;
    }

    public void setSpotId(Integer spotId) {
        this.spotId = spotId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Integer> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<Integer> invitedUsers) {
        this.invitedUsers = invitedUsers;
    }
}
