package com.spot.app.repositories;

import com.spot.app.entities.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    @Query(value = "SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<User> searchByUsername(@Param("query") String query, Pageable pageable);

    @Query(value = """
                SELECT u.* FROM users u
                JOIN user_follow f1 ON f1.follower_id = :userId AND f1.user_id = u.id
                JOIN user_follow f2 ON f2.user_id = :userId AND f2.follower_id = u.id
            """, nativeQuery = true)
    List<User> findFriends(@Param("userId") Integer userId);

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM user_follow f1
                JOIN user_follow f2 ON f1.user_id = f2.follower_id AND f1.follower_id = f2.user_id
                WHERE f1.user_id IN (:userAId, :userBId)
                  AND f1.follower_id IN (:userAId, :userBId)
            )
            """, nativeQuery = true)
    boolean vibeCheck(
            @Param("userAId") Integer userAId,
            @Param("userBId") Integer userBId);

}