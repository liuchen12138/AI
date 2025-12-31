package com.gomoku.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_move")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameMove {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "move_id")
    private Long moveId;
    
    @Column(name = "game_id", nullable = false)
    private Long gameId;
    
    @Column(name = "move_number", nullable = false)
    private Integer moveNumber;
    
    @Column(name = "player_id", nullable = false)
    private Long playerId;
    
    @Column(name = "position_x", nullable = false)
    private Integer positionX;
    
    @Column(name = "position_y", nullable = false)
    private Integer positionY;
    
    @Column(name = "move_time", nullable = false)
    private LocalDateTime moveTime;
    
    @Column(name = "time_used")
    private Integer timeUsed;
    
    @PrePersist
    protected void onCreate() {
        if (moveTime == null) {
            moveTime = LocalDateTime.now();
        }
    }
}
