package com.hexplatoon.syncrift_backend.repository;


import com.hexplatoon.syncrift_backend.entity.Friend;
import com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus;
import com.hexplatoon.syncrift_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Long> {

    /**
     * Find all friendships for a given user with a specific status
     */
    List<Friend> findByUserIdAndStatus(User userId, FriendshipStatus status);

    /**
     * Find all pending friend requests sent to a user
     */
    @Query("SELECT f FROM Friend f WHERE f.friendId = :user AND f.status = com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus.PENDING")
    List<Friend> findPendingRequestsForUser(@Param("user") User user);

    /**
     * Find a friend request between two users regardless of who sent it
     */
    @Query("SELECT f FROM Friend f WHERE (f.userId = :user1 AND f.friendId = :user2) OR (f.userId = :user2 AND f.friendId = :user1)")
    List<Friend> findFriendRequestBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Check if a friendship exists between two users with ACCEPTED status
     */
    @Query("SELECT COUNT(f) > 0 FROM Friend f WHERE f.userId = :user1 AND f.friendId = :user2 AND f.status = com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus.ACCEPTED")
    boolean existsFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Find a specific friend request from one user to another
     */
    Optional<Friend> findByUserIdAndFriendId(User userId, User friendId);

    /**
     * Update the status of a friendship
     */
    @Modifying
    @Query("UPDATE Friend f SET f.status = :status WHERE f.id = :id")
    void updateFriendshipStatus(@Param("id") Long id, @Param("status") FriendshipStatus status);

    /**
     * Delete all friendships between two users (for removing a friend)
     */
    @Modifying
    @Query("DELETE FROM Friend f WHERE (f.userId = :user1 AND f.friendId = :user2) OR (f.userId = :user2 AND f.friendId = :user1)")
    void deleteFriendshipBetweenUsers(@Param("user1") User user1, @Param("user2") User user2);

    /**
     * Count number of friends a user has
     */
    @Query("SELECT COUNT(f) FROM Friend f WHERE f.userId = :user AND f.status = com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus.ACCEPTED")
    long countFriendsByUser(@Param("user") User user);

    /**
     * Find all users who have sent a friend request to a specific user
     */
    @Query("SELECT f.userId FROM Friend f WHERE f.friendId = :user AND f.status = com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus.PENDING")
    List<User> findUsersThatSentRequestsTo(@Param("user") User user);

    /**
     * Find mutual friends between two users
     */
    @Query("SELECT f1.friendId FROM Friend f1, Friend f2 WHERE " +
            "f1.userId = :user1 AND f1.status = com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus.ACCEPTED AND " +
            "f2.userId = :user2 AND f2.status = com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus.ACCEPTED AND " +
            "f1.friendId = f2.friendId")
    List<User> findMutualFriends(@Param("user1") User user1, @Param("user2") User user2);
}

