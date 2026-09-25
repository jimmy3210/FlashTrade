package com.tradex.flashtrade.dto.admin;

import java.time.LocalDateTime;

public class CompanyDto {
    private Long id;
    private String name;
    private Boolean isActive;
    private Integer groupCount;
    private LocalDateTime createdAt;

    public CompanyDto() {
    }

    public CompanyDto(Long id, String name, Boolean isActive, Integer groupCount, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.isActive = isActive;
        this.groupCount = groupCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Integer getGroupCount() {
        return groupCount;
    }

    public void setGroupCount(Integer groupCount) {
        this.groupCount = groupCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
