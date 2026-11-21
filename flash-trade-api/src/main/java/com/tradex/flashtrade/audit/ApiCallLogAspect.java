package com.tradex.flashtrade.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.tradex.flashtrade.security.AuthService;
import com.tradex.flashtrade.security.ClientIpResolver;
import com.tradex.flashtrade.security.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * API 调用日志 AOP 拦截器
 * 自动记录所有 /admin 和 /api/v1/trade 的操作
 */
@Aspect
@Component
@Order(0)
@Slf4j
public class ApiCallLogAspect {
    
    @Autowired private AuditLogger auditLogger;
    @Autowired(required = false)
    @Qualifier("securityAuthService")
    private AuthService authService;
    @Autowired(required = false) private JwtTokenProvider jwtTokenProvider;
    @Autowired private ClientIpResolver clientIpResolver;
    
    /**
     * 拦截所有管理端和交易端的 API 调用
     */
    @Around("execution(* com.tradex.flashtrade.controller.admin..*(..)) || " +
            "execution(* com.tradex.flashtrade.controller.AdminUserController.*(..)) || " +
            "execution(* com.tradex.flashtrade.controller.BrokerAccountAdminController.*(..)) || " +
            "execution(* com.tradex.flashtrade.controller.TradeController.*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = null;
        long startTime = System.currentTimeMillis();
        
        try {
            request = getRequest();
            if (request == null) {
                return joinPoint.proceed();
            }
            
            // 获取用户信息
            Long userId = null;
            String username = null;
            try {
                if (authService != null) {
                    userId = authService.getCurrentUserId();
                    username = authService.getCurrentUsername();
                }
            } catch (Exception e) {
                // 用户信息获取失败，可能是未登录，继续处理
                log.debug("Could not get current user info", e);
            }
            
            // 获取请求信息
            String httpMethod = request.getMethod();
            String endpoint = request.getRequestURI();
            String requestIp = clientIpResolver.resolve(request);
            String userAgent = request.getHeader("User-Agent");
            
            // 判断是否为关键操作（交易、管理操作）
            boolean isCritical = endpoint.contains("/trade/order") || 
                                endpoint.contains("/admin/broker-accounts") ||
                                endpoint.contains("/admin/users") ||
                                endpoint.contains("/admin/orders");
            
            // 执行方法
            Object result = joinPoint.proceed();
            
            // 记录成功的调用
            long responseTime = System.currentTimeMillis() - startTime;
            auditLogger.logApiCall(userId, username, httpMethod, endpoint, 
                    null, null, 200, responseTime, requestIp, userAgent, isCritical);
            
            if (isCritical && responseTime > 1000) {
                log.warn("⏱️  Slow API call detected: {} {} took {}ms",
                        httpMethod, endpoint, responseTime);
            }
            
            return result;
            
        } catch (Exception e) {
            // 记录失败的调用
            long responseTime = System.currentTimeMillis() - startTime;
            if (request != null) {
                String httpMethod = request.getMethod();
                String endpoint = request.getRequestURI();
                String requestIp = clientIpResolver.resolve(request);
                String userAgent = request.getHeader("User-Agent");
                
                log.error("❌ API call error: {} {} | error: {} | time: {}ms",
                        httpMethod, endpoint, e.getMessage(), responseTime);
                
                try {
                    Long userId = null;
                    String username = null;
                    if (authService != null) {
                        userId = authService.getCurrentUserId();
                        username = authService.getCurrentUsername();
                    }
                    
                    auditLogger.logApiCall(userId, username, httpMethod, endpoint,
                            null, null, 500, responseTime, requestIp, userAgent, true);
                } catch (Exception ex) {
                    log.error("Failed to log API error", ex);
                }
            }
            
            throw e;
        }
    }
    
    private HttpServletRequest getRequest() {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest();
            }
        } catch (Exception e) {
            log.debug("Could not get HTTP request", e);
        }
        return null;
    }
    
}
