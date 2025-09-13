package com.spot.app.controllers;

import com.spot.app.dtos.SpotDTO;
import com.spot.app.dtos.UserCrudDTO;
import com.spot.app.entities.Spot;
import com.spot.app.entities.User;
import com.spot.app.enums.Privacy;
import com.spot.app.repositories.SpotRepository;
import com.spot.app.repositories.UserRepository;
import com.spot.app.services.NotificationService;
import com.spot.app.services.SessionService;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Coordinate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/spots")
public class SpotController extends BaseController {

    private final SessionService sessionService;
    private final NotificationService notificationService;

    private final SpotRepository spotRepo;
    private final UserRepository userRepo;
    private final GeometryFactory geometryFactory = new GeometryFactory(); // TODO: Bean

    public SpotController(SpotRepository repo, UserRepository userRepo, SessionService sessionService,
            NotificationService notificationService) {
        this.spotRepo = repo;
        this.userRepo = userRepo;
        this.sessionService = sessionService;
        this.notificationService = notificationService;
    }

    @GetMapping()
    public ResponseEntity<?> getAllInRange(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") Integer meters,
            @RequestHeader("X-Session-Token") String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        // Spot validi filtrati nella query
        List<SpotDTO> spots = spotRepo.findAllSpotsWithinRange(userId, lat, lng, meters).stream()
                .map(SpotDTO::new)
                .toList();

        return ResponseEntity.ok(spots);
    }

    @GetMapping("/nearest")
    public ResponseEntity<?> findNearest(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "1000") double meters,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        // Spot validi filtrati nella query
        Spot s = spotRepo.findNearestSpotWithinRange(userId, lat, lng, meters);

        return (s != null) ? ResponseEntity.ok(new SpotDTO(s)) : ResponseEntity.noContent().build();
    }

    @PostMapping()
    public ResponseEntity<?> createSpot(
            @RequestBody SpotDTO dto,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer ownerId = sessionService.getUserIdFromSession(sessionToken);
        if (ownerId == null) {
            return unauthorized();
        }

        if (dto.getName() == null || dto.getLat() == null || dto.getLng() == null) {
            return badRequest("Missing required fields: name, lat, lng");
        }

        if (dto.getPrivacy() == Privacy.INVITE_ONLY
                && (dto.getInvitedUsers() == null || dto.getInvitedUsers().isEmpty())) {
            return badRequest("Missing required fields: invited users for INVITE_ONLY spot");
        }

        User owner = userRepo.findById(ownerId).orElse(null);
        if (owner == null) {
            return notFound("User not found");
        }

        Point point = geometryFactory.createPoint(new Coordinate(dto.getLng(), dto.getLat()));

        Spot spot = new Spot();
        spot.setName(dto.getName());
        spot.setDescription(dto.getDescription());
        spot.setPosition(point);
        spot.setOwner(owner);
        spot.setPrivacy(dto.getPrivacy() != null ? dto.getPrivacy() : Privacy.PUBLIC);

        if (spot.getPrivacy() == Privacy.INVITE_ONLY && dto.getInvitedUsers() != null) {
            List<User> invited = userRepo.findAllById(dto.getInvitedUsers());
            spot.setInvitedUsers(invited);

            for (User invitedUser : invited) {
                if (!invitedUser.getId().equals(ownerId)) {
                    notificationService.sendInvitationNotification(
                            invitedUser.getId(),
                            owner.getUsername(),
                            spot.getName());
                }
            }
        }

        Spot savedSpot = spotRepo.save(spot);
        return ResponseEntity.ok(new SpotDTO(savedSpot));
    }

    @GetMapping("/{spotId:\\d+}")
    public ResponseEntity<?> getById(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        SpotDTO dto = new SpotDTO(optionalSpot.get());

        if (optionalSpot.isPresent()) {
            if (!spotRepo.userAllowed(userId, dto.getId()))
                return notFound("Spot not found (for you >:) eheh)");
        }
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{spotId:\\d+}")
    public ResponseEntity<?> deleteSpot(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {
        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        Spot spot = optionalSpot.get();
        if (!spot.getOwner().getId().equals(userId)) {
            return forbidden("You don't have permission to delete this spot");
        }

        spotRepo.delete(spot);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{spotId:\\d+}")
    public ResponseEntity<?> updateSpot(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken,
            @RequestBody SpotDTO dto) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);

        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        Spot spot = optionalSpot.get();
        if (!spot.getOwner().getId().equals(userId)) {
            return forbidden("You don't own this spot");
        }

        if (dto.getName() != null)
            spot.setName(dto.getName());
        if (dto.getDescription() != null)
            spot.setDescription(dto.getDescription());
        if (dto.getLat() != null && dto.getLng() != null) {
            Point newPoint = geometryFactory.createPoint(new Coordinate(dto.getLng(), dto.getLat()));
            spot.setPosition(newPoint);
        }
        if (dto.getPrivacy() != null)
            spot.setPrivacy(dto.getPrivacy());

        if (spot.getPrivacy() == Privacy.INVITE_ONLY && dto.getInvitedUsers() != null) {
            List<User> newInvited = userRepo.findAllById(dto.getInvitedUsers());
            List<User> oldInvited = new ArrayList<>(spot.getInvitedUsers());

            spot.setInvitedUsers(newInvited);

            for (User u : newInvited) {
                if (!oldInvited.contains(u) && !u.getId().equals(spot.getOwner().getId())) {
                    notificationService.sendInvitationNotification(
                            u.getId(), spot.getOwner().getUsername(), spot.getName());
                }
            }
        }

        Spot updated = spotRepo.save(spot);
        return ResponseEntity.ok(new SpotDTO(updated));
    }

    @GetMapping("/{spotId:\\d+}/follow")
    public ResponseEntity<?> followSpot(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isEmpty()) {
            return notFound("User not found");
        }

        Spot spot = optionalSpot.get();
        User user = optionalUser.get();

        if (spot.getFollowers().contains(user)) {
            return conflict("Already following this spot");
        }

        spot.getFollowers().add(user);
        spotRepo.save(spot);

        return ResponseEntity.ok("Spot successfully followed");
    }

    @GetMapping("/{spotId:\\d+}/unfollow")
    public ResponseEntity<?> unfollowSpot(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        Optional<User> optionalUser = userRepo.findById(userId);
        if (optionalUser.isEmpty()) {
            return notFound("User not found");
        }

        Spot spot = optionalSpot.get();
        User user = optionalUser.get();

        if (!spot.getFollowers().contains(user)) {
            return badRequest("You are not following this spot");
        }

        spot.getFollowers().remove(user);
        spotRepo.save(spot);

        return ResponseEntity.ok("Spot successfully followed");
    }

    @GetMapping("/{spotId:\\d+}/followers")
    public ResponseEntity<?> getFollowers(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        Spot spot = optionalSpot.get();

        if (!spotRepo.userAllowed(userId, spotId)) {
            return forbidden("You are not allowed to view followers of this spot");
        }

        var followers = spot.getFollowers().stream()
                .map(UserCrudDTO::new)
                .toList();

        return ResponseEntity.ok(followers);
    }

    @GetMapping("/{spotId:\\d+}/invited")
    public ResponseEntity<?> getInvitedUsers(
            @PathVariable Integer spotId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null) {
            return unauthorized();
        }

        Optional<Spot> optionalSpot = spotRepo.findById(spotId);
        if (optionalSpot.isEmpty()) {
            return notFound("Spot not found");
        }

        Spot spot = optionalSpot.get();

        if (spot.getPrivacy() != Privacy.INVITE_ONLY) {
            return badRequest("This spot is not invitation only");
        }

        boolean invited = spotRepo.vibeCheck(userId, spotId);

        if (!(invited || spot.getOwner().getId().equals(userId))) {
            return forbidden("You don't have permission to view this spot's invited users");
        }

        var invitedUsers = spot.getInvitedUsers().stream()
                .map(UserCrudDTO::new).toList();

        return ResponseEntity.ok(invitedUsers);
    }

}
