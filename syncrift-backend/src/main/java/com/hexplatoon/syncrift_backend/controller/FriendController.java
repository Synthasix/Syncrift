package com.hexplatoon.syncrift_backend.controller;

import com.hexplatoon.syncrift_backend.dto.ApiResponse;
import com.hexplatoon.syncrift_backend.dto.friend.FriendRequestDto;
import com.hexplatoon.syncrift_backend.dto.friend.FriendStatusDto;
import com.hexplatoon.syncrift_backend.dto.user.ProfileDto;
import com.hexplatoon.syncrift_backend.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class FriendController {

    private final FriendService friendService;

    /**
     * Send a friend request to another user
     */
    @PostMapping("/request")
    public ResponseEntity<ApiResponse> sendFriendRequest(
            @Valid @RequestBody FriendRequestDto request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            friendService.sendFriendRequest(username, request.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "Friend request sent successfully"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to send friend request");
        }
    }

    /**
     * Accept a friend request
     */
    @PutMapping("/request/{requestUsername}/accept")
    public ResponseEntity<ApiResponse> acceptFriendRequest(
            @PathVariable String requestUsername,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            friendService.acceptFriendRequest(username, requestUsername);
            return ResponseEntity.ok(new ApiResponse(true, "Friend request accepted"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to accept friend request");
        }
    }

    /**
     * Decline a friend request
     */
    @DeleteMapping("/request/{requestUsername}/decline")
    public ResponseEntity<ApiResponse> declineFriendRequest(
            @PathVariable String requestUsername,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            friendService.declineFriendRequest(username, requestUsername);
            return ResponseEntity.ok(new ApiResponse(true, "Friend request declined"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to decline friend request");
        }
    }

    /**
     * Block a user
     */
    @PostMapping("/block")
    public ResponseEntity<ApiResponse> blockUser(
            @Valid @RequestBody FriendRequestDto request,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            friendService.blockUser(username, request.getUsername());
            return ResponseEntity.ok(new ApiResponse(true, "User blocked successfully"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to block user");
        }
    }

    /**
     * Remove a friend
     */
    @DeleteMapping("/{friendUsername}")
    public ResponseEntity<ApiResponse> removeFriend(
            @PathVariable String friendUsername,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            friendService.removeFriend(username, friendUsername);
            return ResponseEntity.ok(new ApiResponse(true, "Friend removed successfully"));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to remove friend");
        }
    }

    /**
     * Get friendship status with another user
     */
    @GetMapping("/status/{otherUsername}")
    public ResponseEntity<FriendStatusDto> getFriendStatus(
            @PathVariable String otherUsername,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            String status = friendService.getFriendStatusAsText(username, otherUsername);
            return ResponseEntity.ok(new FriendStatusDto(status, username, otherUsername));
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get friendship status");
        }
    }

    /**
     * List all friends of the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<ProfileDto>> getFriends(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ProfileDto> friends = friendService.listFriends(username);
            return ResponseEntity.ok(friends);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve friends list");
        }
    }

    /**
     * List all pending friend requests for the authenticated user
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ProfileDto>> getPendingRequests(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ProfileDto> pendingRequests = friendService.listPendingRequests(username);
            return ResponseEntity.ok(pendingRequests);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve pending requests");
        }
    }

    /**
     * Get mutual friends between the authenticated user and another user
     */
    @GetMapping("/mutual/{otherUsername}")
    public ResponseEntity<List<ProfileDto>> getMutualFriends(
            @PathVariable String otherUsername,
            Authentication authentication) {
        try {
            String username = authentication.getName();
            List<ProfileDto> mutualFriends = friendService.getMutualFriends(username, otherUsername);
            return ResponseEntity.ok(mutualFriends);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to retrieve mutual friends");
        }
    }
}

