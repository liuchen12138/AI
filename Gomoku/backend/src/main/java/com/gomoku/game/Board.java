package com.gomoku.game;

import com.gomoku.enums.PieceColor;
import lombok.Data;

/**
 * 棋盘管理类
 */
@Data
public class Board {
    
    public static final int BOARD_SIZE = 15;
    
    // 棋盘状态：0-空，1-黑棋，2-白棋
    private int[][] board;
    
    // 当前回合数
    private int moveCount;
    
    public Board() {
        this.board = new int[BOARD_SIZE][BOARD_SIZE];
        this.moveCount = 0;
    }
    
    /**
     * 放置棋子
     */
    public boolean placeStone(int x, int y, PieceColor color) {
        if (!isValidPosition(x, y) || !isEmpty(x, y)) {
            return false;
        }
        
        board[x][y] = color == PieceColor.BLACK ? 1 : 2;
        moveCount++;
        return true;
    }
    
    /**
     * 检查位置是否有效
     */
    public boolean isValidPosition(int x, int y) {
        return x >= 0 && x < BOARD_SIZE && y >= 0 && y < BOARD_SIZE;
    }
    
    /**
     * 检查位置是否为空
     */
    public boolean isEmpty(int x, int y) {
        return board[x][y] == 0;
    }
    
    /**
     * 获取指定位置的棋子
     */
    public int getStone(int x, int y) {
        if (!isValidPosition(x, y)) {
            return 0;
        }
        return board[x][y];
    }
    
    /**
     * 检查是否获胜
     */
    public boolean checkWin(int x, int y) {
        int stone = board[x][y];
        if (stone == 0) {
            return false;
        }
        
        // 检查四个方向
        return checkDirection(x, y, 1, 0, stone) ||  // 横向
               checkDirection(x, y, 0, 1, stone) ||  // 纵向
               checkDirection(x, y, 1, 1, stone) ||  // 左上到右下
               checkDirection(x, y, 1, -1, stone);   // 右上到左下
    }
    
    /**
     * 检查指定方向是否有五子连珠
     */
    private boolean checkDirection(int x, int y, int dx, int dy, int stone) {
        int count = 1;
        
        // 正向检查
        int nx = x + dx;
        int ny = y + dy;
        while (isValidPosition(nx, ny) && board[nx][ny] == stone) {
            count++;
            nx += dx;
            ny += dy;
        }
        
        // 反向检查
        nx = x - dx;
        ny = y - dy;
        while (isValidPosition(nx, ny) && board[nx][ny] == stone) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        
        return count >= 5;
    }
    
    /**
     * 检查是否为长连（超过5子）
     */
    public boolean isOverline(int x, int y) {
        int stone = board[x][y];
        if (stone == 0) {
            return false;
        }
        
        return checkOverlineDirection(x, y, 1, 0, stone) ||
               checkOverlineDirection(x, y, 0, 1, stone) ||
               checkOverlineDirection(x, y, 1, 1, stone) ||
               checkOverlineDirection(x, y, 1, -1, stone);
    }
    
    /**
     * 检查指定方向是否超过五子
     */
    private boolean checkOverlineDirection(int x, int y, int dx, int dy, int stone) {
        int count = 1;
        
        int nx = x + dx;
        int ny = y + dy;
        while (isValidPosition(nx, ny) && board[nx][ny] == stone) {
            count++;
            nx += dx;
            ny += dy;
        }
        
        nx = x - dx;
        ny = y - dy;
        while (isValidPosition(nx, ny) && board[nx][ny] == stone) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        
        return count > 5;
    }
    
    /**
     * 检查棋盘是否已满
     */
    public boolean isFull() {
        return moveCount >= BOARD_SIZE * BOARD_SIZE;
    }
    
    /**
     * 重置棋盘
     */
    public void reset() {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        moveCount = 0;
    }
    
    /**
     * 复制棋盘
     */
    public Board copy() {
        Board newBoard = new Board();
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(this.board[i], 0, newBoard.board[i], 0, BOARD_SIZE);
        }
        newBoard.moveCount = this.moveCount;
        return newBoard;
    }
}
