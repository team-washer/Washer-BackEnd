package com.washer.Things.global.auth;

import com.washer.Things.domain.user.entity.enums.Role;
import com.washer.Things.global.entity.UserCredential;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

import java.util.List;
import java.util.stream.Collectors;

public class AuthDetails implements UserDetails {

    private final UserCredential credential;
    private final List<Role> roles;

    public AuthDetails(UserCredential credential, List<Role> roles) {
        this.credential = credential;
        this.roles = roles;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getPermission()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return credential.getEncodedPassword();
    }

    @Override
    public String getUsername() {
        return credential.getEmail();
    }
}
