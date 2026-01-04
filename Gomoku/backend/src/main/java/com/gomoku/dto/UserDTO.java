package com.gomoku.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long userId;
    private String username;
    private String email;
    private Integer level;
    private Integer score;
    private Integer totalGames;
    private Integer winCount;
    private Integer loseCount;
    private Integer drawCount;
    private Double winRate;
}
