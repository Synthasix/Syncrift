package com.hexplatoon.syncrift_backend.service;

import com.hexplatoon.syncrift_backend.dto.challenge.ChallengeDto;
import com.hexplatoon.syncrift_backend.entity.Challenge;
import com.hexplatoon.syncrift_backend.entity.Challenge.ChallengeStatus;
import com.hexplatoon.syncrift_backend.entity.Friend;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.repository.ChallengeRepository;
import com.hexplatoon.syncrift_backend.repository.FriendRepository;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Validated
@RequiredArgsConstructor
public class ChallengeService {

    // TODO : Use friend service in place of friend repository
    private final ChallengeRepository challengeRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FriendService friendService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    /**
     * Create a new challenge request from one user to another
     * @param recipientUsername Username of the recipient
     * @param eventType Type of event (e.g., "typing", "css", "codeforces")
     * @return The created challenge request as DTO
     */
    @Transactional
    public ChallengeDto createChallenge(@NotBlank String senderUsername,
                                        @NotBlank String recipientUsername,
                                        Challenge.EventType eventType) {
        if (senderUsername.equals(recipientUsername)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot challenge yourself");
        }

        User sender = findUserByUsername(senderUsername);
        User recipient = findUserByUsername(recipientUsername);

        // Validate that users are friends
        validateFriendship(sender, recipient);

        // Check if there's already a pending challenge
        // TODO : change the check in pendingchallengebwtusers for type
        Optional<Challenge> existingChallenge =
            challengeRepository.findPendingChallengeBetweenUsers(sender, recipient);
        
        if (existingChallenge.isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A pending challenge already exists");
        }

        // Create expiry time (60 seconds from now)
        LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(60);

        // Create new challenge
        Challenge challenge = Challenge.builder()
                .sender(sender)
                .recipient(recipient)
                .status(ChallengeStatus.PENDING)
                .eventType(eventType)
                .expiresAt(expiresAt)
                .build();

        Challenge savedChallenge = challengeRepository.save(challenge);
        
        // Notify recipient about the challenge
//        notificationService.createNotification(
//                recipientUsername,
//                senderUsername,
//                "challenge_sent",
//                senderUsername + "have sent you a " + eventType + " challenge"
//        );
//
        ChallengeDto dto = convertToDto(savedChallenge);
        simpMessagingTemplate.convertAndSendToUser(recipientUsername,
                "/topic/challenge", dto);

        return dto;
    }

    // Not working for now
    /**
     * Accept a challenge and create a battle
     * @param recipientUsername Username of the recipient accepting the challenge
     * @param challengeId ID of the challenge to accept
     * @return The challenge request as DTO with updated status
     */
    @Transactional
    public void acceptChallenge(@NotBlank String recipientUsername, Long challengeId) {
        User recipient = findUserByUsername(recipientUsername);

        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));

        User sender = challenge.getSender();
        // Validate challenge status and recipient
        validateChallengeStatus(challenge, recipient, ChallengeStatus.PENDING);

        // Update challenge status
        challenge.setStatus(ChallengeStatus.ACCEPTED);
    }

    /**
     * Decline a challenge
     * @param recipientUsername Username of the recipient declining the challenge
     * @param challengeId ID of the challenge to decline
     * @return The challenge request as DTO with updated status
     */
    @Transactional
    public ChallengeDto declineChallenge(@NotBlank String recipientUsername, Long challengeId) {
        User recipient = findUserByUsername(recipientUsername);
        
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        
        // Validate challenge status and recipient
        validateChallengeStatus(challenge, recipient, ChallengeStatus.PENDING);
        
        // Update challenge status
        challenge.setStatus(ChallengeStatus.DECLINED);
        Challenge updatedChallenge = challengeRepository.save(challenge);
        
        // Notify users about decline
        String senderUsername = updatedChallenge.getSender().getUsername();
        System.out.println("Sender: " + senderUsername + " Reciept: " + recipientUsername);
        notificationService.createNotification(
                senderUsername,
                recipientUsername,
                "challenge_declined",
                recipientUsername + " have declined the challenge."
        );
        
        return convertToDto(updatedChallenge);
    }

    //Not required for now
//    /**
//     * Cancel a pending challenge
//     * @param senderUsername Username of the sender cancelling the challenge
//     * @param challengeId ID of the challenge to cancel
//     * @return The challenge request as DTO with updated status
//     */
//    @Transactional
//    public ChallengeRequestDto cancelChallenge(@NotBlank String senderUsername, Long challengeId) {
//        User sender = findUserByUsername(senderUsername);
//
//        ChallengeRequest challenge = challengeRequestRepository.findById(challengeId)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
//
//        // Validate challenge owner and status
//        if (!challenge.getSender().equals(sender)) {
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only cancel your own challenges");
//        }
//
//        if (challenge.getStatus() != ChallengeStatus.PENDING) {
//            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending challenges can be cancelled");
//        }
//
//        // Update challenge status
//        challenge.setStatus(ChallengeStatus.CANCELLED);
//        ChallengeRequest updatedChallenge = challengeRequestRepository.save(challenge);
//
//        return convertToDto(updatedChallenge);
//    }

    /**
     * Get all pending challenges received by a user
     * @param username Username of the user
     * @return List of challenge request DTOs
     */
    @Transactional(readOnly = true)
    public List<ChallengeDto> getPendingChallengesForUser(@NotBlank String username) {
        User user = findUserByUsername(username);
        List<Challenge> challenges = challengeRepository.findByRecipientAndStatus(user, ChallengeStatus.PENDING);
        return challenges.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get all pending challenges sent by a user
     * @param username Username of the user
     * @return List of challenge request DTOs
     */
    @Transactional(readOnly = true)
    public List<ChallengeDto> getPendingChallengesSentByUser(@NotBlank String username) {
        User user = findUserByUsername(username);
        List<Challenge> challenges = challengeRepository.findBySenderAndStatus(user, ChallengeStatus.PENDING);
        return challenges.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get challenge details by ID
     * @param challengeId ID of the challenge
     * @return The challenge request as DTO
     */
    @Transactional(readOnly = true)
    public ChallengeDto getChallengeById(Long challengeId) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Challenge not found"));
        return convertToDto(challenge);
    }

    /**
     * Scheduled task to handle expired challenges
     * Runs every 10 seconds
     */
    @Scheduled(fixedRate = 10000)
    @Transactional
    public void handleExpiredChallenges() {
        LocalDateTime now = LocalDateTime.now();
        List<Challenge> expiredChallenges = challengeRepository.findExpiredChallenges(now);
        
        for (Challenge challenge : expiredChallenges) {
            challenge.setStatus(ChallengeStatus.EXPIRED);
            challengeRepository.save(challenge);
        }
    }

    /**
     * Validate that two users are friends
     */
    private void validateFriendship(User user1, User user2) {
        boolean areFriends =
                friendService.getFriendStatus(user1.getUsername(), user2.getUsername()) == Friend.FriendshipStatus.ACCEPTED;
        if (!areFriends) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only challenge friends");
        }
    }

    /**
     * Validate challenge status and recipient
     */
    private void validateChallengeStatus(Challenge challenge, User recipient, ChallengeStatus expectedStatus) {
        if (!challenge.getRecipient().equals(recipient)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This challenge is not for you");
        }
        
        if (challenge.getStatus() != expectedStatus) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Challenge is not in " + expectedStatus + " status");
        }
        
        if (expectedStatus == ChallengeStatus.PENDING && challenge.getExpiresAt().isBefore(LocalDateTime.now())) {
            // Auto-expire the challenge if it's past the expiration time
            challenge.setStatus(ChallengeStatus.EXPIRED);
            challengeRepository.save(challenge);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge has expired");
        }
    }

    // TODO : Move this to Global Mapper
    /**
     * Convert challenge entity to DTO
     */
    private ChallengeDto convertToDto(Challenge challenge) {
        Long timeRemaining = null;
        if (challenge.getStatus() == ChallengeStatus.PENDING) {
            timeRemaining = Math.max(0, Duration.between(LocalDateTime.now(), challenge.getExpiresAt()).getSeconds());
        }
        
        return ChallengeDto.builder()
                .challengeId(challenge.getId())
                .senderUsername(challenge.getSender().getUsername())
                .recipientUsername(challenge.getRecipient().getUsername())
//                .battleId(challenge.getBattle() != null ? challenge.getBattle().getId() : null)
                .status(challenge.getStatus())
                .eventType(challenge.getEventType())
                .createdAt(challenge.getCreatedAt())
                .expiresAt(challenge.getExpiresAt())
                .timeRemainingSeconds(timeRemaining)
                .build();
    }

    /**
     * Find user by username
     */
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }
}

