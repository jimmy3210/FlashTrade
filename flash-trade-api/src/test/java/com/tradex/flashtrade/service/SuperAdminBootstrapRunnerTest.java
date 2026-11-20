package com.tradex.flashtrade.service;

import com.tradex.flashtrade.entity.OperationLog;
import com.tradex.flashtrade.entity.Role;
import com.tradex.flashtrade.entity.User;
import com.tradex.flashtrade.repository.OperationLogRepository;
import com.tradex.flashtrade.repository.UserRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SuperAdminBootstrapRunnerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final OperationLogRepository operationLogRepository = mock(OperationLogRepository.class);

    @Test
    void promotesConfiguredEnabledAdminAndRecordsAudit() {
        User user = user(Role.ROLE_ADMIN, true);
        when(userRepository.findByUsernameForUpdate("bootstrap-user")).thenReturn(Optional.of(user));
        when(userRepository.existsByRole(Role.ROLE_SUPER_ADMIN)).thenReturn(false);

        runner(true, "bootstrap-user").run(null);

        assertThat(user.getRole()).isEqualTo(Role.ROLE_SUPER_ADMIN);
        verify(userRepository).save(user);
        verify(operationLogRepository).save(argThat(log ->
                log.getOperation().equals("BOOTSTRAP_SUPER_ADMIN")
                        && log.getTargetId().equals(user.getId())
                        && log.getNewValue().contains("ROLE_SUPER_ADMIN")));
    }

    @Test
    void doesNothingWhenBootstrapIsDisabled() {
        runner(false, "").run(null);

        verifyNoInteractions(userRepository, operationLogRepository);
    }

    @Test
    void rejectsBootstrapWhenTargetIsNotAnAdmin() {
        User user = user(Role.ROLE_TRADER, true);
        when(userRepository.findByUsernameForUpdate("bootstrap-user")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> runner(true, "bootstrap-user").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ROLE_ADMIN");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(operationLogRepository);
    }

    @Test
    void refusesToCreateAnotherSuperAdmin() {
        User user = user(Role.ROLE_ADMIN, true);
        when(userRepository.findByUsernameForUpdate("bootstrap-user")).thenReturn(Optional.of(user));
        when(userRepository.existsByRole(Role.ROLE_SUPER_ADMIN)).thenReturn(true);

        assertThatThrownBy(() -> runner(true, "bootstrap-user").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, never()).save(any());
        verifyNoInteractions(operationLogRepository);
    }

    @Test
    void rejectsDisabledTarget() {
        User user = user(Role.ROLE_ADMIN, false);
        when(userRepository.findByUsernameForUpdate("bootstrap-user")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> runner(true, "bootstrap-user").run(null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be enabled");

        verify(userRepository, never()).save(any());
    }

    private SuperAdminBootstrapRunner runner(boolean enabled, String username) {
        return new SuperAdminBootstrapRunner(
                userRepository, operationLogRepository, enabled, username);
    }

    private User user(Role role, boolean enabled) {
        User user = new User();
        user.setId(42L);
        user.setUsername("bootstrap-user");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }
}
