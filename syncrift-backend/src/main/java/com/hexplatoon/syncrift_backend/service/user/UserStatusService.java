package com.hexplatoon.syncrift_backend.service.user;

import com.hexplatoon.syncrift_backend.dto.user.ProfileDto;
import com.hexplatoon.syncrift_backend.dto.user.UserStatusDto;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import com.hexplatoon.syncrift_backend.service.FriendService;
import com.hexplatoon.syncrift_backend.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing user online status and broadcasting status changes.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class UserStatusService {
    private final UserRepository userRepository;
    private final FriendService friendService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Updates a user's status and notifies their friends.
     *
     * @param username the username of the user
     * @param newStatus the new status to set
     */
    public void updateUserStatus(String username, User.UserStatus newStatus) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
        
        User.UserStatus oldStatus = user.getStatus();
        if (oldStatus == newStatus) {
            return; // No change needed
        }

        user.setStatus(newStatus);
        userRepository.save(user);
        
        // Broadcast status update to all friends
        broadcastStatusUpdate(user);
    }

    /**
     * Sets a user's status to ONLINE.
     *
     * @param username the username of the user
     */
    public void setUserOnline(String username) {
        updateUserStatus(username, User.UserStatus.ONLINE);
    }

    /**
     * Sets a user's status to OFFLINE.
     *
     * @param username the username of the user
     */
    public void setUserOffline(String username) {
        updateUserStatus(username, User.UserStatus.OFFLINE);
    }

    /**
     * Sets a user's status to IN_BATTLE.
     *
     * @param username the username of the user
     */
    public void setUserInBattle(String username) {
        updateUserStatus(username, User.UserStatus.IN_BATTLE);
    }


    /**
     * Gets the current status of a user.
     *
     * @param username the username of the user
     * @return the current UserStatus
     */
    public User.UserStatus getUserStatus(String username) {
        return userRepository.findByUsername(username)
                .map(User::getStatus)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    /**
     * Broadcasts a user's status update to all their friends.
     *
     * @param user the user whose status changed
     */
    private void broadcastStatusUpdate(User user) {
        List<ProfileDto> friends = friendService.listFriends(user.getUsername());
        UserStatusDto statusUpdate = UserStatusDto.fromUser(user);
        // TODO : change the subscription channel path
        for (ProfileDto friend : friends) {
            simpMessagingTemplate.convertAndSendToUser(
                friend.getUsername(),
                "/topic/user/status",
                statusUpdate
            );

            // send notification instead of online status update
//            notificationService.createNotification(
//                    friend.getUsername(),
//                    user.getUsername(),
//                    "user_status",
//                    user.getUsername() + " is " + user.getStatus().name()
//            );
        }
    }
}
