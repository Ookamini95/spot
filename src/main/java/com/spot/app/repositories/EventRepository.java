package com.spot.app.repositories;

import com.spot.app.entities.Event;
import com.spot.app.entities.User;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Integer> {

        List<Event> findByOwnerIdIn(List<Integer> ownerIds);

        List<Event> findByInvitedUsersContains(User user);

        @Query(value = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM event_invitations
                            WHERE event_id = :eventId AND user_id = :userId
                        )
                        """, nativeQuery = true)
        boolean vibeCheck(
                        @Param("userId") Integer userId,
                        @Param("eventId") Integer eventId);
        // ^ Checks if user invited to event

        @Query(value = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM events e
                            LEFT JOIN event_invitations ei ON ei.event_id = e.id
                            LEFT JOIN users u ON e.owner_id = u.id
                            LEFT JOIN user_follow f1 ON f1.user_id = :userId AND f1.follower_id = u.id
                            LEFT JOIN user_follow f2 ON f2.user_id = u.id AND f2.follower_id = :userId
                            WHERE e.id = :eventId
                            AND (
                                e.owner_id = :userId
                                OR e.privacy = 'PUBLIC'
                                OR (e.privacy = 'INVITE_ONLY' AND ei.user_id = :userId)
                                OR (e.privacy = 'FRIEND_ONLY' AND f1.user_id IS NOT NULL AND f2.user_id IS NOT NULL)
                            )
                        )
                        """, nativeQuery = true)
        boolean userAllowed(
                        @Param("userId") Integer userId,
                        @Param("eventId") Integer eventId);

        @Query(value = """
                        SELECT e.*
                        FROM events e
                        JOIN spots s ON e.spot_id = s.id

                        LEFT JOIN spot_invitations si ON si.spot_id = s.id
                        LEFT JOIN users u ON s.owner_id = u.id
                        LEFT JOIN user_follow f1 ON f1.user_id = :userId AND f1.follower_id = u.id
                        LEFT JOIN user_follow f2 ON f2.user_id = u.id AND f2.follower_id = :userId

                        WHERE (
                            s.owner_id = 1
                            OR (s.privacy = 'PUBLIC' AND f1.user_id IS NOT NULL)
                            OR (s.privacy = 'INVITE_ONLY' AND si.user_id = :userId)
                            OR (s.privacy = 'FRIEND_ONLY' AND f1.user_id IS NOT NULL AND f2.user_id IS NOT NULL)
                        )
                        """, nativeQuery = true)
        List<Event> findEventsByUserId(@Param("userId") Integer userId);

        @Query(value = """
                        SELECT e.*
                        FROM events e
                        LEFT JOIN event_invitations ei ON ei.event_id = e.id
                        LEFT JOIN users u ON e.owner_id = u.id
                        LEFT JOIN user_follow f1 ON f1.user_id = :userId AND f1.follower_id = u.id
                        LEFT JOIN user_follow f2 ON f2.user_id = u.id AND f2.follower_id = :userId
                        WHERE e.spot_id = :spotId
                        AND (
                            e.owner_id = :userId
                            OR e.privacy = 'PUBLIC'
                            OR (e.privacy = 'INVITE_ONLY' AND ei.user_id = :userId)
                            OR (e.privacy = 'FRIEND_ONLY' AND f1.user_id IS NOT NULL AND f2.user_id IS NOT NULL)
                        )
                        """, nativeQuery = true)
        List<Event> findEventsBySpot(
                        @Param("spotId") Integer spotId,
                        @Param("userId") Integer userId);

}
