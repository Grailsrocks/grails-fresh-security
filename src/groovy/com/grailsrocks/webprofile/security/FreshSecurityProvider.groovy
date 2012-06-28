package com.grailsrocks.webprofile.security

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils        
import org.grails.plugin.platform.security.SecurityBridge
import org.apache.commons.logging.LogFactory

class FreshSecurityProvider implements SecurityBridge {
    def log = LogFactory.getLog(FreshSecurityProvider)

    def springSecurityService
    def grailsApplication
    
    String getProviderName() {
        "Fresh Security"
    }
    
    String getUserIdentity() {
        def princ = springSecurityService.principal
        if (log.debugEnabled) {
            log.debug "Getting current user identity, principal is [$princ] (${princ?.getClass()})"
        }
        if (princ instanceof String) {
            return null
        } else {
            if (log.debugEnabled) {
                log.debug "Getting current user identity, returning principal identity [${princ?.identity}] (${princ?.identity.getClass()})"
            }
            return princ?.identity   
        }
    }

    /**
     * Get user info object i.e. email address, other stuff defined by the application
     */
    def getUserInfo() {
        def princ = springSecurityService.principal 
        if (log.debugEnabled) {
            log.debug "Getting current user info, principal is [${princ}] (${princ?.getClass()})"
        }
        def value = princ instanceof String ? null : princ
        if (log.debugEnabled) {
            log.debug "Getting current user info, returning [${value}] (${value?.getClass()})"
        }
        return value
    }

    boolean userHasRole(role) {
        SpringSecurityUtils.ifAnyGranted(role.toString())
/*        def princ = springSecurityService.principal
        if (princ instanceof String) {
            return grailsApplication.config.plugin.freshSecurity.guest.roles
        }
        def auths = []
        def authorities = princ?.authorities
        if (authorities) {
            auths.addAll(authorities)
        }
        return auths ?: ['ROLE_GUEST']
*/
    }

    /**
     * Can the current user access this object to perform the named action?
     * @param object The object, typically domain but we don't care what
     * @param action Some application-defined action string i.e. "view" or "edit"
     */
    boolean userIsAllowed(object, action) {
        false // Not implemented yet
    }
    
    Map createLink(String action) {
        def r = [controller:'freshSecurityAuth']
        switch (action) {
            case 'login': r.action = "login"; break;
            case 'logout': r.action = "logout"; break;
            case 'signup': r.action = "signup"; break;
            default: 
                throw new IllegalArgumentException('Security link [$action] is not recognized')
        }
        return r
    }
    
    def withUser(identity, Closure closure) {
        SpringSecurityUtils.doWithAuth(identity, closure)
    }
}