package com.tradex.flashtrade.service;

import com.tradex.flashtrade.entity.OperationLog;
import com.tradex.flashtrade.entity.Role;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.repository.OperationLogRepository;
import com.tradex.flashtrade.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
public class SuperAdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SuperAdminBootstrapRunner.class);

    private final UserRepository userRepository;
    private final OperationLogRepository operationLogRepository;
    private final boolean enabled;
    private final String username;

    public SuperAdminBootstrapRunner(
            UserRepository userRepository,
            OperationLogRepository operationLogRepository,
            @Value("${app.security.bootstrap-super-admin.enabled:false}") boolean enabled,
            @Value("${app.security.bootstrap-super-admin.username:}") String username) {
        this.userRepository = userRepository;
        this.operationLogRepository = operationLogRepository;
        this.enabled = enabled;
        this.username = username;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) return;
        if (username == null || username.isBlank()) {
            throw new IllegalStateException(
                    "Super-admin bootstrap is enabled but no bootstrap username was configured");
        }

        User user = userRepository.findByUsernameForUpdate(username.trim())
                .orElseThrow(() -> new IllegalStateException(
                        "Configured super-admin bootstrap user does not exist"));
        if (!user.isEnabled()) {
            throw new IllegalStateException(
                    "Configured super-admin bootstrap user must be enabled");
        }
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            log.info("Configured super-admin bootstrap user is already provisioned");
            return;
        }
        if (user.getRole() != Role.ROLE_ADMIN) {
            throw new IllegalStateException(
                    "Super-admin bootstrap can only promote an existing ROLE_ADMIN user");
        }
        if (userRepository.existsByRole(Role.ROLE_SUPER_ADMIN)) {
            throw new IllegalStateException(
                    "A super-admin already exists; refusing to bootstrap another account");
        }

        user.setRole(Role.ROLE_SUPER_ADMIN);
        userRepository.save(user);
        operationLogRepository.save(OperationLog.builder()
                .operatorUsername("system-bootstrap")
                .operation("BOOTSTRAP_SUPER_ADMIN")
                .targetType("USER")
                .targetId(user.getId())
                .targetName(user.getUsername())
                .oldValue("\"ROLE_ADMIN\"")
                .newValue("\"ROLE_SUPER_ADMIN\"")
                .changeSummary("Granted the initial super-admin role through deployment bootstrap")
                .requestPath("application-startup")
                .requestMethod("BOOTSTRAP")
                .httpStatus(200)
                .userAgent("environment-bootstrap")
                .createdAt(LocalDateTime.now())
                .build());

        log.warn("Initial super-admin role granted to configured user; disable bootstrap configuration");
    }
}
