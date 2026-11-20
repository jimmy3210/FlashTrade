package com.tradex.flashtrade.dto.admin;

import java.util.List;

public record ScopeMappingStatusDto(
        List<UnmappedUserDto> unmappedUsers,
        List<UnmappedAccountDto> unmappedAccounts) {

    public record UnmappedUserDto(Long userId, String username, String role) {}

    public record UnmappedAccountDto(
            Long accountId,
            Long ownerUserId,
            String ownerUsername,
            String brokerCode,
            String accountNo) {}
}
