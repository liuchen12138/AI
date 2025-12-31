package com.gomoku.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 积分等级系统服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreService {
    
    private final UserService userService;
    
    /**
     * 计算积分变化
     */
    public int calculateScoreChange(int winnerLevel, int loserLevel, boolean isWin, boolean isDraw) {
        if (isDraw) {
            return 2; // 平局固定积分
        }
        
        if (isWin) {
            // 胜利积分：+10 + (对手等级 - 自己等级) × 2
            return 10 + (loserLevel - winnerLevel) * 2;
        } else {
            // 失败积分：-5 - (自己等级 - 对手等级) × 1
            int penalty = -5 - (winnerLevel - loserLevel);
            return Math.max(penalty, -15); // 最多扣15分
        }
    }
    
    /**
     * 更新对局结果的积分
     */
    public void updateGameScore(Long winnerId, Long loserId, int winnerLevel, int loserLevel) {
        if (winnerId != null) {
            // 赢家加分
            int winnerScore = calculateScoreChange(winnerLevel, loserLevel, true, false);
            userService.updateUserScore(winnerId, winnerScore);
            userService.updateGameStats(winnerId, true, false);
        }
        
        if (loserId != null) {
            // 输家扣分
            int loserScore = calculateScoreChange(loserLevel, winnerLevel, false, false);
            userService.updateUserScore(loserId, loserScore);
            userService.updateGameStats(loserId, false, false);
        }
    }
    
    /**
     * 更新平局积分
     */
    public void updateDrawScore(Long player1Id, Long player2Id) {
        userService.updateUserScore(player1Id, 2);
        userService.updateGameStats(player1Id, false, true);
        
        if (player2Id != null) {
            userService.updateUserScore(player2Id, 2);
            userService.updateGameStats(player2Id, false, true);
        }
    }
    
    /**
     * 获取等级称号
     */
    public String getLevelTitle(int level) {
        return switch (level) {
            case 1 -> "初学者";
            case 2 -> "入门";
            case 3 -> "进阶";
            case 4 -> "熟练";
            case 5 -> "高手";
            case 6 -> "专家";
            case 7 -> "大师";
            default -> level >= 8 ? "宗师" : "新手";
        };
    }
}
package com.gomoku.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 积分等级系统服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreService {
    
    private final UserService userService;
    
    /**
     * 计算积分变化
     */
    public int calculateScoreChange(int winnerLevel, int loserLevel, boolean isWin, boolean isDraw) {
        if (isDraw) {
            return 2; // 平局固定积分
        }
        
        if (isWin) {
            // 胜利积分：+10 + (对手等级 - 自己等级) × 2
            return 10 + (loserLevel - winnerLevel) * 2;
        } else {
            // 失败积分：-5 - (自己等级 - 对手等级) × 1
            int penalty = -5 - (winnerLevel - loserLevel);
            return Math.max(penalty, -15); // 最多扣15分
        }
    }
    
    /**
     * 更新对局结果的积分
     */
    public void updateGameScore(Long winnerId, Long loserId, int winnerLevel, int loserLevel) {
        if (winnerId != null) {
            // 赢家加分
            int winnerScore = calculateScoreChange(winnerLevel, loserLevel, true, false);
            userService.updateUserScore(winnerId, winnerScore);
            userService.updateGameStats(winnerId, true, false);
        }
        
        if (loserId != null) {
            // 输家扣分
            int loserScore = calculateScoreChange(loserLevel, winnerLevel, false, false);
            userService.updateUserScore(loserId, loserScore);
            userService.updateGameStats(loserId, false, false);
        }
    }
    
    /**
     * 更新平局积分
     */
    public void updateDrawScore(Long player1Id, Long player2Id) {
        userService.updateUserScore(player1Id, 2);
        userService.updateGameStats(player1Id, false, true);
        
        if (player2Id != null) {
            userService.updateUserScore(player2Id, 2);
            userService.updateGameStats(player2Id, false, true);
        }
    }
    
    /**
     * 获取等级称号
     */
    public String getLevelTitle(int level) {
        return switch (level) {
            case 1 -> "初学者";
            case 2 -> "入门";
            case 3 -> "进阶";
            case 4 -> "熟练";
            case 5 -> "高手";
            case 6 -> "专家";
            case 7 -> "大师";
            default -> level >= 8 ? "宗师" : "新手";
        };
    }
}
