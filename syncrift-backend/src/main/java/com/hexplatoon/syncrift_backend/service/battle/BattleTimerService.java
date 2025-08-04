package com.hexplatoon.syncrift_backend.service.battle;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

// TODO : Refine the code. It's smelling
// TODO : Add proper exception handling for websocket and write fail safe checks in all necessary places
@Service
public class BattleTimerService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final Map<Long, ScheduledFuture<?>> battleTimers = new ConcurrentHashMap<>();
    private final Map<Long, ScheduledFuture<?>> readinessTimers = new ConcurrentHashMap<>();
    private final BattleService battleService;

    public BattleTimerService(@Lazy BattleService battleService) {
        this.battleService = battleService;
    }

    public void startBattleTimer(Long battleId, int durationSeconds) {
        // Avoid duplicate timers
        if (battleTimers.containsKey(battleId)) return;

        ScheduledFuture<?> future = scheduler.schedule(() -> {
            System.out.println("battle ended.");
            battleService.endBattle(battleId);
            battleTimers.remove(battleId);
        }, durationSeconds, TimeUnit.SECONDS);

        battleTimers.put(battleId, future);
    }

    public void startReadinessTimer(Long battleId, int durationSeconds) {
        if (readinessTimers.containsKey(battleId)) return;
        ScheduledFuture<?> future = scheduler.schedule(() -> {;
            if (!battleService.isBothReady(battleId)){
                battleService.cancelBattle(battleId);
            }
            readinessTimers.remove(battleId);
        }, durationSeconds, TimeUnit.SECONDS);

        readinessTimers.put(battleId, future);
    }

    public void cancelBattleTimer(Long battleId) {
        ScheduledFuture<?> future = battleTimers.remove(battleId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public void cancelReadinessTimer(Long battleId) {
        ScheduledFuture<?> future = readinessTimers.remove(battleId);
        if (future != null) {
            future.cancel(true);
        }
    }

    public boolean isBattleRunning(Long battleId) {
        return battleTimers.containsKey(battleId);
    }

//    public void forceEndBattleTimer(Long battleId) {
//        cancelBattleTimer(battleId);
//        battleService.endBattle(battleId);
//    }
}
