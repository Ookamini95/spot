package com.spot.app.repositories;

import com.spot.app.entities.Spot;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpotRepository extends JpaRepository<Spot, Integer> {
        List<Spot> findByOwnerId(Integer ownerId);
        List<Spot> findByOwnerIdIn(List<Integer> ownerIds);

        /* === Custom queries === */
        @Query(value = """
                        SELECT DISTINCT s.*
                        FROM spots s

                        LEFT JOIN spot_invitations si ON si.spot_id = s.id

                        LEFT JOIN users u ON s.owner_id = u.id

                        LEFT JOIN user_follow f1 ON f1.user_id = :userId AND f1.follower_id = u.id
                        LEFT JOIN user_follow f2 ON f2.user_id = u.id AND f2.follower_id = :userId

                        WHERE (
                            s.owner_id = 1
                            OR s.owner_id = :userId
                            OR (s.privacy = 'PUBLIC' AND f1.user_id IS NOT NULL)
                            OR (s.privacy = 'INVITE_ONLY' AND si.user_id = :userId)
                            OR (s.privacy = 'FRIEND_ONLY' AND f1.user_id IS NOT NULL AND f2.user_id IS NOT NULL)
                        )
                        AND ST_DWithin(
                            CAST(s.position AS geography),
                            CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography),
                            :meters
                        )
                        """, nativeQuery = true)
        List<Spot> findAllSpotsWithinRange(
                        @Param("userId") Integer userId,
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("meters") double meters);

        @Query(value = """
                        SELECT s.*
                        FROM spots s

                        LEFT JOIN spot_invitations si ON si.spot_id = s.id
                        LEFT JOIN users u ON s.owner_id = u.id
                        LEFT JOIN user_follow f1 ON f1.user_id = :userId AND f1.follower_id = u.id
                        LEFT JOIN user_follow f2 ON f2.user_id = u.id AND f2.follower_id = :userId

                        WHERE (
                            s.owner_id = 1
                            OR s.owner_id = :userId
                            OR (s.privacy = 'PUBLIC' AND f1.user_id IS NOT NULL)
                            OR (s.privacy = 'INVITE_ONLY' AND si.user_id = :userId)
                            OR (s.privacy = 'FRIEND_ONLY' AND f1.user_id IS NOT NULL AND f2.user_id IS NOT NULL)
                        )
                        AND ST_DWithin(
                            CAST(s.position AS geography),
                            CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography),
                            :meters
                        )
                        ORDER BY s.position <-> ST_SetSRID(ST_MakePoint(:lng, :lat), 4326)
                        LIMIT 1
                        """, nativeQuery = true)
        Spot findNearestSpotWithinRange(
                        @Param("userId") Integer userId,
                        @Param("lat") double lat,
                        @Param("lng") double lng,
                        @Param("meters") double meters);

        @Query(value = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM spot_invitations
                            WHERE spot_id = :spotId AND user_id = :userId
                        )
                        """, nativeQuery = true)
        boolean vibeCheck(
                        @Param("userId") Integer userId,
                        @Param("spotId") Integer spotId);
        // ^ Checks if user invited to spot

        @Query(value = """
                        SELECT EXISTS (
                            SELECT 1
                            FROM spots s

                            LEFT JOIN spot_invitations si ON si.spot_id = s.id
                            LEFT JOIN users u ON s.owner_id = u.id
                            LEFT JOIN user_follow f1 ON f1.user_id = :userId AND f1.follower_id = u.id
                            LEFT JOIN user_follow f2 ON f2.user_id = u.id AND f2.follower_id = :userId

                            WHERE s.id = :spotId
                            AND (
                                s.owner_id = :userId
                                OR s.owner_id = 1
                                OR (s.privacy = 'PUBLIC')
                                OR (s.privacy = 'INVITE_ONLY' AND si.user_id = :userId)
                                OR (s.privacy = 'FRIEND_ONLY' AND f1.user_id IS NOT NULL AND f2.user_id IS NOT NULL)
                            )
                        )
                        """, nativeQuery = true)
        boolean userAllowed(
                        @Param("userId") Integer userId,
                        @Param("spotId") Integer spotId);

        @Query(value = """
                        SELECT DISTINCT s.*
                        FROM spots s
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
                        AND s.name ILIKE CONCAT('%', :query, '%')
                        ORDER BY s.name ASC
                        LIMIT :limit OFFSET :offset
                        """, nativeQuery = true)
        List<Spot> searchSpots(
                        @Param("userId") Integer userId,
                        @Param("query") String query,
                        @Param("limit") int limit,
                        @Param("offset") int offset);

        @Query(value = """
                        SELECT DISTINCT s.*
                        FROM spots s
                        LEFT JOIN spot_followers sf ON sf.spot_id = s.id
                        WHERE s.owner_id = :userId OR sf.user_id = :userId
                        """, nativeQuery = true)
        List<Spot> findFollowedSpots(@Param("userId") Integer userId);

}
