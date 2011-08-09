package com.grailsrocks.webprofile.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class FreshSecurityUserDetails implements UserDetails {
    final Collection<GrantedAuthority> authorities
    final String password
    final String username
    final boolean enabled

    final boolean accountNonExpired = true
    final boolean accountNonLocked = true
    final boolean credentialsNonExpired = true
    
    FreshSecurityUserDetails(userObj, authorities) {
        this.username = userObj.userName
        this.password = userObj.password
        this.authorities = authorities
        this.enabled = userObj.enabled
        this.accountNonExpired = !userObj.accountExpired
        this.accountNonLocked = !userObj.accountLocked
        this.credentialsNonLocked = !userObj.credentialsLocked
    }
}