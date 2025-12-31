package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.dto.UserDTO;
import com.gomoku.service.RankingService;
import com.gomoku.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取积分排行榜
     */
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getScoreRanking(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<UserDTO> ranking = rankingService.getScoreRanking(limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取胜率排行榜
     */
    @GetMapping("/winrate")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getWinRateRanking(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<UserDTO> ranking = rankingService.getWinRateRanking(limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取等级排行榜
     */
    @GetMapping("/level")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getLevelRanking(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<UserDTO> ranking = rankingService.getLevelRanking(limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取我的排名信息
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<RankingService.RankInfo>> getMyRanking(
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(jwt);
            RankingService.RankInfo rankInfo = rankingService.getUserRanking(userId);
            return ResponseEntity.ok(ApiResponse.success(rankInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.dto.UserDTO;
import com.gomoku.service.RankingService;
import com.gomoku.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {
    
    private final RankingService rankingService;
    private final JwtUtil jwtUtil;
    
    /**
     * 获取积分排行榜
     */
    @GetMapping("/score")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getScoreRanking(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<UserDTO> ranking = rankingService.getScoreRanking(limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取胜率排行榜
     */
    @GetMapping("/winrate")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getWinRateRanking(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<UserDTO> ranking = rankingService.getWinRateRanking(limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取等级排行榜
     */
    @GetMapping("/level")
    public ResponseEntity<ApiResponse<List<UserDTO>>> getLevelRanking(
            @RequestParam(defaultValue = "100") int limit) {
        try {
            List<UserDTO> ranking = rankingService.getLevelRanking(limit);
            return ResponseEntity.ok(ApiResponse.success(ranking));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 获取我的排名信息
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<RankingService.RankInfo>> getMyRanking(
            @RequestHeader("Authorization") String token) {
        try {
            String jwt = token.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(jwt);
            RankingService.RankInfo rankInfo = rankingService.getUserRanking(userId);
            return ResponseEntity.ok(ApiResponse.success(rankInfo));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
