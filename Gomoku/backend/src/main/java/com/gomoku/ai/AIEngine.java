package com.gomoku.ai;

import com.gomoku.enums.AIDifficulty;
import com.gomoku.enums.PieceColor;
import com.gomoku.game.Board;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * AI引擎 - 支持三种难度
 */
@Component
public class AIEngine {
    
    private final Random random = new Random();
    
    /**
     * 计算AI的下一步落子位置
     */
    public Position calculateNextMove(Board board, AIDifficulty difficulty, PieceColor aiColor) {
        return switch (difficulty) {
            case EASY -> calculateEasyMove(board, aiColor);
            case MEDIUM -> calculateMediumMove(board, aiColor);
            case HARD -> calculateHardMove(board, aiColor);
        };
    }
    
    /**
     * 简单难度：随机落子 + 简单防守
     */
    private Position calculateEasyMove(Board board, PieceColor aiColor) {
        PieceColor opponentColor = aiColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
        
        // 30%概率进行防守检测
        if (random.nextDouble() < 0.3) {
            Position blockPos = findBlockingMove(board, opponentColor);
            if (blockPos != null) {
                return blockPos;
            }
        }
        
        // 否则随机落子
        return getRandomMove(board);
    }
    
    /**
     * 中等难度：模式识别 + 攻守平衡
     */
    private Position calculateMediumMove(Board board, PieceColor aiColor) {
        PieceColor opponentColor = aiColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK;
        
        // 1. 检查是否有必胜点
        Position winPos = findWinningMove(board, aiColor);
        if (winPos != null) {
            return winPos;
        }
        
        // 2. 检查是否需要防守
        Position blockPos = findBlockingMove(board, opponentColor);
        if (blockPos != null) {
            return blockPos;
        }
        
        // 3. 寻找最佳落子点
        return findBestMove(board, aiColor);
    }
    
    /**
     * 困难难度：MinMax算法 + Alpha-Beta剪枝
     */
    private Position calculateHardMove(Board board, PieceColor aiColor) {
        int depth = 2; // 搜索深度
        Position bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        List<Position> candidates = getCandidatePositions(board);
        
        for (Position pos : candidates) {
            Board tempBoard = board.copy();
            tempBoard.placeStone(pos.x, pos.y, aiColor);
            
            int score = minimax(tempBoard, depth - 1, false, aiColor, Integer.MIN_VALUE, Integer.MAX_VALUE);
            
            if (score > bestScore) {
                bestScore = score;
                bestMove = pos;
            }
        }
        
        return bestMove != null ? bestMove : getRandomMove(board);
    }
    
    /**
     * MinMax算法实现
     */
    private int minimax(Board board, int depth, boolean isMaximizing, PieceColor aiColor, int alpha, int beta) {
        if (depth == 0 || board.isFull()) {
            return evaluateBoard(board, aiColor);
        }
        
        PieceColor currentColor = isMaximizing ? aiColor : 
                (aiColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
        
        List<Position> moves = getCandidatePositions(board);
        
        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (Position pos : moves) {
                Board tempBoard = board.copy();
                tempBoard.placeStone(pos.x, pos.y, currentColor);
                int score = minimax(tempBoard, depth - 1, false, aiColor, alpha, beta);
                maxScore = Math.max(maxScore, score);
                alpha = Math.max(alpha, score);
                if (beta <= alpha) {
                    break; // Beta剪枝
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (Position pos : moves) {
                Board tempBoard = board.copy();
                tempBoard.placeStone(pos.x, pos.y, currentColor);
                int score = minimax(tempBoard, depth - 1, true, aiColor, alpha, beta);
                minScore = Math.min(minScore, score);
                beta = Math.min(beta, score);
                if (beta <= alpha) {
                    break; // Alpha剪枝
                }
            }
            return minScore;
        }
    }
    
    /**
     * 评估棋盘分数
     */
    private int evaluateBoard(Board board, PieceColor aiColor) {
        int score = 0;
        int aiStone = aiColor == PieceColor.BLACK ? 1 : 2;
        int opponentStone = aiStone == 1 ? 2 : 1;
        
        // 评估所有方向的连子情况
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.getStone(x, y) == aiStone) {
                    score += evaluatePosition(board, x, y, aiStone);
                } else if (board.getStone(x, y) == opponentStone) {
                    score -= evaluatePosition(board, x, y, opponentStone);
                }
            }
        }
        
        return score;
    }
    
    /**
     * 评估位置分数
     */
    private int evaluatePosition(Board board, int x, int y, int stone) {
        int score = 0;
        
        // 评估四个方向
        score += evaluateDirection(board, x, y, 1, 0, stone);   // 横向
        score += evaluateDirection(board, x, y, 0, 1, stone);   // 纵向
        score += evaluateDirection(board, x, y, 1, 1, stone);   // 斜向
        score += evaluateDirection(board, x, y, 1, -1, stone);  // 反斜向
        
        return score;
    }
    
    /**
     * 评估指定方向的分数
     */
    private int evaluateDirection(Board board, int x, int y, int dx, int dy, int stone) {
        int count = 1;
        
        // 正向计数
        int nx = x + dx, ny = y + dy;
        while (board.isValidPosition(nx, ny) && board.getStone(nx, ny) == stone) {
            count++;
            nx += dx;
            ny += dy;
        }
        
        // 反向计数
        nx = x - dx; ny = y - dy;
        while (board.isValidPosition(nx, ny) && board.getStone(nx, ny) == stone) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        
        // 根据连子数返回分数
        return switch (count) {
            case 5 -> 100000;  // 五连
            case 4 -> 10000;   // 活四
            case 3 -> 1000;    // 活三
            case 2 -> 100;     // 活二
            default -> 10;     // 单子
        };
    }
    
    /**
     * 寻找必胜点
     */
    private Position findWinningMove(Board board, PieceColor color) {
        int stone = color == PieceColor.BLACK ? 1 : 2;
        
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.isEmpty(x, y)) {
                    Board tempBoard = board.copy();
                    tempBoard.placeStone(x, y, color);
                    if (tempBoard.checkWin(x, y)) {
                        return new Position(x, y);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 寻找防守点
     */
    private Position findBlockingMove(Board board, PieceColor opponentColor) {
        return findWinningMove(board, opponentColor);
    }
    
    /**
     * 寻找最佳落子点
     */
    private Position findBestMove(Board board, PieceColor aiColor) {
        List<Position> candidates = getCandidatePositions(board);
        Position bestPos = null;
        int bestScore = Integer.MIN_VALUE;
        
        for (Position pos : candidates) {
            int score = evaluatePosition(board, pos.x, pos.y, aiColor == PieceColor.BLACK ? 1 : 2);
            if (score > bestScore) {
                bestScore = score;
                bestPos = pos;
            }
        }
        
        return bestPos != null ? bestPos : getRandomMove(board);
    }
    
    /**
     * 获取候选落子位置（周围有子的位置）
     */
    private List<Position> getCandidatePositions(Board board) {
        List<Position> candidates = new ArrayList<>();
        
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.isEmpty(x, y) && hasNeighbor(board, x, y)) {
                    candidates.add(new Position(x, y));
                }
            }
        }
        
        // 如果没有候选位置，返回中心点
        if (candidates.isEmpty()) {
            candidates.add(new Position(7, 7));
        }
        
        return candidates;
    }
    
    /**
     * 检查周围是否有棋子
     */
    private boolean hasNeighbor(Board board, int x, int y) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (board.isValidPosition(nx, ny) && !board.isEmpty(nx, ny)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 获取随机落子位置
     */
    private Position getRandomMove(Board board) {
        List<Position> emptyPositions = new ArrayList<>();
        
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.isEmpty(x, y)) {
                    emptyPositions.add(new Position(x, y));
                }
            }
        }
        
        if (emptyPositions.isEmpty()) {
            return null;
        }
        
        return emptyPositions.get(random.nextInt(emptyPositions.size()));
    }
    
    /**
     * 位置类
     */
    @Data
    @AllArgsConstructor
    public static class Position {
        private int x;
        private int y;
    }
}
