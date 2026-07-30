package com.tradex.flashtrade.service;

import com.tradex.flashtrade.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserLookupService {

    private final UserRepository userRepository;

    public UserLookupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new org.springframework.security.core.userdetails
                        .UsernameNotFoundException("用户不存在: " + username))
                .getId();
    }
}
