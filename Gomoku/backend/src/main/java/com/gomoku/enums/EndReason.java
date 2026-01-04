package com.gomoku.enums;

public enum EndReason {
    /**
     * 正常结束（五子连珠）
     */
    NORMAL,
    
    /**
     * 超时判负
     */
    TIMEOUT,
    
    /**
     * 认输
     */
    RESIGN,
    
    /**
     * 断线超时
     */
    DISCONNECT
}
