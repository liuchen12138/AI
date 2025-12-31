package com.gomoku.service;

import com.gomoku.entity.User;
import com.gomoku.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 匹配服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final GameService gameService;
    
    private static final String MATCH_QUEUE_PREFIX = "match:queue:level:";
    private static final String MATCHING_USER_PREFIX = "match:user:";
    private static final long MATCH_TIMEOUT = 120; // 120秒匹配超时
    
    /**
     * 开始匹配
     */
    public MatchResult startMatch(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否已在匹配队列
        String matchingKey = MATCHING_USER_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(matchingKey))) {
            throw new RuntimeException("您已在匹配队列中");
        }
        
        int level = user.getLevel();
        
        // 先尝试从±2级范围匹配
        Long opponentId = findOpponent(level, 2);
        
        if (opponentId != null) {
            // 找到对手，创建对局
            return createMatch(userId, opponentId);
        }
        
        // 未找到对手，加入匹配队列
        MatchRequest request = new MatchRequest(userId, level, System.currentTimeMillis());
        String queueKey = MATCH_QUEUE_PREFIX + level;
        
        redisTemplate.opsForList().rightPush(queueKey, request);
        redisTemplate.opsForValue().set(matchingKey, "matching", MATCH_TIMEOUT, TimeUnit.SECONDS);
        
        log.info("用户 {} 加入匹配队列，等级：{}", userId, level);
        
        return MatchResult.waiting();
    }
    
    /**
     * 取消匹配
     */
    public void cancelMatch(Long userId) {
        String matchingKey = MATCHING_USER_PREFIX + userId;
        redisTemplate.delete(matchingKey);
        
        // 从所有可能的队列中移除
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            String queueKey = MATCH_QUEUE_PREFIX + user.getLevel();
            removeFromQueue(queueKey, userId);
        }
        
        log.info("用户 {} 取消匹配", userId);
    }
    
    /**
     * 查询匹配状态
     */
    public MatchStatus getMatchStatus(Long userId) {
        String matchingKey = MATCHING_USER_PREFIX + userId;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(matchingKey))) {
            return new MatchStatus(true, false, null);
        }
        
        return new MatchStatus(false, false, null);
    }
    
    /**
     * 寻找对手
     */
    private Long findOpponent(int level, int range) {
        for (int offset = 0; offset <= range; offset++) {
            // 尝试同级
            if (offset == 0) {
                Long opponent = findOpponentInQueue(level);
                if (opponent != null) return opponent;
            } else {
                // 尝试高level
                Long opponent = findOpponentInQueue(level + offset);
                if (opponent != null) return opponent;
                
                // 尝试低level
                opponent = findOpponentInQueue(level - offset);
                if (opponent != null) return opponent;
            }
        }
        
        // 30秒后扩大范围到±5级
        for (int offset = range + 1; offset <= 5; offset++) {
            Long opponent = findOpponentInQueue(level + offset);
            if (opponent != null) return opponent;
            
            opponent = findOpponentInQueue(level - offset);
            if (opponent != null) return opponent;
        }
        
        return null;
    }
    
    /**
     * 从指定等级队列中寻找对手
     */
    private Long findOpponentInQueue(int level) {
        String queueKey = MATCH_QUEUE_PREFIX + level;
        
        Object obj = redisTemplate.opsForList().leftPop(queueKey);
        if (obj instanceof MatchRequest) {
            MatchRequest request = (MatchRequest) obj;
            
            // 检查请求是否超时
            if (System.currentTimeMillis() - request.getTimestamp() < MATCH_TIMEOUT * 1000) {
                return request.getUserId();
            } else {
                // 请求超时，清理并继续查找
                redisTemplate.delete(MATCHING_USER_PREFIX + request.getUserId());
                return findOpponentInQueue(level);
            }
        }
        
        return null;
    }
    
    /**
     * 创建匹配
     */
    private MatchResult createMatch(Long player1Id, Long player2Id) {
        // 清理匹配状态
        redisTemplate.delete(MATCHING_USER_PREFIX + player1Id);
        redisTemplate.delete(MATCHING_USER_PREFIX + player2Id);
        
        // 随机分配黑白
        boolean player1IsBlack = Math.random() < 0.5;
        Long blackPlayerId = player1IsBlack ? player1Id : player2Id;
        Long whitePlayerId = player1IsBlack ? player2Id : player1Id;
        
        // 创建对局
        var game = gameService.createPVPGame(blackPlayerId, whitePlayerId);
        
        log.info("匹配成功：玩家 {} vs 玩家 {}，对局ID：{}", player1Id, player2Id, game.getGameId());
        
        return MatchResult.success(game.getGameId(), blackPlayerId, whitePlayerId);
    }
    
    /**
     * 从队列中移除用户
     */
    private void removeFromQueue(String queueKey, Long userId) {
        // 获取队列中所有元素
        Long size = redisTemplate.opsForList().size(queueKey);
        if (size == null || size == 0) return;
        
        for (int i = 0; i < size; i++) {
            Object obj = redisTemplate.opsForList().index(queueKey, i);
            if (obj instanceof MatchRequest) {
                MatchRequest request = (MatchRequest) obj;
                if (request.getUserId().equals(userId)) {
                    redisTemplate.opsForList().remove(queueKey, 1, obj);
                    break;
                }
            }
        }
    }
    
    /**
     * 匹配请求
     */
    @Data
    @AllArgsConstructor
    public static class MatchRequest {
        private Long userId;
        private Integer level;
        private Long timestamp;
    }
    
    /**
     * 匹配结果
     */
    @Data
    @AllArgsConstructor
    public static class MatchResult {
        private boolean matched;
        private Long gameId;
        private Long blackPlayerId;
        private Long whitePlayerId;
        
        public static MatchResult waiting() {
            return new MatchResult(false, null, null, null);
        }
        
        public static MatchResult success(Long gameId, Long blackPlayerId, Long whitePlayerId) {
            return new MatchResult(true, gameId, blackPlayerId, whitePlayerId);
        }
    }
    
    /**
     * 匹配状态
     */
    @Data
    @AllArgsConstructor
    public static class MatchStatus {
        private boolean matching;
        private boolean matched;
        private Long gameId;
    }
}
package com.gomoku.service;

import com.gomoku.entity.User;
import com.gomoku.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 匹配服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MatchService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserRepository userRepository;
    private final GameService gameService;
    
    private static final String MATCH_QUEUE_PREFIX = "match:queue:level:";
    private static final String MATCHING_USER_PREFIX = "match:user:";
    private static final long MATCH_TIMEOUT = 120; // 120秒匹配超时
    
    /**
     * 开始匹配
     */
    public MatchResult startMatch(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 检查是否已在匹配队列
        String matchingKey = MATCHING_USER_PREFIX + userId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(matchingKey))) {
            throw new RuntimeException("您已在匹配队列中");
        }
        
        int level = user.getLevel();
        
        // 先尝试从±2级范围匹配
        Long opponentId = findOpponent(level, 2);
        
        if (opponentId != null) {
            // 找到对手，创建对局
            return createMatch(userId, opponentId);
        }
        
        // 未找到对手，加入匹配队列
        MatchRequest request = new MatchRequest(userId, level, System.currentTimeMillis());
        String queueKey = MATCH_QUEUE_PREFIX + level;
        
        redisTemplate.opsForList().rightPush(queueKey, request);
        redisTemplate.opsForValue().set(matchingKey, "matching", MATCH_TIMEOUT, TimeUnit.SECONDS);
        
        log.info("用户 {} 加入匹配队列，等级：{}", userId, level);
        
        return MatchResult.waiting();
    }
    
    /**
     * 取消匹配
     */
    public void cancelMatch(Long userId) {
        String matchingKey = MATCHING_USER_PREFIX + userId;
        redisTemplate.delete(matchingKey);
        
        // 从所有可能的队列中移除
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            String queueKey = MATCH_QUEUE_PREFIX + user.getLevel();
            removeFromQueue(queueKey, userId);
        }
        
        log.info("用户 {} 取消匹配", userId);
    }
    
    /**
     * 查询匹配状态
     */
    public MatchStatus getMatchStatus(Long userId) {
        String matchingKey = MATCHING_USER_PREFIX + userId;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(matchingKey))) {
            return new MatchStatus(true, false, null);
        }
        
        return new MatchStatus(false, false, null);
    }
    
    /**
     * 寻找对手
     */
    private Long findOpponent(int level, int range) {
        for (int offset = 0; offset <= range; offset++) {
            // 尝试同级
            if (offset == 0) {
                Long opponent = findOpponentInQueue(level);
                if (opponent != null) return opponent;
            } else {
                // 尝试高level
                Long opponent = findOpponentInQueue(level + offset);
                if (opponent != null) return opponent;
                
                // 尝试低level
                opponent = findOpponentInQueue(level - offset);
                if (opponent != null) return opponent;
            }
        }
        
        // 30秒后扩大范围到±5级
        for (int offset = range + 1; offset <= 5; offset++) {
            Long opponent = findOpponentInQueue(level + offset);
            if (opponent != null) return opponent;
            
            opponent = findOpponentInQueue(level - offset);
            if (opponent != null) return opponent;
        }
        
        return null;
    }
    
    /**
     * 从指定等级队列中寻找对手
     */
    private Long findOpponentInQueue(int level) {
        String queueKey = MATCH_QUEUE_PREFIX + level;
        
        Object obj = redisTemplate.opsForList().leftPop(queueKey);
        if (obj instanceof MatchRequest) {
            MatchRequest request = (MatchRequest) obj;
            
            // 检查请求是否超时
            if (System.currentTimeMillis() - request.getTimestamp() < MATCH_TIMEOUT * 1000) {
                return request.getUserId();
            } else {
                // 请求超时，清理并继续查找
                redisTemplate.delete(MATCHING_USER_PREFIX + request.getUserId());
                return findOpponentInQueue(level);
            }
        }
        
        return null;
    }
    
    /**
     * 创建匹配
     */
    private MatchResult createMatch(Long player1Id, Long player2Id) {
        // 清理匹配状态
        redisTemplate.delete(MATCHING_USER_PREFIX + player1Id);
        redisTemplate.delete(MATCHING_USER_PREFIX + player2Id);
        
        // 随机分配黑白
        boolean player1IsBlack = Math.random() < 0.5;
        Long blackPlayerId = player1IsBlack ? player1Id : player2Id;
        Long whitePlayerId = player1IsBlack ? player2Id : player1Id;
        
        // 创建对局
        var game = gameService.createPVPGame(blackPlayerId, whitePlayerId);
        
        log.info("匹配成功：玩家 {} vs 玩家 {}，对局ID：{}", player1Id, player2Id, game.getGameId());
        
        return MatchResult.success(game.getGameId(), blackPlayerId, whitePlayerId);
    }
    
    /**
     * 从队列中移除用户
     */
    private void removeFromQueue(String queueKey, Long userId) {
        // 获取队列中所有元素
        Long size = redisTemplate.opsForList().size(queueKey);
        if (size == null || size == 0) return;
        
        for (int i = 0; i < size; i++) {
            Object obj = redisTemplate.opsForList().index(queueKey, i);
            if (obj instanceof MatchRequest) {
                MatchRequest request = (MatchRequest) obj;
                if (request.getUserId().equals(userId)) {
                    redisTemplate.opsForList().remove(queueKey, 1, obj);
                    break;
                }
            }
        }
    }
    
    /**
     * 匹配请求
     */
    @Data
    @AllArgsConstructor
    public static class MatchRequest {
        private Long userId;
        private Integer level;
        private Long timestamp;
    }
    
    /**
     * 匹配结果
     */
    @Data
    @AllArgsConstructor
    public static class MatchResult {
        private boolean matched;
        private Long gameId;
        private Long blackPlayerId;
        private Long whitePlayerId;
        
        public static MatchResult waiting() {
            return new MatchResult(false, null, null, null);
        }
        
        public static MatchResult success(Long gameId, Long blackPlayerId, Long whitePlayerId) {
            return new MatchResult(true, gameId, blackPlayerId, whitePlayerId);
        }
    }
    
    /**
     * 匹配状态
     */
    @Data
    @AllArgsConstructor
    public static class MatchStatus {
        private boolean matching;
        private boolean matched;
        private Long gameId;
    }
}
