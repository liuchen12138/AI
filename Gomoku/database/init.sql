-- 五子棋在线对弈平台数据库初始化脚本

-- 创建数据库
-- CREATE DATABASE gomoku_db WITH ENCODING 'UTF8';

-- 连接到数据库
-- \c gomoku_db;

-- 用户表
CREATE TABLE IF NOT EXISTS "user" (
    user_id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    level INTEGER NOT NULL DEFAULT 1,
    score INTEGER NOT NULL DEFAULT 0,
    total_games INTEGER NOT NULL DEFAULT 0,
    win_count INTEGER NOT NULL DEFAULT 0,
    lose_count INTEGER NOT NULL DEFAULT 0,
    draw_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 对局表
CREATE TABLE IF NOT EXISTS game (
    game_id BIGSERIAL PRIMARY KEY,
    game_mode VARCHAR(20) NOT NULL CHECK (game_mode IN ('PVP', 'PVE')),
    black_player_id BIGINT NOT NULL REFERENCES "user"(user_id),
    white_player_id BIGINT REFERENCES "user"(user_id),
    ai_difficulty VARCHAR(20) CHECK (ai_difficulty IN ('EASY', 'MEDIUM', 'HARD')),
    status VARCHAR(20) NOT NULL CHECK (status IN ('WAITING', 'PLAYING', 'FINISHED')),
    winner_id BIGINT REFERENCES "user"(user_id),
    end_reason VARCHAR(50) CHECK (end_reason IN ('NORMAL', 'TIMEOUT', 'RESIGN', 'DISCONNECT')),
    black_score_change INTEGER,
    white_score_change INTEGER,
    started_at TIMESTAMP,
    ended_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 棋谱表
CREATE TABLE IF NOT EXISTS game_move (
    move_id BIGSERIAL PRIMARY KEY,
    game_id BIGINT NOT NULL REFERENCES game(game_id) ON DELETE CASCADE,
    move_number INTEGER NOT NULL,
    player_id BIGINT NOT NULL REFERENCES "user"(user_id),
    position_x INTEGER NOT NULL CHECK (position_x BETWEEN 0 AND 14),
    position_y INTEGER NOT NULL CHECK (position_y BETWEEN 0 AND 14),
    move_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    time_used INTEGER
);

-- 排行榜快照表
CREATE TABLE IF NOT EXISTS ranking_snapshot (
    snapshot_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES "user"(user_id),
    rank INTEGER NOT NULL,
    score INTEGER NOT NULL,
    level INTEGER NOT NULL,
    win_rate DECIMAL(5,2),
    snapshot_type VARCHAR(20) NOT NULL CHECK (snapshot_type IN ('DAILY', 'WEEKLY', 'MONTHLY')),
    snapshot_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_user_username ON "user"(username);
CREATE INDEX IF NOT EXISTS idx_user_email ON "user"(email);
CREATE INDEX IF NOT EXISTS idx_user_score ON "user"(score DESC);
CREATE INDEX IF NOT EXISTS idx_user_level ON "user"(level DESC);

CREATE INDEX IF NOT EXISTS idx_game_black_player ON game(black_player_id);
CREATE INDEX IF NOT EXISTS idx_game_white_player ON game(white_player_id);
CREATE INDEX IF NOT EXISTS idx_game_status ON game(status);
CREATE INDEX IF NOT EXISTS idx_game_created_at ON game(created_at DESC);

CREATE INDEX IF NOT EXISTS idx_game_move_game_id ON game_move(game_id);
CREATE INDEX IF NOT EXISTS idx_game_move_composite ON game_move(game_id, move_number);

CREATE INDEX IF NOT EXISTS idx_ranking_snapshot_composite ON ranking_snapshot(snapshot_type, snapshot_date, rank);
CREATE INDEX IF NOT EXISTS idx_ranking_snapshot_user ON ranking_snapshot(user_id);

-- 创建更新时间触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 为用户表创建更新触发器
CREATE TRIGGER update_user_updated_at BEFORE UPDATE ON "user"
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- 插入测试数据（可选）
-- INSERT INTO "user" (username, password_hash, email, level, score)
-- VALUES 
--     ('testuser1', '$2a$10$XxXxXxXxXxXxXxXxXxXxXeXxXxXxXxXxXxXxXxXxXxXxXxXxXxX', 'test1@example.com', 1, 0),
--     ('testuser2', '$2a$10$XxXxXxXxXxXxXxXxXxXxXeXxXxXxXxXxXxXxXxXxXxXxXxXxXxX', 'test2@example.com', 1, 0);

COMMENT ON TABLE "user" IS '用户表';
COMMENT ON TABLE game IS '对局表';
COMMENT ON TABLE game_move IS '棋谱表';
COMMENT ON TABLE ranking_snapshot IS '排行榜快照表';
