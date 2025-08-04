package com.hexplatoon.syncrift_backend.service.battle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hexplatoon.syncrift_backend.dto.battle.Result;
import com.hexplatoon.syncrift_backend.dto.battle.websocket.BattleCreateWebsocketDto;
import com.hexplatoon.syncrift_backend.dto.battle.websocket.BattleResultWebsocketDto;
import com.hexplatoon.syncrift_backend.dto.battle.websocket.BattleStartWebsocketDto;
import com.hexplatoon.syncrift_backend.dto.battle.Readiness;
import com.hexplatoon.syncrift_backend.dto.battle.config.Config;
import com.hexplatoon.syncrift_backend.entity.Challenge;
import com.hexplatoon.syncrift_backend.entity.User;
import com.hexplatoon.syncrift_backend.entity.Battle;
import com.hexplatoon.syncrift_backend.dto.battle.BattleSession;
import com.hexplatoon.syncrift_backend.mapper.ProfileMapper;
import com.hexplatoon.syncrift_backend.repository.BattleRepository;
import com.hexplatoon.syncrift_backend.repository.UserRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// TODO : Refine Code

@Slf4j
@Service
public class BattleService{

    private final Map<Long, BattleSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<Long, Readiness> readinessMap = new ConcurrentHashMap<>();
    private final Map<Long, Battle> activeBattles = new ConcurrentHashMap<>();
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final BattleRepository battleRepository;
    private final BattleTimerService battleTimerService;
    private final TypingBattleHandlerService typingBattleHandlerService;
    private final CssBattleHandlerService cssBattleHandlerService;
    private final ObjectMapper objectMapper;



    @Autowired
    public BattleService(
            UserRepository userRepository,
            SimpMessagingTemplate simpMessagingTemplate,
            BattleRepository battleRepository,
            BattleTimerService battleTimerService,
            TypingBattleHandlerService typingBattleHandlerService, CssBattleHandlerService cssBattleHandlerService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.battleRepository = battleRepository;
        this.battleTimerService = battleTimerService;
        this.typingBattleHandlerService = typingBattleHandlerService;
        this.cssBattleHandlerService = cssBattleHandlerService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void createBattle(Challenge.EventType eventType, String challengerUsername, String opponentUsername) {
        User challenger = findUserByUsername(challengerUsername);
        User opponent = findUserByUsername(opponentUsername);

        // TODO : This duration will be fetched from the challenge
        int duration = 0;
        if (eventType == Challenge.EventType.TB) duration = 30;
        else if (eventType == Challenge.EventType.CSS) duration = 30;
        else if (eventType == Challenge.EventType.CF) duration = 3600;


        Battle battle = Battle.builder()
                .category(Battle.Category.valueOf(eventType.name()))
                .createdAt(LocalDateTime.now())
                .challenger(challenger)
                .opponent(opponent)
                .status(Battle.Status.WAITING)
                .duration(duration)
                .build();

        battleRepository.save(battle);

        // response to send to each of the user
        BattleCreateWebsocketDto responseDto = BattleCreateWebsocketDto.builder()
                .battleId(battle.getId())
                .category(battle.getCategory())
                .challenger(ProfileMapper.toMiniProfileDto(challenger))
                .opponent(ProfileMapper.toMiniProfileDto(opponent))
                .message("CREATED")
                .build();

        // response sent to each user using websocket
        simpMessagingTemplate.convertAndSendToUser(challengerUsername,"/topic/battle/create", responseDto);
        simpMessagingTemplate.convertAndSendToUser(opponentUsername,"/topic/battle/create", responseDto);

        // to check for both user being ready
        // remove this object from map if timer expire or both user get ready
        Readiness ready = new Readiness();
        readinessMap.put(battle.getId(), ready);

        // start a timer for checking both user being ready
        battleTimerService.startReadinessTimer(battle.getId(), 30);
    }

    @Transactional
    public void startBattle(Long battleId){
        Battle battle = findBattleById(battleId);
        validateBattle(battle, Battle.Status.WAITING);

        activeBattles.put(battle.getId(), battle);
        BattleSession session = BattleSession.builder().build();
        activeSessions.put(battle.getId(), session);

        // update user status in db
        User challenger = battle.getChallenger();
        User opponent = battle.getOpponent();
        challenger.setStatus(User.UserStatus.IN_BATTLE);
        opponent.setStatus(User.UserStatus.IN_BATTLE);
        userRepository.save(challenger);
        userRepository.save(opponent);

        // config fetch logic
        Config config = null;
        if (battle.getCategory() == Battle.Category.TB){
            config = typingBattleHandlerService.getConfig(battleId);

        }else if(battle.getCategory() == Battle.Category.CSS){
            config = cssBattleHandlerService.getConfig(battleId);
        }
        try {
            battle.setConfigJson(objectMapper.writeValueAsString(config));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing config", e);
        } catch (AssertionError e) {
            throw new RuntimeException("No battle running with id " + battleId, e);
        }

        //When both are ready
        BattleStartWebsocketDto dto = BattleStartWebsocketDto.builder()
                .battleId(battle.getId())
                .config(config)
                .category(battle.getCategory())
                .build();

        // send data to users with websocket
        simpMessagingTemplate.convertAndSendToUser(challenger.getUsername(),"/topic/battle/start", dto);
        simpMessagingTemplate.convertAndSendToUser(opponent.getUsername(),"/topic/battle/start", dto);
        System.out.println("Battle started with config:" + config.toString());
        battle.setStatus(Battle.Status.ONGOING);
        battle.setStartedAt(LocalDateTime.now());
        battleRepository.save(battle);

        // Start timer
        battleTimerService.startBattleTimer(battleId, battle.getDuration() +
                (battle.getCategory() == Battle.Category.TB ? 5 : 0));

//        activeSessions.put(battleId, session);
    }

    @Transactional
    public void endBattle(Long battleId) {
        Battle battle = getActiveBattleById(battleId);
        validateBattle(battle, Battle.Status.ONGOING);

        // update ending time in the battle
        battle.setUpdatedAt(LocalDateTime.now());

        // update user status
        User challenger = battle.getChallenger();
        User opponent = battle.getOpponent();
        challenger.setStatus(User.UserStatus.ONLINE);
        userRepository.save(challenger);
        opponent.setStatus(User.UserStatus.ONLINE);
        userRepository.save(opponent);

        // result config fetch logic
        Result result = null;
        if (battle.getCategory() == Battle.Category.TB){
            result = typingBattleHandlerService.getResult(battleId);
        }else if(battle.getCategory() == Battle.Category.CSS){
            try {
                result = cssBattleHandlerService.getResult(battleId);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if (result == null) {
            throw new RuntimeException("Error fetching result");
        }

        // save result in db
        try {
            battle.setResultJson(objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing result", e);
        }

        // BattleResultWebsocketDto
        BattleResultWebsocketDto dto = BattleResultWebsocketDto.builder()
                .result(result)
                .battleId(battle.getId())
                .category(battle.getCategory())
                .build();
        // send to both the users
        simpMessagingTemplate.convertAndSendToUser(opponent.getUsername(),"/topic/battle/end", dto);
        simpMessagingTemplate.convertAndSendToUser(challenger.getUsername(),"/topic/battle/end", dto);

        // remove active battle and session
        // update battle status and winnerUsername
        battle.setStatus(Battle.Status.ENDED);
        battle.setWinnerUsername(result.getWinnerUsername());
        BattleSession session = activeSessions.remove(battleId);
        Battle removedBattle = activeBattles.remove(battleId);

        if (removedBattle != null && session != null) {
            battleRepository.save(removedBattle);
        }else{
            throw new RuntimeException("Error delete active session or battle");
        }
    }

    @Transactional
    public void cancelBattle(Long battleId) {
        Battle battle = findBattleById(battleId);

        battle.setStatus(Battle.Status.CANCELED);
        battleRepository.save(battle);

        readinessMap.remove(battleId);

        System.out.println("Battle " + battleId + " cancelled.");
    }


    // TODO : create get challenger username and get opponent username
    @Transactional
    public void updateReadiness(@NotNull String username , Long battleId) {

        Readiness ready = readinessMap.get(battleId);
        Battle battle = findBattleById(battleId);


        String challengerUsername = battle.getChallenger().getUsername();
        String opponentUsername =  battle.getOpponent().getUsername();

        validateBattle(battle, Battle.Status.WAITING);

        if(username.equals(challengerUsername) && !readinessMap.get(battleId).isChallengerOk()) {
            ready.setChallengerOk(true);
        }
        else if(username.equals(opponentUsername) && !readinessMap.get(battleId).isOpponentOk()) {
            ready.setOpponentOk(true);
        }
        else {
            throw new UsernameNotFoundException("Username " + username + " not found Or Battle already started.");
        }

        if (isBothReady(battleId)) {
            readinessMap.remove(battleId);
            battleTimerService.cancelReadinessTimer(battleId);
            startBattle(battleId);
        }
    }

    public boolean isBothReady(Long battleId) {
        Readiness ready = readinessMap.get(battleId);
        return ready!=null && ready.isChallengerOk()&& ready.isOpponentOk();
    }

    public Battle findBattleById(Long battleId) {
        return battleRepository.findById(battleId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Battle not found : " + battleId));
    }
    public Battle getActiveBattleById(Long battleId) {
        return activeBattles.get(battleId);
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + username));
    }

    private void validateBattle(Battle battle, Battle.Status expectedStatus) {
        if (battle.getStatus() != expectedStatus) {
            throw new RuntimeException("Expected " + expectedStatus.name() +
                    " but got " + battle.getStatus().name());
        }
    }
}
