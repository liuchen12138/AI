package com.gomoku.service;

import com.gomoku.dto.*;
import com.gomoku.entity.User;
import com.gomoku.repository.UserRepository;
import com.gomoku.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * 用户注册
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已被注册");
        }
        
        // 创建用户
        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .level(1)
                .score(0)
                .totalGames(0)
                .winCount(0)
                .loseCount(0)
                .drawCount(0)
                .build();
        
        user = userRepository.save(user);
        log.info("用户注册成功: {}", user.getUsername());
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        
        return AuthResponse.of(token, convertToDTO(user));
    }
    
    /**
     * 用户登录
     */
    public AuthResponse login(LoginRequest request) {
        // 查找用户
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        log.info("用户登录成功: {}", user.getUsername());
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername());
        
        return AuthResponse.of(token, convertToDTO(user));
    }
    
    /**
     * 获取用户信息
     */
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        return convertToDTO(user);
    }
    
    /**
     * 获取用户统计信息
     */
    public UserDTO getUserStats(Long userId) {
        return getUserById(userId);
    }
    
    /**
     * 更新用户积分
     */
    @Transactional
    public void updateUserScore(Long userId, int scoreChange) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        int newScore = Math.max(0, user.getScore() + scoreChange);
        user.setScore(newScore);
        
        // 检查是否需要升级
        updateUserLevel(user);
        
        userRepository.save(user);
        log.info("用户 {} 积分变化: {}, 当前积分: {}", userId, scoreChange, newScore);
    }
    
    /**
     * 更新用户等级
     */
    private void updateUserLevel(User user) {
        int score = user.getScore();
        int newLevel = calculateLevel(score);
        
        if (newLevel != user.getLevel()) {
            user.setLevel(newLevel);
            log.info("用户 {} 等级提升至: {}", user.getUserId(), newLevel);
        }
    }
    
    /**
     * 根据积分计算等级
     */
    private int calculateLevel(int score) {
        if (score >= 1000) return 5;
        if (score >= 600) return 4;
        if (score >= 300) return 3;
        if (score >= 100) return 2;
        return 1;
    }
    
    /**
     * 更新对局统计
     */
    @Transactional
    public void updateGameStats(Long userId, boolean isWin, boolean isDraw) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        user.setTotalGames(user.getTotalGames() + 1);
        
        if (isDraw) {
            user.setDrawCount(user.getDrawCount() + 1);
        } else if (isWin) {
            user.setWinCount(user.getWinCount() + 1);
        } else {
            user.setLoseCount(user.getLoseCount() + 1);
        }
        
        userRepository.save(user);
    }
    
    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .level(user.getLevel())
                .score(user.getScore())
                .totalGames(user.getTotalGames())
                .winCount(user.getWinCount())
                .loseCount(user.getLoseCount())
                .drawCount(user.getDrawCount())
                .winRate(user.getWinRate())
                .build();
    }
}
