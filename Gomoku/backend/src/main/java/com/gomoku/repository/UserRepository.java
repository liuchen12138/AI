package com.gomoku.repository;

import com.gomoku.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 根据积分排序获取前N名用户
     */
    List<User> findTop100ByOrderByScoreDesc();
    
    /**
     * 根据等级排序获取前N名用户
     */
    List<User> findTop100ByOrderByLevelDesc();
    
    /**
     * 获取胜率最高的用户（至少10场对局）
     */
    @Query("SELECT u FROM User u WHERE u.totalGames >= 10 ORDER BY (CAST(u.winCount AS double) / u.totalGames) DESC")
    List<User> findTop100ByWinRate();
}
