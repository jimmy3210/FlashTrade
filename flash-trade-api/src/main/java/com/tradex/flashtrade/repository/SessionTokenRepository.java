package com.tradex.flashtrade.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.tradex.flashtrade.entity.SessionToken;

@Repository
public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {

    /**
     * 查找用户在指定 broker account 下的有效 token
     */
    Optional<SessionToken> findByUserIdAndBrokerAccountIdAndAuthType(
            Long userId, String brokerAccountId, String authType);

    /**
     * 查找用户的所有有效 token（某个 broker account）
     */
    List<SessionToken> findByUserIdAndBrokerAccountIdOrderByCreatedAtDesc(
            Long userId, String brokerAccountId);

    /**
     * 查找用户的指定类型的所有 token
     */
    List<SessionToken> findByUserIdAndAuthTypeOrderByCreatedAtDesc(
            Long userId, String authType);

    /**
     * 查找所有已过期的 token（用于清理）
     */
    @Query("SELECT t FROM SessionToken t WHERE t.authType = 'BEARER' " +
            "AND t.expiresAt IS NOT NULL AND t.expiresAt < CURRENT_TIMESTAMP " +
            "OR t.authType = 'OAUTH1A' " +
            "AND t.oauthExpiresAt IS NOT NULL AND t.oauthExpiresAt < CURRENT_TIMESTAMP")
    List<SessionToken> findAllExpiredTokens();

    /**
     * 查找即将过期的 token（5分钟内）
     * 用于主动刷新 OAuth 2.0 token
     */
    @Query("SELECT t FROM SessionToken t WHERE t.authType = 'BEARER' " +
            "AND t.expiresAt IS NOT NULL " +
            "AND t.expiresAt < CURRENT_TIMESTAMP + 5 MINUTE " +
            "AND t.expiresAt > CURRENT_TIMESTAMP")
    List<SessionToken> findTokensExpiringInFiveMinutes();

    /**
     * 查找某个 broker account 下最新的 token
     */
    Optional<SessionToken> findFirstByBrokerAccountIdAndAuthTypeOrderByCreatedAtDesc(
            String brokerAccountId, String authType);

    /**
     * 查找特定用户和 broker 的所有 token
     */
    List<SessionToken> findByUserIdAndBrokerIdOrderByCreatedAtDesc(
            Long userId, Long brokerId);

    /**
     * 删除已过期的 token（清理任务）
     */
    long deleteByExpiresAtBefore(LocalDateTime dateTime);

    long deleteByOauthExpiresAtBefore(LocalDateTime dateTime);
}
