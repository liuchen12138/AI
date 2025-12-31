package com.gomoku.entity;

import com.gomoku.enums.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Game {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_id")
    private Long gameId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "game_mode", nullable = false, length = 20)
    private GameMode gameMode;
    
    @Column(name = "black_player_id", nullable = false)
    private Long blackPlayerId;
    
    @Column(name = "white_player_id")
    private Long whitePlayerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "ai_difficulty", length = 20)
    private AIDifficulty aiDifficulty;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GameStatus status;
    
    @Column(name = "winner_id")
    private Long winnerId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "end_reason", length = 50)
    private EndReason endReason;
    
    @Column(name = "black_score_change")
    private Integer blackScoreChange;
    
    @Column(name = "white_score_change")
    private Integer whiteScoreChange;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "ended_at")
    private LocalDateTime endedAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) {
            status = GameStatus.WAITING;
        }
    }
}
