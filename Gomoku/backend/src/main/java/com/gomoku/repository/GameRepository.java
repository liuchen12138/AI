package com.gomoku.repository;

import com.gomoku.entity.Game;
import com.gomoku.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    
    /**
     * 根据状态查找对局
     */
    List<Game> findByStatus(GameStatus status);
    
    /**
     * 查找玩家参与的所有对局（分页）
     */
    @Query("SELECT g FROM Game g WHERE g.blackPlayerId = :playerId OR g.whitePlayerId = :playerId ORDER BY g.createdAt DESC")
    Page<Game> findByPlayerId(@Param("playerId") Long playerId, Pageable pageable);
    
    /**
     * 查找玩家正在进行的对局
     */
    @Query("SELECT g FROM Game g WHERE (g.blackPlayerId = :playerId OR g.whitePlayerId = :playerId) AND g.status IN ('WAITING', 'PLAYING')")
    Optional<Game> findActiveGameByPlayerId(@Param("playerId") Long playerId);
    
    /**
     * 统计玩家对局数
     */
    @Query("SELECT COUNT(g) FROM Game g WHERE (g.blackPlayerId = :playerId OR g.whitePlayerId = :playerId) AND g.status = 'FINISHED'")
    long countFinishedGamesByPlayerId(@Param("playerId") Long playerId);
}
