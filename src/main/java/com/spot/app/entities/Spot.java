package com.spot.app.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.spot.app.enums.Privacy;

import jakarta.persistence.*;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "spots")
@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Spot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Column(length = 1000)
    private String description;

    @Column(columnDefinition = "geography(Point,4326)")
    private Point position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Privacy privacy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany
    @JoinTable(name = "spot_followers", joinColumns = @JoinColumn(name = "spot_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> followers = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "spot_invitations", joinColumns = @JoinColumn(name = "spot_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> invitedUsers = new HashSet<>();

    @OneToMany(mappedBy = "spot", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Event> events = new HashSet<>();

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Point getPosition() {
        return position;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<User> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<User> followers) {
        this.followers = followers;
    }

    public void addFollower(User user) {
        this.followers.add(user);
    }

    public void removeFollower(User user) {
        this.followers.remove(user);
    }

    public Set<User> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<User> userList) {
        this.invitedUsers = new HashSet<>(userList);
    }

    public void inviteUser(User user) {
        this.invitedUsers.add(user);
    }

    public void uninviteUser(User user) {
        this.invitedUsers.remove(user);
    }

    public Set<Event> getEvents() {
        return events;
    }

    public void addEvent(Event event) {
        events.add(event);
        event.setSpot(this);
    }

    public void removeEvent(Event event) {
        events.remove(event);
        event.setSpot(null);
    }
}
