package com.hexplatoon.syncrift_backend.dto.user;

import com.hexplatoon.syncrift_backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for representing user status information.
 * Used for sending status updates through WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusDto {
    private Long userId;
    private String username;
    private User.UserStatus status;

    /**
     * Creates a UserStatusDto from a User entity.
     *
     * @param user the User entity
     * @return a new UserStatusDto with user's status information
     */
    public static UserStatusDto fromUser(User user) {
        return UserStatusDto.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .status(user.getStatus())
                .build();
    }
}
