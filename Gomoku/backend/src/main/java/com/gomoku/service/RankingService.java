package com.gomoku.service;

import com.gomoku.dto.UserDTO;
import com.gomoku.entity.User;
import com.gomoku.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 排行榜服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RankingService {
    
    private final UserRepository userRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final String SCORE_RANKING_KEY = "ranking:score";
    private static final String LEVEL_RANKING_KEY = "ranking:level";
    private static final String WINRATE_RANKING_KEY = "ranking:winrate";
    private static final long CACHE_EXPIRE_TIME = 5; // 5分钟过期
    
    /**
     * 获取积分排行榜
     */
    public List<UserDTO> getScoreRanking(int limit) {
        String key = SCORE_RANKING_KEY;
        
        // 尝试从缓存获取
        Set<Object> cachedRanking = redisTemplate.opsForZSet()
                .reverseRange(key, 0, limit - 1);
        
        if (cachedRanking != null && !cachedRanking.isEmpty()) {
            return convertCacheToUserDTOList(cachedRanking);
        }
        
        // 从数据库查询
        List<User> users = userRepository.findTop100ByOrderByScoreDesc();
        List<UserDTO> ranking = new ArrayList<>();
        
        // 更新缓存
        for (User user : users) {
            ranking.add(convertToDTO(user));
            redisTemplate.opsForZSet().add(key, user.getUserId(), user.getScore());
        }
        
        redisTemplate.expire(key, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return ranking.stream().limit(limit).toList();
    }
    
    /**
     * 获取胜率排行榜
     */
    public List<UserDTO> getWinRateRanking(int limit) {
        // 胜率排行榜不使用缓存，直接从数据库查询
        List<User> users = userRepository.findTop100ByWinRate();
        List<UserDTO> ranking = new ArrayList<>();
        
        for (User user : users) {
            ranking.add(convertToDTO(user));
        }
        
        return ranking.stream().limit(limit).toList();
    }
    
    /**
     * 获取等级排行榜
     */
    public List<UserDTO> getLevelRanking(int limit) {
        String key = LEVEL_RANKING_KEY;
        
        // 尝试从缓存获取
        Set<Object> cachedRanking = redisTemplate.opsForZSet()
                .reverseRange(key, 0, limit - 1);
        
        if (cachedRanking != null && !cachedRanking.isEmpty()) {
            return convertCacheToUserDTOList(cachedRanking);
        }
        
        // 从数据库查询
        List<User> users = userRepository.findTop100ByOrderByLevelDesc();
        List<UserDTO> ranking = new ArrayList<>();
        
        // 更新缓存
        for (User user : users) {
            ranking.add(convertToDTO(user));
            redisTemplate.opsForZSet().add(key, user.getUserId(), user.getLevel());
        }
        
        redisTemplate.expire(key, CACHE_EXPIRE_TIME, TimeUnit.MINUTES);
        
        return ranking.stream().limit(limit).toList();
    }
    
    /**
     * 获取用户排名信息
     */
    public RankInfo getUserRanking(Long userId) {
        RankInfo rankInfo = new RankInfo();
        
        // 积分排名
        Long scoreRank = redisTemplate.opsForZSet().reverseRank(SCORE_RANKING_KEY, userId);
        rankInfo.setScoreRank(scoreRank != null ? scoreRank.intValue() + 1 : -1);
        
        // 等级排名
        Long levelRank = redisTemplate.opsForZSet().reverseRank(LEVEL_RANKING_KEY, userId);
        rankInfo.setLevelRank(levelRank != null ? levelRank.intValue() + 1 : -1);
        
        return rankInfo;
    }
    
    /**
     * 更新用户排行榜缓存
     */
    public void updateUserRanking(Long userId, int score, int level) {
        // 更新积分排行
        redisTemplate.opsForZSet().add(SCORE_RANKING_KEY, userId, score);
        
        // 更新等级排行
        redisTemplate.opsForZSet().add(LEVEL_RANKING_KEY, userId, level);
        
        log.debug("更新用户 {} 排行榜缓存：积分={}, 等级={}", userId, score, level);
    }
    
    /**
     * 清除排行榜缓存
     */
    public void clearRankingCache() {
        redisTemplate.delete(SCORE_RANKING_KEY);
        redisTemplate.delete(LEVEL_RANKING_KEY);
        redisTemplate.delete(WINRATE_RANKING_KEY);
        log.info("排行榜缓存已清除");
    }
    
    /**
     * 从缓存转换为UserDTO列表
     */
    private List<UserDTO> convertCacheToUserDTOList(Set<Object> cachedIds) {
        List<UserDTO> result = new ArrayList<>();
        
        for (Object obj : cachedIds) {
            Long userId = Long.valueOf(obj.toString());
            userRepository.findById(userId).ifPresent(user -> 
                result.add(convertToDTO(user))
            );
        }
        
        return result;
    }
    
    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .level(user.getLevel())
                .score(user.getScore())
                .totalGames(user.getTotalGames())
                .winCount(user.getWinCount())
                .loseCount(user.getLoseCount())
                .drawCount(user.getDrawCount())
                .winRate(user.getWinRate())
                .build();
    }
    
    /**
     * 排名信息类
     */
    @lombok.Data
    public static class RankInfo {
        private int scoreRank;
        private int levelRank;
    }
}
