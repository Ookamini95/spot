package com.spot.app.controllers;

import com.spot.app.dtos.FeedItemDTO;
import com.spot.app.dtos.NotificationDTO;
import com.spot.app.entities.User;
import com.spot.app.repositories.EventRepository;
import com.spot.app.repositories.SpotRepository;
import com.spot.app.repositories.UserRepository;
import com.spot.app.services.NotificationService;
import com.spot.app.services.SessionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/feed")
public class FeedController extends BaseController {

    private final EventRepository eventRepo;
    private final SpotRepository spotRepo;
    private final UserRepository userRepo;
    private final SessionService sessionService;
    private final NotificationService notificationService;

    public FeedController(
            EventRepository eventRepo,
            SpotRepository spotRepo,
            UserRepository userRepo,
            SessionService sessionService,
            NotificationService notificationService) {
        this.eventRepo = eventRepo;
        this.spotRepo = spotRepo;
        this.userRepo = userRepo;
        this.sessionService = sessionService;
        this.notificationService = notificationService;
    }

    @GetMapping()
    public ResponseEntity<?> getFeed(@RequestHeader("X-Session-Token") String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) return unauthorized();

        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isEmpty()) return unauthorized();

        User currentUser = optionalUser.get();

        Set<Integer> followedIds = currentUser.getFollowing().stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        followedIds.add(userId); // include own content

        List<FeedItemDTO> feed = new ArrayList<>();

        eventRepo.findByOwnerIdIn(new ArrayList<>(followedIds))
                .forEach(e -> feed.add(FeedItemDTO.fromEvent(e)));

        eventRepo.findByInvitedUsersContains(currentUser)
                .forEach(e -> feed.add(FeedItemDTO.fromEvent(e)));

        spotRepo.findByOwnerIdIn(new ArrayList<>(followedIds))
                .forEach(s -> feed.add(FeedItemDTO.fromSpot(s)));

        feed.sort((a, b) -> {
            if (a.getDate() != null && b.getDate() != null) return b.getDate().compareTo(a.getDate());
            if (a.getDate() != null) return -1;
            if (b.getDate() != null) return 1;
            return b.getId().compareTo(a.getId());
        });

        return ResponseEntity.ok(feed);
    }

    @GetMapping("/notifications")
    public ResponseEntity<?> getUnreadNotifications(@RequestHeader("X-Session-Token") String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) return unauthorized();

        List<NotificationDTO> dtoList = notificationService.getUnreadNotifications(userId).stream()
                .map(NotificationDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/notifications/read")
    public ResponseEntity<?> markAllAsRead(@RequestHeader("X-Session-Token") String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) return unauthorized();

        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }
}
