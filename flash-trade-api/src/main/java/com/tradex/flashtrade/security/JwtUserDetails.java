package com.tradex.flashtrade.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 用户详情
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserDetails implements UserDetails {
    
    private Long id;
    private String username;
    private String password;
    private String role;
    private Collection<? extends GrantedAuthority> authorities;
    private boolean enabled;
    private boolean accountNonLocked;
    private boolean accountNonExpired;
    private boolean credentialsNonExpired;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }
    
    @Override
    public String getPassword() {
        return this.password;
    }
    
    @Override
    public String getUsername() {
        return this.username;
    }
    
    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }
    
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}
