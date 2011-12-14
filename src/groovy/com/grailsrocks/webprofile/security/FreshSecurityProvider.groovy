package com.grailsrocks.webprofile.security

import org.grails.plugin.platform.security.SecurityBridge

class FreshSecurityProvider implements SecurityBridge {
    def springSecurityService
    def grailsApplication
    
    String getUserName() {
        def princ = springSecurityService.principal
        if (princ instanceof String) {
            return null
        } else {
            return princ?.username   
        }
    }

    /**
     * Get user info object i.e. email address, other stuff defined by the application
     */
    def getUserInfo() {
        springSecurityService.principal 
    }

    boolean userHasRole(role) {
        def princ = springSecurityService.principal
        if (princ instanceof String) {
            return grailsApplication.config.plugin.freshSecurity.guest.roles
        }
        def auths = []
        def authorities = princ?.authorities
        if (authorities) {
            auths.addAll(authorities)
        }
        return auths ?: ['ROLE_GUEST']
    }

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    boolean userIsAllowed(object, action) {
        false // Not implemented yet
    }
    
    String createLink(String action) {
        "LINK HERE - NOT IMPLEMENTED"
    }
}