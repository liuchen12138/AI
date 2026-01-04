package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.entity.Game;
import com.gomoku.entity.GameMove;
import com.gomoku.enums.AIDifficulty;
import com.gomoku.service.GameService;
import com.gomoku.util.JwtUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/game")
@RequiredArgsConstructor
public class GameController extends BaseController {
    
    private final GameService gameService;
    
    /**
     * 创建人机对战
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Game>> createGame(
            @RequestHeader("Authorization") String token,
            @RequestBody CreateGameRequest request) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Game game = gameService.createPVEGame(userId, request.getDifficulty());
            return ResponseEntity.ok(ApiResponse.success("创建成功", game));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 落子
     */
    @PostMapping("/{gameId}/move")
    public ResponseEntity<ApiResponse<GameService.GameMoveResult>> makeMove(
            @RequestHeader("Authorization") String token,
            @PathVariable Long gameId,
            @RequestBody MoveRequest request) {
        try {
            Long userId = getUserIdFromToken(token);
            
            GameService.GameMoveResult result = gameService.makeMove(
                    gameId, userId, request.getX(), request.getY());
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 认输
     */
    @PostMapping("/{gameId}/resign")
    public ResponseEntity<ApiResponse<Void>> resign(
            @RequestHeader("Authorization") String token,
            @PathVariable Long gameId) {
        try {
            Long userId = getUserIdFromToken(token);
            
            gameService.resign(gameId, userId);
            return ResponseEntity.ok(ApiResponse.success("认输成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取对局详情
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<Game>> getGame(@PathVariable Long gameId) {
        try {
            Game game = gameService.getGame(gameId);
            return ResponseEntity.ok(ApiResponse.success(game));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取历史对局
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<Page<Game>>> getHistory(
            @RequestHeader("Authorization") String token,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            Long userId = getUserIdFromToken(token);
            
            Page<Game> games = gameService.getGameHistory(userId, PageRequest.of(page, size));
            return ResponseEntity.ok(ApiResponse.success(games));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取棋谱
     */
    @GetMapping("/{gameId}/moves")
    public ResponseEntity<ApiResponse<List<GameMove>>> getMoves(@PathVariable Long gameId) {
        try {
            List<GameMove> moves = gameService.getGameMoves(gameId);
            return ResponseEntity.ok(ApiResponse.success(moves));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @Data
    public static class CreateGameRequest {
        private AIDifficulty difficulty;
    }
    
    @Data
    public static class MoveRequest {
        private int x;
        private int y;
    }
}
