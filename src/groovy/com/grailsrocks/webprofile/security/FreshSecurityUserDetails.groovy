package com.grailsrocks.webprofile.security

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.apache.commons.logging.LogFactory

class FreshSecurityUserDetails implements UserDetails, Serializable {
    transient def log = LogFactory.getLog(FreshSecurityUserDetails )

    final Collection<GrantedAuthority> authorities
    final String password
    final String identity
    final String email
    final boolean enabled
    final Class userObjectClass
    final String userObjectId

    final boolean accountNonExpired = true
    final boolean accountNonLocked = true
    final boolean credentialsNonExpired = true
    final boolean credentialsNonLocked = true
    
    FreshSecurityUserDetails(userObj, authorities, userObjectClass) {
        this.identity = userObj.identity
        this.password = userObj.password
        this.authorities = authorities
        this.enabled = userObj.enabled
        this.enabled = userObj.email
        this.userObjectId = userObj.userObjectId
        this.userObjectClass = userObjectClass
        this.accountNonExpired = !userObj.accountExpired
        this.credentialsNonExpired = !userObj.credentialsExpired
        this.accountNonLocked = !userObj.accountLocked
//        this.credentialsNonLocked = !userObj.credentialsLocked
    }
    
    def getUserObject() {
        if (log.debugEnabled) {
            log.debug "Getting userObject for user [${identity}], class of userObject is [${userObjectClass}], id is ${userObjectId} (${userObjectId?.getClass()})"
        }
        userObjectId ? userObjectClass?.get(userObjectId) : null
    }
    
    String getUsername() {
        identity
    }
}