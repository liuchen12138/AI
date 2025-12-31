package com.gomoku.repository;

import com.gomoku.entity.GameMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameMoveRepository extends JpaRepository<GameMove, Long> {
    
    /**
     * 根据对局ID查找所有棋步
     */
    List<GameMove> findByGameIdOrderByMoveNumberAsc(Long gameId);
    
    /**
     * 统计对局中的棋步数
     */
    long countByGameId(Long gameId);
    
    /**
     * 删除对局的所有棋步
     */
    void deleteByGameId(Long gameId);
}
package com.gomoku.repository;

import com.gomoku.entity.GameMove;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameMoveRepository extends JpaRepository<GameMove, Long> {
    
    /**
     * 根据对局ID查找所有棋步
     */
    List<GameMove> findByGameIdOrderByMoveNumberAsc(Long gameId);
    
    /**
     * 统计对局中的棋步数
     */
    long countByGameId(Long gameId);
    
    /**
     * 删除对局的所有棋步
     */
    void deleteByGameId(Long gameId);
}
