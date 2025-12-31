package com.gomoku.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;
    
    @Column(name = "total_games", nullable = false)
    @Builder.Default
    private Integer totalGames = 0;
    
    @Column(name = "win_count", nullable = false)
    @Builder.Default
    private Integer winCount = 0;
    
    @Column(name = "lose_count", nullable = false)
    @Builder.Default
    private Integer loseCount = 0;
    
    @Column(name = "draw_count", nullable = false)
    @Builder.Default
    private Integer drawCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 计算胜率
     */
    public double getWinRate() {
        if (totalGames == 0) {
            return 0.0;
        }
        return (double) winCount / totalGames * 100;
    }
}
package com.gomoku.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "\"user\"")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;
    
    @Column(unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;
    
    @Column(nullable = false)
    @Builder.Default
    private Integer score = 0;
    
    @Column(name = "total_games", nullable = false)
    @Builder.Default
    private Integer totalGames = 0;
    
    @Column(name = "win_count", nullable = false)
    @Builder.Default
    private Integer winCount = 0;
    
    @Column(name = "lose_count", nullable = false)
    @Builder.Default
    private Integer loseCount = 0;
    
    @Column(name = "draw_count", nullable = false)
    @Builder.Default
    private Integer drawCount = 0;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * 计算胜率
     */
    public double getWinRate() {
        if (totalGames == 0) {
            return 0.0;
        }
        return (double) winCount / totalGames * 100;
    }
}
