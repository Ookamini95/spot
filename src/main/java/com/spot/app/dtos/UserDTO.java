package com.spot.app.dtos;

import java.util.ArrayList;
import java.util.List;

import com.spot.app.entities.Event;
import com.spot.app.entities.Spot;
import com.spot.app.entities.User;

public class UserDTO {

    private Integer id;

    private String username;
    private String email;
    private List<Integer> followers;
    private List<Integer> following;
    private List<Integer> ownedSpots;
    private List<Integer> ownedEvents;
    private List<Integer> followedSpots;
    private List<Integer> followedEvents;
    private List<Integer> invitedSpots;
    private List<Integer> invitedEvents;

    public UserDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();

        this.followers = user.getFollowers() != null
                ? user.getFollowers().stream().map(User::getId).toList()
                : new ArrayList<>();

        this.following = user.getFollowing() != null
                ? user.getFollowing().stream().map(User::getId).toList()
                : new ArrayList<>();

        this.ownedEvents = user.getEvents() != null
                ? user.getEvents().stream().map(Event::getId).toList()
                : new ArrayList<>();

        this.ownedSpots = user.getSpots() != null
                ? user.getSpots().stream().map(Spot::getId).toList()
                : new ArrayList<>();

        this.followedSpots = user.getFollowedSpots() != null
                ? user.getFollowedSpots().stream().map(Spot::getId).toList()
                : new ArrayList<>();

        this.followedEvents = user.getFollowedEvents() != null
                ? user.getFollowedEvents().stream().map(Event::getId).toList()
                : new ArrayList<>();

        this.invitedSpots = user.getInvitedSpots() != null
                ? user.getInvitedSpots().stream().map(Spot::getId).toList()
                : new ArrayList<>();

        this.invitedEvents = user.getInvitedEvents() != null
                ? user.getInvitedEvents().stream().map(Event::getId).toList()
                : new ArrayList<>();
    }

    // Getter e setter

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Integer> getFollowers() {
        return followers;
    }

    public void setFollowers(List<Integer> followers) {
        this.followers = followers;
    }

    public List<Integer> getFollowing() {
        return following;
    }

    public void setFollowing(List<Integer> following) {
        this.following = following;
    }

    public List<Integer> getOwnedSpots() {
        return ownedSpots;
    }

    public void setOwnedSpots(List<Integer> ownedSpots) {
        this.ownedSpots = ownedSpots;
    }

    public List<Integer> getOwnedEvents() {
        return ownedEvents;
    }

    public void setOwnedEvents(List<Integer> ownedEvents) {
        this.ownedEvents = ownedEvents;
    }

    public List<Integer> getFollowedSpots() {
        return followedSpots;
    }

    public void setFollowedSpots(List<Integer> followedSpots) {
        this.followedSpots = followedSpots;
    }

    public List<Integer> getFollowedEvents() {
        return followedEvents;
    }

    public void setFollowedEvents(List<Integer> followedEvents) {
        this.followedEvents = followedEvents;
    }

    public List<Integer> getInvitedSpots() {
        return invitedSpots;
    }

    public void setInvitedSpots(List<Integer> invitedSpots) {
        this.invitedSpots = invitedSpots;
    }

    public List<Integer> getInvitedEvents() {
        return invitedEvents;
    }

    public void setInvitedEvents(List<Integer> invitedEvents) {
        this.invitedEvents = invitedEvents;
    }
}
