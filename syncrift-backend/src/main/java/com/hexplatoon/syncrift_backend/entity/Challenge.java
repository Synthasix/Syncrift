package com.hexplatoon.syncrift_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "challenge_requests",
    indexes = {
        @Index(name = "idx_challenge_requests_sender_status", columnList = "sender_id, status"),
        @Index(name = "idx_challenge_requests_recipient_status", columnList = "recipient_id, status"),
        @Index(name = "idx_challenge_requests_expires_at", columnList = "expires_at")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Challenge {

    // TODO : Rename the dto and the entity to Challenge
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    private User recipient;

    // Not Required for now
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "battle_id")
//    private Battle battle;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChallengeStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 20)
    private EventType eventType;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public enum ChallengeStatus {
        PENDING,
        ACCEPTED,
        DECLINED,
        EXPIRED,
        CANCELLED
    }

    public enum EventType {
        CSS, TB, CF
    }
}

