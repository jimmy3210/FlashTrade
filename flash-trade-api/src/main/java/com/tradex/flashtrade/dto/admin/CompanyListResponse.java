package com.tradex.flashtrade.dto.admin;

import java.util.List;

public class CompanyListResponse {
    private List<CompanyDto> items;
    private long total;
    private int page;
    private int size;

    public CompanyListResponse() {
    }

    public CompanyListResponse(List<CompanyDto> items, long total, int page, int size) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.size = size;
    }

    public List<CompanyDto> getItems() {
        return items;
    }

    public void setItems(List<CompanyDto> items) {
        this.items = items;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
