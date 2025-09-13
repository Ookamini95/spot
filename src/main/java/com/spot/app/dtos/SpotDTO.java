package com.spot.app.dtos;

import java.util.List;

import org.locationtech.jts.geom.Point;

import com.spot.app.entities.Spot;
import com.spot.app.entities.User;
import com.spot.app.enums.Privacy;

public class SpotDTO {

    private Integer id;

    private Integer ownerId;
    private String name;
    private String description;
    private Double lat;
    private Double lng;
    private Privacy privacy;
    private List<Integer> invitedUsers;

    public SpotDTO() {
    }

    public SpotDTO(Spot s) {
        this.id = s.getId();
        this.ownerId = s.getOwner().getId();
        this.name = s.getName();
        this.privacy = s.getPrivacy();
        this.description = s.getDescription();
        this.invitedUsers = s.getInvitedUsers().stream()
                .map(User::getId).toList();
        if (s.getPosition() != null) {
            this.lat = s.getPosition().getY();
            this.lng = s.getPosition().getX();
        }
    }

    public SpotDTO(Integer id, Integer ownerId, String name, String description, Point point,
            Privacy privacy, List<Integer> invited) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.privacy = privacy;
        this.description = description;
        this.invitedUsers = invited;
        if (point != null) {
            this.lat = point.getY();
            this.lng = point.getX();
        }
    }

    public SpotDTO(Integer id, Integer ownerId, String name, String description, Double lat, Double lng,
            Privacy privacy, List<Integer> invited) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.privacy = privacy;
        this.description = description;
        this.invitedUsers = invited;
        this.lat = lat;
        this.lng = lng;
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

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public void setPrivacy(Privacy privacy) {
        this.privacy = privacy;
    }

    public Privacy getPrivacy() {
        return privacy;
    }

    public List<Integer> getInvitedUsers() {
        return invitedUsers;
    }

    public void setInvitedUsers(List<Integer> invited) {
        this.invitedUsers = invited;
    }
}
