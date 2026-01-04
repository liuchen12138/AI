package com.gomoku.websocket;

import com.gomoku.service.GameService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket消息处理器
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GameWebSocketHandler {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final GameService gameService;
    
    /**
     * 处理落子消息
     */
    @MessageMapping("/game/{gameId}/move")
    public void handleMove(
            @DestinationVariable Long gameId,
            @Payload Map<String, Integer> move,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getHeader("userId");
            
            int x = move.get("x");
            int y = move.get("y");
            
            // 执行落子
            GameService.GameMoveResult result = gameService.makeMove(gameId, userId, x, y);
            
            // 发送落子成功消息给当前玩家
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getUser().getName(),
                    "/queue/game/" + gameId,
                    WebSocketMessage.success(WebSocketMessage.MessageType.MOVE_SUCCESS, gameId, result)
            );
            
            // 广播对手落子消息
            messagingTemplate.convertAndSend(
                    "/topic/game/" + gameId,
                    WebSocketMessage.success(WebSocketMessage.MessageType.OPPONENT_MOVE, gameId, result)
            );
            
            // 如果游戏结束，发送游戏结束消息
            if (result.isGameOver()) {
                messagingTemplate.convertAndSend(
                        "/topic/game/" + gameId,
                        WebSocketMessage.success(WebSocketMessage.MessageType.GAME_OVER, gameId, result)
                );
            }
            
        } catch (Exception e) {
            log.error("处理落子消息失败", e);
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getUser().getName(),
                    "/queue/errors",
                    WebSocketMessage.error(e.getMessage())
            );
        }
    }
    
    /**
     * 处理认输消息
     */
    @MessageMapping("/game/{gameId}/resign")
    public void handleResign(
            @DestinationVariable Long gameId,
            SimpMessageHeaderAccessor headerAccessor) {
        try {
            Long userId = (Long) headerAccessor.getHeader("userId");
            
            gameService.resign(gameId, userId);
            
            // 广播认输消息
            messagingTemplate.convertAndSend(
                    "/topic/game/" + gameId,
                    WebSocketMessage.success(WebSocketMessage.MessageType.OPPONENT_RESIGN, gameId, userId)
            );
            
        } catch (Exception e) {
            log.error("处理认输消息失败", e);
            messagingTemplate.convertAndSendToUser(
                    headerAccessor.getUser().getName(),
                    "/queue/errors",
                    WebSocketMessage.error(e.getMessage())
            );
        }
    }
    
    /**
     * 处理心跳消息
     */
    @MessageMapping("/game/{gameId}/heartbeat")
    public void handleHeartbeat(
            @DestinationVariable Long gameId,
            SimpMessageHeaderAccessor headerAccessor) {
        // 发送心跳响应
        messagingTemplate.convertAndSendToUser(
                headerAccessor.getUser().getName(),
                "/queue/heartbeat",
                WebSocketMessage.success(WebSocketMessage.MessageType.HEARTBEAT_ACK, gameId, null)
        );
    }
}
