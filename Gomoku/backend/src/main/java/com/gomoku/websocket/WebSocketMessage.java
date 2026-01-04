package com.gomoku.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * WebSocket消息基类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    
    private MessageType type;
    private Long gameId;
    private Object data;
    private Long timestamp;
    
    public enum MessageType {
        // 客户端发送
        MOVE,           // 落子
        RESIGN,         // 认输
        HEARTBEAT,      // 心跳
        
        // 服务器推送
        GAME_START,     // 游戏开始
        MOVE_SUCCESS,   // 落子成功
        MOVE_INVALID,   // 落子非法
        OPPONENT_MOVE,  // 对手落子
        GAME_OVER,      // 游戏结束
        OPPONENT_RESIGN,// 对手认输
        OPPONENT_DISCONNECT, // 对手断线
        HEARTBEAT_ACK,  // 心跳响应
        ERROR           // 错误消息
    }
    
    public static WebSocketMessage success(MessageType type, Long gameId, Object data) {
        return new WebSocketMessage(type, gameId, data, System.currentTimeMillis());
    }
    
    public static WebSocketMessage error(String message) {
        return new WebSocketMessage(MessageType.ERROR, null, message, System.currentTimeMillis());
    }
}
