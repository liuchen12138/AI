package com.gomoku.service;

import com.gomoku.ai.AIEngine;
import com.gomoku.entity.Game;
import com.gomoku.entity.GameMove;
import com.gomoku.enums.*;
import com.gomoku.game.Board;
import com.gomoku.game.GameLogic;
import com.gomoku.repository.GameMoveRepository;
import com.gomoku.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 对局服务
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GameService {
    
    private final GameRepository gameRepository;
    private final GameMoveRepository gameMoveRepository;
    private final UserService userService;
    private final ScoreService scoreService;
    private final AIEngine aiEngine;
    private final GameLogic gameLogic;
    
    // 内存中的对局状态缓存
    private final Map<Long, Board> activeBoardsCache = new ConcurrentHashMap<>();
    
    /**
     * 创建人机对战
     */
    @Transactional
    public Game createPVEGame(Long playerId, AIDifficulty difficulty) {
        // 检查玩家是否已有进行中的对局
        gameRepository.findActiveGameByPlayerId(playerId).ifPresent(game -> {
            throw new RuntimeException("您已有进行中的对局");
        });
        
        // 玩家随机分配黑白
        boolean playerIsBlack = Math.random() < 0.5;
        
        Game game = Game.builder()
                .gameMode(GameMode.PVE)
                .blackPlayerId(playerIsBlack ? playerId : null)
                .whitePlayerId(playerIsBlack ? null : playerId)
                .aiDifficulty(difficulty)
                .status(GameStatus.PLAYING)
                .startedAt(LocalDateTime.now())
                .build();
        
        game = gameRepository.save(game);
        
        // 创建棋盘缓存
        activeBoardsCache.put(game.getGameId(), new Board());
        
        log.info("创建人机对战: gameId={}, playerId={}, difficulty={}, playerColor={}", 
                game.getGameId(), playerId, difficulty, playerIsBlack ? "BLACK" : "WHITE");
        
        return game;
    }
    
    /**
     * 创建双人对战
     */
    @Transactional
    public Game createPVPGame(Long blackPlayerId, Long whitePlayerId) {
        Game game = Game.builder()
                .gameMode(GameMode.PVP)
                .blackPlayerId(blackPlayerId)
                .whitePlayerId(whitePlayerId)
                .status(GameStatus.PLAYING)
                .startedAt(LocalDateTime.now())
                .build();
        
        game = gameRepository.save(game);
        activeBoardsCache.put(game.getGameId(), new Board());
        
        log.info("创建双人对战: gameId={}, blackPlayer={}, whitePlayer={}", 
                game.getGameId(), blackPlayerId, whitePlayerId);
        
        return game;
    }
    
    /**
     * 玩家落子
     */
    @Transactional
    public GameMoveResult makeMove(Long gameId, Long playerId, int x, int y) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("对局不存在"));
        
        if (game.getStatus() != GameStatus.PLAYING) {
            throw new RuntimeException("对局已结束");
        }
        
        Board board = activeBoardsCache.computeIfAbsent(gameId, id -> loadBoardFromDatabase(id));
        
        // 验证是否轮到该玩家
        PieceColor currentTurn = gameLogic.getNextTurn(board.getMoveCount());
        if (!isPlayerTurn(game, playerId, currentTurn)) {
            throw new RuntimeException("不是您的回合");
        }
        
        // 执行落子
        GameLogic.GameResult result = gameLogic.executeMove(board, x, y, currentTurn);
        
        if (result == GameLogic.GameResult.INVALID_MOVE) {
            throw new RuntimeException("非法落子");
        }
        
        // 保存落子记录
        GameMove move = GameMove.builder()
                .gameId(gameId)
                .moveNumber(board.getMoveCount())
                .playerId(playerId)
                .positionX(x)
                .positionY(y)
                .build();
        gameMoveRepository.save(move);
        
        GameMoveResult moveResult = new GameMoveResult();
        moveResult.setSuccess(true);
        moveResult.setX(x);
        moveResult.setY(y);
        moveResult.setMoveNumber(board.getMoveCount());
        
        // 检查游戏结果
        if (result != GameLogic.GameResult.CONTINUE) {
            handleGameEnd(game, result, currentTurn);
            moveResult.setGameOver(true);
            moveResult.setWinnerId(game.getWinnerId());
            activeBoardsCache.remove(gameId);
        } else {
            moveResult.setGameOver(false);
            moveResult.setNextTurn(gameLogic.getNextTurn(board.getMoveCount()));
            
            // 如果是人机对战且轮到AI
            if (game.getGameMode() == GameMode.PVE && !moveResult.isGameOver()) {
                AIEngine.Position aiMove = calculateAIMove(board, game, playerId);
                if (aiMove != null) {
                    moveResult.setAiMove(aiMove);
                }
            }
        }
        
        return moveResult;
    }
    
    /**
     * 计算AI落子
     */
    private AIEngine.Position calculateAIMove(Board board, Game game, Long humanPlayerId) {
        PieceColor aiColor = gameLogic.getNextTurn(board.getMoveCount());
        AIEngine.Position aiPosition = aiEngine.calculateNextMove(board, game.getAiDifficulty(), aiColor);
        
        if (aiPosition != null) {
            // 执行AI落子
            GameLogic.GameResult result = gameLogic.executeMove(board, aiPosition.getX(), aiPosition.getY(), aiColor);
            
            // 保存AI落子记录
            Long aiPlayerId = game.getBlackPlayerId() != null && !game.getBlackPlayerId().equals(humanPlayerId) 
                    ? game.getBlackPlayerId() : game.getWhitePlayerId();
            
            GameMove aiMove = GameMove.builder()
                    .gameId(game.getGameId())
                    .moveNumber(board.getMoveCount())
                    .playerId(aiPlayerId)
                    .positionX(aiPosition.getX())
                    .positionY(aiPosition.getY())
                    .build();
            gameMoveRepository.save(aiMove);
            
            // 检查AI落子后的游戏结果
            if (result != GameLogic.GameResult.CONTINUE) {
                handleGameEnd(game, result, aiColor);
                activeBoardsCache.remove(game.getGameId());
            }
        }
        
        return aiPosition;
    }
    
    /**
     * 玩家认输
     */
    @Transactional
    public void resign(Long gameId, Long playerId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("对局不存在"));
        
        if (game.getStatus() != GameStatus.PLAYING) {
            throw new RuntimeException("对局已结束");
        }
        
        // 确定胜者
        Long winnerId = game.getBlackPlayerId().equals(playerId) 
                ? game.getWhitePlayerId() 
                : game.getBlackPlayerId();
        
        game.setStatus(GameStatus.FINISHED);
        game.setWinnerId(winnerId);
        game.setEndReason(EndReason.RESIGN);
        game.setEndedAt(LocalDateTime.now());
        
        gameRepository.save(game);
        
        // 更新积分（人机对战不更新）
        if (game.getGameMode() == GameMode.PVP && winnerId != null) {
            updateScoresAfterGame(game);
        }
        
        activeBoardsCache.remove(gameId);
        
        log.info("玩家认输: gameId={}, resignPlayerId={}, winnerId={}", gameId, playerId, winnerId);
    }
    
    /**
     * 处理游戏结束
     */
    private void handleGameEnd(Game game, GameLogic.GameResult result, PieceColor lastMoveColor) {
        game.setStatus(GameStatus.FINISHED);
        game.setEndedAt(LocalDateTime.now());
        game.setEndReason(EndReason.NORMAL);
        
        switch (result) {
            case BLACK_WIN:
                game.setWinnerId(game.getBlackPlayerId());
                break;
            case WHITE_WIN:
                game.setWinnerId(game.getWhitePlayerId());
                break;
            case BLACK_LOSE_OVERLINE:
                game.setWinnerId(game.getWhitePlayerId());
                break;
            case DRAW:
                game.setWinnerId(null);
                break;
            default:
                break;
        }
        
        gameRepository.save(game);
        
        // 更新积分（仅双人对战）
        if (game.getGameMode() == GameMode.PVP) {
            updateScoresAfterGame(game);
        }
        
        log.info("对局结束: gameId={}, result={}, winnerId={}", game.getGameId(), result, game.getWinnerId());
    }
    
    /**
     * 更新对局后的积分
     */
    private void updateScoresAfterGame(Game game) {
        if (game.getWinnerId() == null) {
            // 平局
            scoreService.updateDrawScore(game.getBlackPlayerId(), game.getWhitePlayerId());
        } else {
            // 有胜负
            Long winnerId = game.getWinnerId();
            Long loserId = game.getBlackPlayerId().equals(winnerId) 
                    ? game.getWhitePlayerId() 
                    : game.getBlackPlayerId();
            
            int winnerLevel = userService.getUserById(winnerId).getLevel();
            int loserLevel = userService.getUserById(loserId).getLevel();
            
            scoreService.updateGameScore(winnerId, loserId, winnerLevel, loserLevel);
        }
    }
    
    /**
     * 验证是否轮到该玩家
     */
    private boolean isPlayerTurn(Game game, Long playerId, PieceColor currentTurn) {
        if (currentTurn == PieceColor.BLACK) {
            return game.getBlackPlayerId() != null && game.getBlackPlayerId().equals(playerId);
        } else {
            return game.getWhitePlayerId() != null && game.getWhitePlayerId().equals(playerId);
        }
    }
    
    /**
     * 从数据库加载棋盘状态
     */
    private Board loadBoardFromDatabase(Long gameId) {
        Board board = new Board();
        List<GameMove> moves = gameMoveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
        
        for (GameMove move : moves) {
            Game game = gameRepository.findById(gameId).orElseThrow();
            PieceColor color = game.getBlackPlayerId().equals(move.getPlayerId()) 
                    ? PieceColor.BLACK 
                    : PieceColor.WHITE;
            board.placeStone(move.getPositionX(), move.getPositionY(), color);
        }
        
        return board;
    }
    
    /**
     * 获取对局详情
     */
    public Game getGame(Long gameId) {
        return gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("对局不存在"));
    }
    
    /**
     * 获取对局历史
     */
    public Page<Game> getGameHistory(Long playerId, Pageable pageable) {
        return gameRepository.findByPlayerId(playerId, pageable);
    }
    
    /**
     * 获取棋谱
     */
    public List<GameMove> getGameMoves(Long gameId) {
        return gameMoveRepository.findByGameIdOrderByMoveNumberAsc(gameId);
    }
    
    /**
     * 落子结果
     */
    @lombok.Data
    public static class GameMoveResult {
        private boolean success;
        private int x;
        private int y;
        private int moveNumber;
        private boolean gameOver;
        private Long winnerId;
        private PieceColor nextTurn;
        private AIEngine.Position aiMove;
    }
}
