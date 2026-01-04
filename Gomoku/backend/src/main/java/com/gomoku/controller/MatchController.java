package com.gomoku.controller;

import com.gomoku.dto.ApiResponse;
import com.gomoku.service.MatchService;
import com.gomoku.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class MatchController extends BaseController {
    
    private final MatchService matchService;
    
    /**
     * 开始匹配
     */
    @PostMapping("/start")
    public ResponseEntity<ApiResponse<MatchService.MatchResult>> startMatch(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            MatchService.MatchResult result = matchService.startMatch(userId);
            return ResponseEntity.ok(ApiResponse.success("开始匹配", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 取消匹配
     */
    @PostMapping("/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelMatch(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            matchService.cancelMatch(userId);
            return ResponseEntity.ok(ApiResponse.success("取消成功", null));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    /**
     * 查询匹配状态
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<MatchService.MatchStatus>> getMatchStatus(
            @RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            
            MatchService.MatchStatus status = matchService.getMatchStatus(userId);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
