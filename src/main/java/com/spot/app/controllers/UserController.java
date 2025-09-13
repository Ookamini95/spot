package com.spot.app.controllers;

import com.spot.app.dtos.EventDTO;
import com.spot.app.dtos.SpotDTO;
import com.spot.app.dtos.UserCrudDTO;
import com.spot.app.dtos.UserDTO;
import com.spot.app.entities.User;
import com.spot.app.repositories.EventRepository;
import com.spot.app.repositories.SpotRepository;
import com.spot.app.repositories.UserRepository;
import com.spot.app.services.NotificationService;
import com.spot.app.services.SessionService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController extends BaseController {

    private final SessionService sessionService;
    private final NotificationService notificationService;

    private UserRepository userRepository;
    private EventRepository eventRepository;
    private SpotRepository spotRepository;

    public UserController(UserRepository userRepo, EventRepository eventRepo, SpotRepository spotRepo,
            SessionService sessionService, NotificationService notificationService) {
        this.userRepository = userRepo;
        this.eventRepository = eventRepo;
        this.spotRepository = spotRepo;
        this.sessionService = sessionService;
        this.notificationService = notificationService;
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        User user = userRepository.findById(userId).orElse(null);
        if (user == null)
            return notFound("User not found");

        UserDTO userDto = new UserDTO(user);
        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Integer userId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();
        // Fetch target user by ID
        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return notFound("User not found");
        }
        UserCrudDTO userCrudDto = new UserCrudDTO(targetUser); // Basic public info DTO
        return ResponseEntity.ok(userCrudDto);
    }

    @GetMapping("/{userId}/follow")
    public ResponseEntity<?> followUser(@PathVariable Integer userId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        User currentUser = userRepository.findById(sessionUserId).orElse(null);
        if (currentUser.getId().equals(userId)) {
            return badRequest("Cannot follow yourself");
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return notFound("User not found");
        }

        if (currentUser.getFollowing().contains(targetUser)) {
            return ResponseEntity.ok().body("Already following user");
        }

        currentUser.getFollowing().add(targetUser);
        targetUser.getFollowers().add(currentUser);
        userRepository.save(currentUser);
        userRepository.save(targetUser);

        notificationService.sendFollowNotification(
                targetUser.getId(),
                currentUser.getUsername());

        return ResponseEntity.ok().body("User successfully followed");
    }

    @GetMapping("/{userId}/unfollow")
    public ResponseEntity<?> unfollowUser(@PathVariable Integer userId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        User currentUser = userRepository.findById(sessionUserId).orElse(null);
        if (currentUser == null) {
            return unauthorized();
        }
        if (currentUser.getId().equals(userId)) {
            return badRequest("Cannot unfollow yourself");
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return notFound("User not found");
        }

        if (!currentUser.getFollowing().contains(targetUser)) {
            return ResponseEntity.ok().body("Not currently following user");
        }
        currentUser.getFollowing().remove(targetUser);
        targetUser.getFollowers().remove(currentUser);

        userRepository.save(currentUser);
        userRepository.save(targetUser);
        return ResponseEntity.ok().body("User unfollowed successfully");
    }

    // GET /users/search?query={username}&page={n}&size={m}
    @GetMapping("/search")
    public ResponseEntity<?> searchUsers(@RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        User currentUser = userRepository.findById(sessionUserId).orElse(null);
        if (currentUser == null) {
            return unauthorized();
        }
        if (query == null || query.trim().isEmpty()) {
            return badRequest("Search query cannot be empty");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<User> resultPage = userRepository.searchByUsername(query, pageable);
        Page<UserCrudDTO> dtoPage = resultPage.map(UserCrudDTO::new);
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/friends")
    public ResponseEntity<?> getFriendsList(
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        User currentUser = userRepository.findById(sessionUserId).orElse(null);
        if (currentUser == null) {
            return unauthorized();
        }

        List<UserCrudDTO> friends = userRepository.findFriends(currentUser.getId()).stream()
                .map(UserCrudDTO::new).toList();
        return ResponseEntity.ok(friends);
    }

    @GetMapping("/{userId}/events")
    public ResponseEntity<?> getUserEvents(@PathVariable Integer userId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        if (!sessionUserId.equals(userId) && !userRepository.vibeCheck(sessionUserId, userId)) {
            return forbidden("You must be friends to view this user's spots");
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return notFound("User not found");
        }

        List<EventDTO> events = eventRepository.findEventsByUserId(userId).stream()
                .map(EventDTO::new).toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{userId}/spots")
    public ResponseEntity<?> getUserSpots(@PathVariable Integer userId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        if (!sessionUserId.equals(userId) && !userRepository.vibeCheck(sessionUserId, userId)) {
            return forbidden("You must be friends to view this user's spots");
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return notFound("User not found");
        }

        List<SpotDTO> spots = spotRepository.findFollowedSpots(userId).stream()
                .map(SpotDTO::new).toList();
        return ResponseEntity.ok(spots);
    }

    @GetMapping("/{userId}/followers")
    public ResponseEntity<?> getUserFollowers(@PathVariable Integer userId,
            @RequestHeader(value = "X-Session-Token", required = false) String sessionToken) {
        Integer sessionUserId = sessionService.getUserIdFromSession(sessionToken);
        if (sessionUserId == null)
            return unauthorized();

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return notFound("User not found");
        }

        if (!sessionUserId.equals(userId) && !userRepository.vibeCheck(sessionUserId, userId)) {
            return forbidden("You must be friends to view this user's spots");
        }

        Set<User> followers = targetUser.getFollowers();
        List<UserCrudDTO> followerDtos = followers.stream().map(UserCrudDTO::new).toList();
        return ResponseEntity.ok(followerDtos);
    }

}
