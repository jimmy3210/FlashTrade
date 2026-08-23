package com.tradex.flashtrade.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 认证服务
 * 提供获取当前用户信息的方法
 */
@Service("securityAuthService")
@Slf4j
public class AuthService {
    
    /**
     * 获取当前用户ID
     */
    public Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
                JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
                return userDetails.getId();
            }
        } catch (Exception e) {
            log.debug("Failed to get current user ID", e);
        }
        return null;
    }
    
    /**
     * 获取当前用户名
     */
    public String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Failed to get current username", e);
        }
        return null;
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof JwtUserDetails) {
                JwtUserDetails userDetails = (JwtUserDetails) authentication.getPrincipal();
                return "ADMIN".equals(userDetails.getRole());
            }
        } catch (Exception e) {
            log.debug("Failed to check admin status", e);
        }
        return false;
    }
}
