package com.hexplatoon.syncrift_backend.service;

import com.hexplatoon.syncrift_backend.dto.user.ProfileDto;
import com.hexplatoon.syncrift_backend.entity.Friend;
import com.hexplatoon.syncrift_backend.entity.Friend.FriendshipStatus;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.mapper.ProfileMapper;
import com.hexplatoon.syncrift_backend.repository.FriendRepository;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@Transactional
@RequiredArgsConstructor
public class


FriendService {

    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    /**
     * Sends a friend request from one user to another.
     *
     * @param currentUsername The username of the user sending the request
     * @param targetUsername  The username of the user receiving the request
     */
    public void sendFriendRequest(@NotBlank String currentUsername, @NotBlank String targetUsername) {
        if (currentUsername.equals(targetUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot send a friend request to yourself");
        }

        User currentUser = findUserByUsername(currentUsername);
        User targetUser = findUserByUsername(targetUsername);

        // Check if any friendship record already exists
        // Query might return multiple records, we just need to check if any exist
        List<Friend> existingFriendships = friendRepository.findFriendRequestBetweenUsers(currentUser, targetUser);
        Optional<Friend> existingFriendship = existingFriendships.isEmpty() ? Optional.empty() : Optional.of(existingFriendships.get(0));

        if (existingFriendship.isPresent()) {
            Friend friendship = existingFriendship.get();
            FriendshipStatus status = friendship.getStatus();

            if (currentUser.equals(friendship.getUserId()) && status == FriendshipStatus.PENDING) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Friend request already sent");
            } else if (targetUser.equals(friendship.getUserId()) && status == FriendshipStatus.PENDING) {
                acceptFriendRequest(currentUsername, targetUsername);
                return;
            } else if (status == FriendshipStatus.ACCEPTED) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "You are already friends with this user");
            } else if (status == FriendshipStatus.BLOCKED) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Unable to send friend request");
            }
        }

        // Create new friend request
        Friend friendRequest = Friend.builder()
                .userId(currentUser)
                .friendId(targetUser)
                .status(FriendshipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        friendRepository.save(friendRequest);
        // Send notification to target user

    }

    /**
     * Accepts a pending friend request.
     *
     * @param recipientUsername The username of the user accepting the request
     * @param senderUsername  The username of the user who sent the request
     * @return A message indicating the result of the operation
     */
    public String acceptFriendRequest(@NotBlank String recipientUsername, @NotBlank String senderUsername) {
        User currentUser = findUserByUsername(recipientUsername);
        User sender = findUserByUsername(senderUsername);

        // Find the pending request
        Friend pendingRequest = friendRepository.findByUserIdAndFriendId(sender, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending friend request found"));

        if (pendingRequest.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend request is not in pending status");
        }

        // Update the request status to ACCEPTED
        pendingRequest.setStatus(FriendshipStatus.ACCEPTED);
        friendRepository.save(pendingRequest);

        // Create a reciprocal friendship record for bidirectional relationship
        // First check if it doesn't already exist
        if (!friendRepository.findByUserIdAndFriendId(currentUser, sender).isPresent()) {
            Friend reciprocalFriendship = Friend.builder()
                    .userId(currentUser)
                    .friendId(sender)
                    .status(FriendshipStatus.ACCEPTED)
                    .createdAt(LocalDateTime.now())
                    .build();
            friendRepository.save(reciprocalFriendship);
        }

        // Send notification to the sender

        return "Friend request accepted";
    }

    /**
     * Declines a pending friend request.
     *
     * @param currentUsername The username of the user declining the request
     * @param senderUsername  The username of the user who sent the request
     * @return A message indicating the result of the operation
     */
    // TODO : change return type to void
    public String declineFriendRequest(@NotBlank String currentUsername, @NotBlank String senderUsername) {
        User currentUser = findUserByUsername(currentUsername);
        User sender = findUserByUsername(senderUsername);

        Friend pendingRequest = friendRepository.findByUserIdAndFriendId(sender, currentUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending friend request found"));

        if (pendingRequest.getStatus() != FriendshipStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Friend request is not in pending status");
        }

        // Send notification to the sender

        friendRepository.delete(pendingRequest);
        return "Friend request declined";
    }

    /**
     * Blocks a user.
     *
     * @param currentUsername The username of the user performing the block
     * @param targetUsername  The username of the user being blocked
     * @return A message indicating the result of the operation
     */
    public String blockUser(@NotBlank String currentUsername, @NotBlank String targetUsername) {
        User currentUser = findUserByUsername(currentUsername);
        User targetUser = findUserByUsername(targetUsername);

        // Remove any existing friendship records in both directions
        friendRepository.deleteFriendshipBetweenUsers(currentUser, targetUser);

        // Create a block record
        Friend blockRecord = Friend.builder()
                .userId(currentUser)
                .friendId(targetUser)
                .status(FriendshipStatus.BLOCKED)
                .createdAt(LocalDateTime.now())
                .build();

        friendRepository.save(blockRecord);
        return "User blocked successfully";
    }

    /**
     * Removes a friend relationship between two users.
     *
     * @param currentUsername The username of the current user
     * @param friendUsername  The username of the friend to remove
     * @return A message indicating the result of the operation
     */
    public String removeFriend(@NotBlank String currentUsername, @NotBlank String friendUsername) {
        User currentUser = findUserByUsername(currentUsername);
        User friendUser = findUserByUsername(friendUsername);

        // Check if they are friends
        boolean areFriends = friendRepository.existsFriendshipBetweenUsers(currentUser, friendUser);

        if (!areFriends) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend relationship not found");
        }

        // Delete friend records in both directions
        friendRepository.deleteFriendshipBetweenUsers(currentUser, friendUser);

        return "Friend removed successfully";
    }

    /**
     * Gets the friendship status between two users.
     *
     * @param currentUsername The username of the current user
     * @param otherUsername   The username of the other user
     * @return The status of the friendship
     */
    public String getFriendStatusAsText(@NotBlank String currentUsername, @NotBlank String otherUsername) {
        User currentUser = findUserByUsername(currentUsername);
        User otherUser = findUserByUsername(otherUsername);

        List<Friend> friendships = friendRepository.findFriendRequestBetweenUsers(currentUser, otherUser);
        Optional<Friend> friendship = friendships.isEmpty() ? Optional.empty() : Optional.of(friendships.get(0));

        if (friendship.isEmpty()) {
            return "NOT_FRIENDS";
        }

        Friend friend = friendship.get();
        FriendshipStatus status = friend.getStatus();

        if (status == FriendshipStatus.ACCEPTED) {
            return "FRIENDS";
        } else if (status == FriendshipStatus.PENDING) {
            if (currentUser.equals(friend.getUserId())) {
                return "REQUEST_SENT";
            } else {
                return "REQUEST_RECEIVED";
            }
        } else if (status == FriendshipStatus.BLOCKED) {
            if (currentUser.equals(friend.getUserId())) {
                return "BLOCKED";
            } else {
                return "NOT_FRIENDS"; // Don't expose that the user is blocked
            }
        }

        return "NOT_FRIENDS";
    }

    public FriendshipStatus getFriendStatus(@NotBlank String currentUsername, @NotBlank String otherUsername) {
        User currentUser = findUserByUsername(currentUsername);
        User otherUser = findUserByUsername(otherUsername);
        List<Friend> friendships = friendRepository.findFriendRequestBetweenUsers(currentUser, otherUser);
        Optional<Friend> friendship = friendships.isEmpty() ? Optional.empty() : Optional.of(friendships.get(0));

        if (friendship.isEmpty()) {
            return FriendshipStatus.UNRELATED;
        }

        return friendship.get().getStatus();
    }

    /**
     * Lists all friends of a user.
     *
     * @param username The username of the user
     * @return A list of ProfileDto objects representing the user's friends
     */
    public List<ProfileDto> listFriends(@NotBlank String username) {
        User user = findUserByUsername(username);
        List<User> friends = friendRepository.findByUserIdAndStatus(user, FriendshipStatus.ACCEPTED).stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());

        return convertUsersToProfileDtos(friends);
    }

    /**
     * Lists all pending friend requests for a user.
     *
     * @param username The username of the user
     * @return A list of ProfileDto objects representing users who sent friend requests
     */
    public List<ProfileDto> listPendingRequests(@NotBlank String username) {
        User user = findUserByUsername(username);
        List<User> requestSenders = friendRepository.findPendingRequestsForUser(user).stream()
                .map(Friend::getUserId)
                .collect(Collectors.toList());

        return convertUsersToProfileDtos(requestSenders);
    }

    /**
     * Gets a list of mutual friends between two users.
     *
     * @param username1 The username of the first user
     * @param username2 The username of the second user
     * @return A list of ProfileDto objects representing mutual friends
     */
    public List<ProfileDto> getMutualFriends(@NotBlank String username1, @NotBlank String username2) {
        User user1 = findUserByUsername(username1);
        User user2 = findUserByUsername(username2);

        List<User> friends1 = friendRepository.findByUserIdAndStatus(user1, FriendshipStatus.ACCEPTED).stream()
                .map(Friend::getFriendId)
                .toList();

        List<User> friends2 = friendRepository.findByUserIdAndStatus(user2, FriendshipStatus.ACCEPTED).stream()
                .map(Friend::getFriendId)
                .collect(Collectors.toList());

        // Find the intersection of the two friend lists
        List<User> mutualFriends = new ArrayList<>(friends1);
        mutualFriends.retainAll(friends2);

        return convertUsersToProfileDtos(mutualFriends);
    }

    /**
     * Helper method to find a user by username.
     *
     * @param username The username to search for
     * @return The found user
     * @throws ResponseStatusException if the user is not found
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }

    /**
     * Helper method to convert a list of User objects to ProfileDto objects.
     *
     * @param users The list of users to convert
     * @return A list of ProfileDto objects
     */
    private List<ProfileDto> convertUsersToProfileDtos(List<User> users) {
        return users.stream()
                .map(ProfileMapper::toProfileDto)
                .collect(Collectors.toList());
    }
}