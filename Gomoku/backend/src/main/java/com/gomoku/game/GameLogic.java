package com.gomoku.game;

import com.gomoku.enums.PieceColor;
import lombok.Getter;
import org.springframework.stereotype.Component;

/**
 * 游戏逻辑管理类
 */
@Component
@Getter
public class GameLogic {
    
    /**
     * 验证落子是否合法
     */
    public boolean validateMove(Board board, int x, int y, PieceColor currentTurn) {
        // 检查坐标是否有效
        if (!board.isValidPosition(x, y)) {
            return false;
        }
        
        // 检查位置是否为空
        if (!board.isEmpty(x, y)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 执行落子并检查游戏结果
     */
    public GameResult executeMove(Board board, int x, int y, PieceColor color) {
        // 放置棋子
        if (!board.placeStone(x, y, color)) {
            return GameResult.INVALID_MOVE;
        }
        
        // 检查是否获胜
        if (board.checkWin(x, y)) {
            // 黑棋长连判负
            if (color == PieceColor.BLACK && board.isOverline(x, y)) {
                return GameResult.BLACK_LOSE_OVERLINE;
            }
            return color == PieceColor.BLACK ? GameResult.BLACK_WIN : GameResult.WHITE_WIN;
        }
        
        // 检查是否平局
        if (board.isFull()) {
            return GameResult.DRAW;
        }
        
        return GameResult.CONTINUE;
    }
    
    /**
     * 获取下一个回合的棋子颜色
     */
    public PieceColor getNextTurn(int moveCount) {
        return moveCount % 2 == 0 ? PieceColor.BLACK : PieceColor.WHITE;
    }
    
    /**
     * 游戏结果枚举
     */
    public enum GameResult {
        CONTINUE,              // 继续游戏
        BLACK_WIN,            // 黑棋获胜
        WHITE_WIN,            // 白棋获胜
        DRAW,                 // 平局
        BLACK_LOSE_OVERLINE,  // 黑棋长连判负
        INVALID_MOVE          // 非法落子
    }
}
