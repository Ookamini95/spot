package com.spot.app.controllers;

import com.spot.app.dtos.CommentDTO;
import com.spot.app.entities.Comment;
import com.spot.app.entities.Event;
import com.spot.app.entities.User;
import com.spot.app.repositories.CommentRepository;
import com.spot.app.repositories.EventRepository;
import com.spot.app.repositories.UserRepository;
import com.spot.app.services.SessionService;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/comments")
public class CommentController extends BaseController {

    private final CommentRepository commentRepo;
    private final EventRepository eventRepo;
    private final UserRepository userRepo;
    private final SessionService sessionService;

    public CommentController(CommentRepository commentRepo, EventRepository eventRepo,
            UserRepository userRepo, SessionService sessionService) {
        this.commentRepo = commentRepo;
        this.eventRepo = eventRepo;
        this.userRepo = userRepo;
        this.sessionService = sessionService;
    }

    @PostMapping()
    public ResponseEntity<?> postComment(
            @RequestBody CommentDTO dto,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        if (dto.getText() == null || dto.getText().trim().isBlank()) {
            return badRequest("Missing required field: text");
        }
        if (dto.getDate() == null) {
            return badRequest("Missing required field: date");
        }
        if (dto.getEventId() == null) {
            return badRequest("Missing required field: event id");
        }

        Optional<Event> eventOpt = eventRepo.findById(dto.getEventId());
        if (eventOpt.isEmpty())
            return notFound("Event not found");

        if (!eventRepo.userAllowed(userId, dto.getEventId())) {
            return forbidden("You can't comment on this event");
        }

        Optional<User> userOpt = userRepo.findById(userId);
        if (userOpt.isEmpty())
            return notFound("User not found");

        Comment comment = new Comment();
        comment.setOwner(userOpt.get());
        comment.setEvent(eventOpt.get());
        comment.setText(dto.getText());
        comment.setDate(dto.getDate());

        Comment saved = commentRepo.save(comment);
        return ResponseEntity.ok(new CommentDTO(saved));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<?> getComments(
            @PathVariable Integer eventId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        if (!eventRepo.userAllowed(userId, eventId)) {
            return forbidden("You can't view this event's comments");
        }

        var comments = commentRepo.findByEventId(eventId, PageRequest.of(page, size))
                .map(CommentDTO::new);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getCommentById(
            @PathVariable Integer commentId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        Optional<Comment> commentOpt = commentRepo.findById(commentId);
        if (commentOpt.isEmpty())
            return notFound("Comment not found");

        Comment comment = commentOpt.get();

        if (!eventRepo.userAllowed(userId, comment.getEvent().getId())) {
            return forbidden("You can't view this comment");
        }

        return ResponseEntity.ok(new CommentDTO(comment));
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<?> editComment(
            @PathVariable Integer commentId,
            @RequestBody CommentDTO dto,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        Optional<Comment> commentOpt = commentRepo.findById(commentId);
        if (commentOpt.isEmpty())
            return notFound("Comment not found");

        Comment comment = commentOpt.get();
        if (!comment.getOwner().getId().equals(userId)) {
            return forbidden("You can't edit this comment");
        }

        if (dto.getText() != null && !dto.getText().isBlank()) {
            comment.setText(dto.getText());
            comment.setEdited(true);
        }
        if (dto.getDate() != null) {
            comment.setDate(dto.getDate());
            comment.setEdited(true);
        }

        Comment updated = commentRepo.save(comment);
        return ResponseEntity.ok(new CommentDTO(updated));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Integer commentId,
            @RequestHeader("X-Session-Token") String sessionToken) {

        Integer userId = sessionService.getUserIdFromSession(sessionToken);
        if (userId == null)
            return unauthorized();

        Optional<Comment> commentOpt = commentRepo.findById(commentId);
        if (commentOpt.isEmpty())
            return notFound("Comment not found");

        Comment comment = commentOpt.get();
        if (!comment.getOwner().getId().equals(userId)) {
            return forbidden("You can't delete this comment");
        }

        commentRepo.delete(comment);
        return ResponseEntity.ok().build();
    }
}
