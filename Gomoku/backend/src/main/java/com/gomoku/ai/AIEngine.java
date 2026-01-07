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
    
    // 记录开始时间
    private long searchStartTime;
    private static final long MAX_SEARCH_TIME = 3000; // 最大搜索时间3秒
    
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
        
        // 60%概率进行防守检测
        if (random.nextDouble() < 0.6) {
            // 检查是否有必胜点
            Position winPos = findWinningMove(board, aiColor);
            if (winPos != null) {
                return winPos;
            }
            
            // 检查是否需要防守
            Position blockPos = findWinningMove(board, opponentColor);
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
        
        // 2. 检查对方是否有必胜点（防守）
        Position blockPos = findWinningMove(board, opponentColor);
        if (blockPos != null) {
            return blockPos;
        }
        
        // 3. 检查是否能形成冲四（进攻）
        Position attackPos = findFourMove(board, aiColor);
        if (attackPos != null) {
            return attackPos;
        }
        
        // 4. 检查是否需要阻止对方形成冲四（防守）
        Position defendPos = findFourMove(board, opponentColor);
        if (defendPos != null) {
            return defendPos;
        }
        
        // 5. 寻找最佳落子点
        return findBestMove(board, aiColor);
    }
    
    /**
     * 困难难度：MinMax算法 + Alpha-Beta剪枝
     */
    private Position calculateHardMove(Board board, PieceColor aiColor) {
        // 设置搜索开始时间
        searchStartTime = System.currentTimeMillis();
        
        int depth = 2; // 降低搜索深度以提高响应速度，同时优化评估函数
        Position bestMove = null;
        int bestScore = Integer.MIN_VALUE;
        
        List<Position> candidates = getOptimizedCandidatePositions(board);
        
        for (Position pos : candidates) {
            // 检查是否超时
            if (System.currentTimeMillis() - searchStartTime > MAX_SEARCH_TIME) {
                break;
            }
            
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
        // 检查是否超时
        if (System.currentTimeMillis() - searchStartTime > MAX_SEARCH_TIME) {
            return evaluateBoard(board, aiColor);
        }
        
        if (depth == 0 || board.isFull()) {
            return evaluateBoard(board, aiColor);
        }
        
        PieceColor currentColor = isMaximizing ? aiColor : 
                (aiColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
        
        List<Position> moves = getOptimizedCandidatePositions(board);
        
        if (isMaximizing) {
            int maxScore = Integer.MIN_VALUE;
            for (Position pos : moves) {
                // 检查是否超时
                if (System.currentTimeMillis() - searchStartTime > MAX_SEARCH_TIME) {
                    return evaluateBoard(board, aiColor);
                }
                
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
                // 检查是否超时
                if (System.currentTimeMillis() - searchStartTime > MAX_SEARCH_TIME) {
                    return evaluateBoard(board, aiColor);
                }
                
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
        
        // 评估所有位置的分数
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.getStone(x, y) == aiStone) {
                    score += evaluatePosition(board, x, y, aiStone);
                } else if (board.getStone(x, y) == opponentStone) {
                    score -= evaluatePosition(board, x, y, opponentStone);
                }
            }
        }
        
        // 额外评估空位的潜在价值（仅评估关键位置以提高性能）
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.isEmpty(x, y) && hasCloseNeighbor(board, x, y, 2)) {
                    // 评估空位对AI的潜在价值
                    Board tempBoard = board.copy();
                    tempBoard.placeStone(x, y, aiColor);
                    int aiPotential = evaluatePosition(tempBoard, x, y, aiStone);
                    
                    // 评估空位对对手的潜在价值
                    tempBoard = board.copy();
                    tempBoard.placeStone(x, y, aiColor == PieceColor.BLACK ? PieceColor.WHITE : PieceColor.BLACK);
                    int opponentPotential = evaluatePosition(tempBoard, x, y, opponentStone);
                    
                    // 综合考虑攻守价值
                    score += (aiPotential - opponentPotential) * 0.1; // 给潜在价值较小的权重
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
        boolean openStart = true; // 开始方向是否开放
        boolean openEnd = true;   // 结束方向是否开放
        
        // 正向计数
        int nx = x + dx, ny = y + dy;
        while (board.isValidPosition(nx, ny) && board.getStone(nx, ny) == stone) {
            count++;
            nx += dx;
            ny += dy;
        }
        // 检查正向是否被阻挡
        if (board.isValidPosition(nx, ny) && board.getStone(nx, ny) != 0) {
            openEnd = false;
        }
        
        // 反向计数
        nx = x - dx; ny = y - dy;
        while (board.isValidPosition(nx, ny) && board.getStone(nx, ny) == stone) {
            count++;
            nx -= dx;
            ny -= dy;
        }
        // 检查反向是否被阻挡
        if (board.isValidPosition(nx, ny) && board.getStone(nx, ny) != 0) {
            openStart = false;
        }
        
        // 根据连子数和开放情况返回分数
        if (count >= 5) {
            return 100000; // 五连
        } else if (count == 4) {
            if (openStart && openEnd) return 10000; // 活四
            else if (openStart || openEnd) return 5000; // 冲四
            else return 500; // 死四
        } else if (count == 3) {
            if (openStart && openEnd) return 1000; // 活三
            else if (openStart || openEnd) return 200; // 眠三
            else return 50; // 死三
        } else if (count == 2) {
            if (openStart && openEnd) return 100; // 活二
            else if (openStart || openEnd) return 20; // 眠二
            else return 5; // 死二
        } else {
            // 对于单子，检查是否形成开放局面
            if (openStart && openEnd) return 10;
            else return 5;
        }
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
     * 获取优化的候选落子位置（只考虑关键区域，减少搜索空间）
     */
    private List<Position> getOptimizedCandidatePositions(Board board) {
        List<Position> candidates = new ArrayList<>();
        
        // 优先考虑离现有棋子较近的位置，限制在2格范围内
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.isEmpty(x, y) && hasCloseNeighbor(board, x, y, 2)) {
                    candidates.add(new Position(x, y));
                }
            }
        }
        
        // 如果没有近邻位置，扩展到3格范围
        if (candidates.isEmpty()) {
            for (int x = 0; x < Board.BOARD_SIZE; x++) {
                for (int y = 0; y < Board.BOARD_SIZE; y++) {
                    if (board.isEmpty(x, y) && hasCloseNeighbor(board, x, y, 3)) {
                        candidates.add(new Position(x, y));
                    }
                }
            }
        }
        
        // 如果还是没有候选位置，返回中心点
        if (candidates.isEmpty()) {
            candidates.add(new Position(7, 7));
        }
        
        // 限制候选位置数量以提高性能
        if (candidates.size() > 20) {
            // 选择评分最高的前20个位置
            candidates.sort((p1, p2) -> {
                Board tempBoard1 = board.copy();
                Board tempBoard2 = board.copy();
                tempBoard1.placeStone(p1.x, p1.y, PieceColor.BLACK); // 临时使用黑棋计算
                tempBoard2.placeStone(p2.x, p2.y, PieceColor.BLACK);
                int score1 = evaluatePosition(tempBoard1, p1.x, p1.y, 1);
                int score2 = evaluatePosition(tempBoard2, p2.x, p2.y, 1);
                return Integer.compare(score2, score1); // 降序排列
            });
            return candidates.subList(0, 20);
        }
        
        return candidates;
    }
    
    /**
     * 检查指定范围内是否有棋子
     */
    private boolean hasCloseNeighbor(Board board, int x, int y, int range) {
        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
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
     * 寻找能形成四子的落子点
     */
    private Position findFourMove(Board board, PieceColor color) {
        int stone = color == PieceColor.BLACK ? 1 : 2;
        
        for (int x = 0; x < Board.BOARD_SIZE; x++) {
            for (int y = 0; y < Board.BOARD_SIZE; y++) {
                if (board.isEmpty(x, y)) {
                    Board tempBoard = board.copy();
                    tempBoard.placeStone(x, y, color);
                    
                    // 检查是否形成了四子
                    if (hasFourInLine(tempBoard, x, y, stone)) {
                        return new Position(x, y);
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * 检查是否形成了四子连线
     */
    private boolean hasFourInLine(Board board, int x, int y, int stone) {
        // 检查四个方向
        int count1 = countDirection(board, x, y, 1, 0, stone); // 横向
        int count2 = countDirection(board, x, y, 0, 1, stone); // 纵向
        int count3 = countDirection(board, x, y, 1, 1, stone); // 斜向
        int count4 = countDirection(board, x, y, 1, -1, stone); // 反斜向
        
        // 如果任一方向形成4子或更多，则返回true
        return count1 >= 4 || count2 >= 4 || count3 >= 4 || count4 >= 4;
    }
    
    /**
     * 计算指定方向的连子数
     */
    private int countDirection(Board board, int x, int y, int dx, int dy, int stone) {
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
        
        return count;
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
