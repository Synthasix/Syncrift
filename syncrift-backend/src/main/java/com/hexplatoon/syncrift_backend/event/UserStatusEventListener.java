package com.hexplatoon.syncrift_backend.event;

import com.hexplatoon.syncrift_backend.service.user.UserStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Event listener for WebSocket session events to track user connection status.
 * Automatically updates user status based on their WebSocket connection state.
 */
@Component
@RequiredArgsConstructor
public class UserStatusEventListener {
    private final UserStatusService userStatusService;

    /**
     * Handles WebSocket connection events.
     * Sets user status to ONLINE when they connect.
     *
     * @param event the connection event
     */
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null) {
            System.out.println("User " + auth.getName() + " connected");
            String username = auth.getName();
            userStatusService.setUserOnline(username);
        }
    }

    /**
     * Handles WebSocket disconnection events.
     * Sets user status to OFFLINE when they disconnect.
     *
     * @param event the disconnection event
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Authentication auth = (Authentication) headerAccessor.getUser();
        if (auth != null) {
            String username = auth.getName();
            userStatusService.setUserOffline(username);
        }
    }
}
