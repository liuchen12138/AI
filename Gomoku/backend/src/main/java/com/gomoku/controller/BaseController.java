package com.gomoku.controller;

import com.gomoku.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Controller基类，提供通用功能
 */
public abstract class BaseController {
    
    @Autowired
    protected JwtUtil jwtUtil;
    
    /**
     * 从Authorization header中提取用户ID
     */
    protected Long getUserIdFromToken(String authorizationHeader) {
        String jwt = authorizationHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(jwt);
    }
}
