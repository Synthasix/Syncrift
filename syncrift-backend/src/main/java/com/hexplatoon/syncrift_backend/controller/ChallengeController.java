package com.hexplatoon.syncrift_backend.controller;

import com.hexplatoon.syncrift_backend.dto.challenge.ChallengeCreateRequestDto;
import com.hexplatoon.syncrift_backend.dto.challenge.ChallengeDto;
import com.hexplatoon.syncrift_backend.service.ChallengeService;
import com.hexplatoon.syncrift_backend.service.user.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    // TODO : remove all the principle objects from each method
    private final ChallengeService challengeService;
    private final CurrentUserService currentUserService;

    /**
     * Endpoint to create a challenge request in backend
     * @param challengeCreateRequestDto
     * @return ChallengeRequestDto
     */
    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ChallengeDto createChallenge(@RequestBody ChallengeCreateRequestDto challengeCreateRequestDto) {
        return challengeService.createChallenge(
                currentUserService.getCurrentUsername(),
                challengeCreateRequestDto.getUsername(),
                challengeCreateRequestDto.getEventType());
    }

    /**
     * Endpoint to accept a challenge received by a user
     * @param principal
     * @param id challenge id
     * @return ChallengeRequestDto
     */
    @PostMapping("/{id}/accept")
    @ResponseStatus(HttpStatus.CREATED)
    public void acceptChallenge(@PathVariable Long id) {
        challengeService.acceptChallenge(currentUserService.getCurrentUsername(), id);
    }
    /**
     * Endpoint to decline a challenge received by a user
     * @param id
     * @return
     */

    // TODO : ChallengeRequestDto response can be replaced with text response
    @PostMapping("/{id}/decline")
    public ChallengeDto declineChallenge(@PathVariable Long id) {

        return challengeService.declineChallenge(
                currentUserService.getCurrentUsername(), id);
    }

    // Not Required for now
//    @PostMapping("/{id}/cancel")
//    public ChallengeRequestDto cancelChallenge(
//            @AuthenticationPrincipal Principal principal,
//            @PathVariable Long id) {
//        return challengeRequestService.cancelChallenge(principal.getName(), id);
//    }

    @GetMapping("/pending")
    public List<ChallengeDto> getPendingChallenges() {
        return challengeService.getPendingChallengesForUser(currentUserService.getCurrentUsername());
    }

    @GetMapping("/sent")
    public List<ChallengeDto> getSentChallenges() {
        return challengeService.getPendingChallengesSentByUser(currentUserService.getCurrentUsername());
    }

    @GetMapping("/{id}")
    public ChallengeDto getChallengeById(@PathVariable Long id) {
        return challengeService.getChallengeById(id);
    }

    // Not Required
//    @MessageMapping("/challenge/request")
//    public void handleChallengeRequest(@Payload WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor) {
//        String username = headerAccessor.getUser().getName();
//
//        if (!(message.getContent() instanceof ChallengeCreateDto challenge)) {
//            throw new IllegalArgumentException("Invalid payload: Expected ChallengeCreateDto");
//        }
//
//        challengeRequestService.createChallenge(username, challenge.getUsername(), challenge.getEventType());
//    }

    // Not Required
//    @MessageMapping("/challenge/response")
//    public void handleChallengeResponse(@Payload WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor) {
//        String username = headerAccessor.getUser().getName();
//
//        if (!(message.getContent() instanceof ChallengeRequestDto challenge)) {
//            throw new IllegalArgumentException("Invalid payload: Expected ChallengeRequestDto");
//        }
//
//        switch (message.getType()) {
//            case "ACCEPT":
//                challengeRequestService.acceptChallenge(username, challenge.getId());
//                break;
//            case "DECLINE":
//                challengeRequestService.declineChallenge(username, challenge.getId());
//                break;
//            default:
//                throw new IllegalArgumentException("Unsupported challenge response type: " + message.getType());
//        }
//    }

    // Not Required
//    @MessageMapping("/challenge/cancel")
//    public void handleChallengeCancel(@Payload WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor) {
//        String username = headerAccessor.getUser().getName();
//
//        if (!(message.getContent() instanceof Long)) {
//            throw new IllegalArgumentException("Invalid payload: Expected Long (challengeId)");
//        }
//
//        Long challengeId = (Long) message.getContent();
//        challengeRequestService.cancelChallenge(username, challengeId);
//    }
}
