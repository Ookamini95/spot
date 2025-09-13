package com.spot.app.controllers;

import com.spot.app.dtos.EventDTO;
import com.spot.app.dtos.UserCrudDTO;
import com.spot.app.entities.Event;
import com.spot.app.entities.Spot;
import com.spot.app.entities.User;
import com.spot.app.enums.Privacy;
import com.spot.app.repositories.EventRepository;
import com.spot.app.repositories.SpotRepository;
import com.spot.app.repositories.UserRepository;
import com.spot.app.services.NotificationService;
import com.spot.app.services.SessionService;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/events")
public class EventController extends BaseController {

    private final SessionService sessionService;
    private final NotificationService notificationService;

    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final SpotRepository spotRepo;

    public EventController(EventRepository eventRepo, SpotRepository spotRepository, UserRepository userRepo,
            SessionService sessionService, NotificationService notificationService) {
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.sessionService = sessionService;
        this.spotRepo = spotRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/spot/{spotId:\\d+}")
    public ResponseEntity<?> getEventsFromSpot(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        // filtered by query
        List<Event> allEvents = eventRepo.findEventsBySpot(spotId, userId);
        List<EventDTO> dtoList = allEvents.stream()
                .map(EventDTO::new)
                .toList();

        return ResponseEntity.ok(dtoList);
    }

    @PostMapping
    public ResponseEntity<?> createEvent(
            @RequestBody EventDTO dto,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer ownerId = sessionService.getUserIdFromSession(sessionToken);
        if (ownerId == null) {
            return unauthorized();
        }

        Optional<User> optionalUser = userRepo.findById(ownerId);
        if (optionalUser.isEmpty()) {
            return notFound("User not found");
        }

        if (dto.getTitle() == null || dto.getDate() == null || dto.getSpotId() == null) {
            return badRequest("Missing required fields: title, date, spotId");
        }

        Optional<Spot> optionalSpot = spotRepo.findById(dto.getSpotId());
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        if (dto.getPrivacy() == Privacy.INVITE_ONLY
                && (dto.getInvitedUsers() == null || dto.getInvitedUsers().isEmpty())) {
            return badRequest("Missing invited users for INVITE_ONLY event");
        }

        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setDescription(dto.getDescription());
        event.setPrivacy(dto.getPrivacy() != null ? dto.getPrivacy() : Privacy.PUBLIC);
        event.setDate(LocalDateTime.parse(dto.getDate())); // Assumes ISO 8601 format
        event.setOwner(optionalUser.get());
        event.setSpot(optionalSpot.get());

        if (event.getPrivacy() == Privacy.INVITE_ONLY) {
            for (Integer invitedUserId : dto.getInvitedUsers()) {
                userRepo.findById(invitedUserId).ifPresent(user -> {
                    event.inviteUser(user);
                    if (!user.getId().equals(event.getOwner().getId())) {
                        notificationService.sendInvitationNotification(
                                user.getId(),
                                optionalUser.get().getUsername(),
                                event.getTitle());
                    }
                });
            }
        }

        Event saved = eventRepo.save(event);
        return ResponseEntity.ok(new EventDTO(saved));
    }

    @GetMapping("/{eventId:\\d+}")
    public ResponseEntity<?> getById(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Event> optionalEvent = eventRepo.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return notFound("Event not found");
        }

        if (!eventRepo.userAllowed(userId, eventId)) {
            return notFound("Event not found (for you >:))");
        }

        return ResponseEntity.ok(new EventDTO(optionalEvent.get()));
    }

    @DeleteMapping("/{eventId:\\d+}")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        Optional<Event> optional = eventRepo.findById(eventId);
        if (optional.isEmpty())
            return notFound("Event not found");

        Event e = optional.get();
        if (!e.getOwner().getId().equals(userId)) {
            return forbidden("You don't have permission to delete this event");
        }

        eventRepo.delete(e);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{eventId:\\d+}")
    public ResponseEntity<?> updateEvent(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken,
            @RequestBody EventDTO dto) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Event> optional = eventRepo.findById(eventId);
        if (optional.isEmpty()) {
            return notFound("Event not found");
        }

        Event event = optional.get();

        if (!event.getOwner().getId().equals(userId)) {
            return forbidden("You don't have permission to update this event");
        }

        if (dto.getTitle() != null) {
            event.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null) {
            event.setDescription(dto.getDescription());
        }

        if (dto.getDate() != null) {
            try {
                event.setDate(LocalDateTime.parse(dto.getDate())); // assumes ISO 8601
            } catch (Exception e) {
                return badRequest("Invalid date format. Use ISO 8601.");
            }
        }

        if (dto.getSpotId() != null) {
            Optional<Spot> optionalSpot = spotRepo.findById(dto.getSpotId());
            if (optionalSpot.isEmpty()) {
                return notFound("Spot not found");
            }
            event.setSpot(optionalSpot.get());
        }

        Privacy oldPrivacy = event.getPrivacy();
        Privacy newPrivacy = dto.getPrivacy() != null ? dto.getPrivacy() : oldPrivacy;

        if (dto.getPrivacy() != null) {
            event.setPrivacy(newPrivacy);
        }

        if (newPrivacy == Privacy.INVITE_ONLY) {
            if (dto.getInvitedUsers() != null) {
                List<User> newInvited = userRepo.findAllById(dto.getInvitedUsers());
                List<User> oldInvited = new ArrayList<>(event.getInvitedUsers());

                event.getInvitedUsers().clear();
                event.getInvitedUsers().addAll(newInvited);

                for (User u : newInvited) {
                    if (!oldInvited.contains(u) && !u.getId().equals(event.getOwner().getId())) {
                        notificationService.sendInvitationNotification(
                                u.getId(),
                                event.getOwner().getUsername(),
                                event.getTitle());
                    }
                }
            } else if (oldPrivacy != Privacy.INVITE_ONLY) {
                return badRequest("Missing invited users for INVITE_ONLY event");
            }
        } else if (oldPrivacy == Privacy.INVITE_ONLY && newPrivacy != Privacy.INVITE_ONLY) {
            event.getInvitedUsers().clear();
        }

        Event updated = eventRepo.save(event);
        return ResponseEntity.ok(new EventDTO(updated));
    }

    @GetMapping("/{eventId:\\d+}/follow")
    public ResponseEntity<?> followEvent(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Event> optionalEvent = eventRepo.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return notFound("Event not found");
        }

        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isEmpty()) {
            return notFound("User not found");
        }

        Event event = optionalEvent.get();
        User user = optionalUser.get();

        if (!eventRepo.userAllowed(userId, eventId)) {
            return forbidden("You are not allowed to follow this event");
        }

        if (event.getFollowers().contains(user)) {
            return conflict("Already following this event");
        }

        event.getFollowers().add(user);
        eventRepo.save(event);

        return ResponseEntity.ok("Event successfully followed");
    }

    @GetMapping("/{eventId:\\d+}/unfollow")
    public ResponseEntity<?> unfollowEvent(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Event> optionalEvent = eventRepo.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return notFound("Event not found");
        }

        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isEmpty()) {
            return notFound("User not found");
        }

        Event event = optionalEvent.get();
        User user = optionalUser.get();

        if (!event.getFollowers().contains(user)) {
            return badRequest("You are not following this event");
        }

        event.getFollowers().remove(user);
        eventRepo.save(event);

        return ResponseEntity.ok("Event successfully unfollowed");
    }

    @GetMapping("/{eventId:\\d+}/followers")
    public ResponseEntity<?> getFollowers(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Event> optionalEvent = eventRepo.findById(eventId);
        if (optionalEvent.isEmpty()) {
            return notFound("Event not found");
        }

        Event event = optionalEvent.get();

        if (!eventRepo.userAllowed(userId, eventId)) {
            return forbidden("You are not allowed to view this event's followers");
        }

        var followers = event.getFollowers().stream()
                .map(UserCrudDTO::new)
                .toList();

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{eventId:\\d+}/invited")
    public ResponseEntity<?> getInvited(
            @PathVariable Integer eventId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        Optional<Event> opt = eventRepo.findById(eventId);
        if (opt.isEmpty())
            return notFound("Event not found");

        Event e = opt.get();

        if (e.getPrivacy() != Privacy.INVITE_ONLY) {
            return badRequest("This event is not invitation only");
        }

        boolean invited = eventRepo.vibeCheck(userId, eventId);
        if (!(invited || e.getOwner().getId().equals(userId))) {
            return forbidden("You can't view this event's invited users");
        }

        List<UserCrudDTO> list = e.getInvitedUsers().stream()
                .map(UserCrudDTO::new).toList();
        return ResponseEntity.ok(list);
    }
}
