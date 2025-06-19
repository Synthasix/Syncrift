package com.hexplatoon.syncrift_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "battles", indexes = {
        @Index(name = "idx_challenger", columnList = "challenger"),
        @Index(name = "idx_opponent", columnList = "opponent"),
        @Index(name = "idx_status", columnList = "status"),
        @Index(name = "idx_category", columnList = "category")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Battle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenger", nullable = false)
    private User challenger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opponent", nullable = false)
    private User opponent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Lob
    @Column(name = "result_json", columnDefinition = "TEXT")
    private String resultJson;

    @Lob
    @Column(name = "config_json", columnDefinition = "TEXT")
    private String configJson;

    @Column(name = "winner_username")
    private String winnerUsername;

    @Column(name = "duration", nullable = false)
    private Integer duration;

    public enum Category {
        CSS, TB, CF
    }

    public enum Status {
        ONGOING, CANCELED, ENDED, WAITING
    }
}

