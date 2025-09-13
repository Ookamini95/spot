package com.spot.app.dtos;

import com.spot.app.entities.User;

// Per il profilo dell'utente utilizzare l'intero UserDTO
public class UserCrudDTO {

    private Integer id;
    private String username;
    private Integer followerCount;
    private Integer followingCount;

    public UserCrudDTO(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.followerCount = user.getFollowers() != null ? user.getFollowers().size() : 0;
        this.followingCount = user.getFollowing() != null ? user.getFollowing().size() : 0;
    }

    public UserCrudDTO(Integer id, String username, int followerCount, int followingCount) {
        this.id = id;
        this.username = username;
        this.followerCount = followerCount;
        this.followingCount = followingCount;
    }

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

    public Integer getFollowerCount() {
        return followerCount;
    }

    public void setFollowerCount(Integer followerCount) {
        this.followerCount = followerCount;
    }

    public Integer getFollowingCount() {
        return followingCount;
    }

    public void setFollowingCount(Integer followingCount) {
        this.followingCount = followingCount;
    }
}
