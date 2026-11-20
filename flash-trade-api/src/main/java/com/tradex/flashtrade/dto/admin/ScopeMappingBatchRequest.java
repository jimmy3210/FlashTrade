package com.tradex.flashtrade.dto.admin;

import java.util.List;

import jakarta.validation.Valid;

public class ScopeMappingBatchRequest {
    @Valid
    private List<UserCompanyMapping> users = List.of();
    @Valid
    private List<AccountOwnerMapping> accounts = List.of();

    public List<UserCompanyMapping> getUsers() {
        return users == null ? List.of() : users;
    }

    public void setUsers(List<UserCompanyMapping> users) {
        this.users = users;
    }

    public List<AccountOwnerMapping> getAccounts() {
        return accounts == null ? List.of() : accounts;
    }

    public void setAccounts(List<AccountOwnerMapping> accounts) {
        this.accounts = accounts;
    }

    public record UserCompanyMapping(Long userId, Long companyId) {}

    public record AccountOwnerMapping(Long accountId, Long userId) {}
}
