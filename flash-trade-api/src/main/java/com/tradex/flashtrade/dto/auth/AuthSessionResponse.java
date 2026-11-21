package com.tradex.flashtrade.dto.auth;

import java.util.List;

public record AuthSessionResponse(String username, List<String> roles) {
}
