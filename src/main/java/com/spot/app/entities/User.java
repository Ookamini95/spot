package com.spot.app.entities;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String username;

    private String email;

    private String password;

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Spot> spots = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "user_follow", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "follower_id"))
    private Set<User> followers = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<User> following = new HashSet<>();

    @ManyToMany(mappedBy = "invitedUsers")
    private Set<Spot> invitedSpots = new HashSet<>();

    @ManyToMany(mappedBy = "invitedUsers")
    private Set<Event> invitedEvents = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<Spot> followedSpots = new HashSet<>();

    @ManyToMany(mappedBy = "followers")
    private Set<Event> followedEvents = new HashSet<>();

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Spot> getSpots() {
        return spots;
    }

    public void setSpots(Set<Spot> spots) {
        this.spots = spots;
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void setEvents(Set<Event> events) {
        this.events = events;
    }

    public Set<Comment> getComments() {
        return comments;
    }

    public void setComments(Set<Comment> comments) {
        this.comments = comments;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }

    public Set<User> getFollowing() {
        return following;
    }

    public void setFollowing(Set<User> following) {
        this.following = following;
    }

    public Set<Spot> getInvitedSpots() {
        return invitedSpots;
    }

    public void setInvitedSpots(Set<Spot> invitedSpots) {
        this.invitedSpots = invitedSpots;
    }

    public Set<Event> getInvitedEvents() {
        return invitedEvents;
    }

    public void setInvitedEvents(Set<Event> invitedEvents) {
        this.invitedEvents = invitedEvents;
    }

    public Set<Spot> getFollowedSpots() {
        return followedSpots;
    }

    public void setFollowedSpots(Set<Spot> followedSpots) {
        this.followedSpots = followedSpots;
    }

    public Set<Event> getFollowedEvents() {
        return followedEvents;
    }

    public void setFollowedEvents(Set<Event> followedEvents) {
        this.followedEvents = followedEvents;
    }
}
