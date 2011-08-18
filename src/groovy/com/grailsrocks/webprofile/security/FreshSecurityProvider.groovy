package com.grailsrocks.webprofile.security


class FreshSecurityProvider {
    def springSecurityService
    def grailsApplication
    
    def getUserId() {
        def princ = springSecurityService.principal
        if (princ instanceof String) {
            return null
        } else {
            return princ?.username   
        }
    }

    def getUserEmail() {
        def princ = springSecurityService.principal
        return (princ instanceof String) ? null : princ?.email   
    }

    /**
     * Get user info object i.e. email address, other stuff defined by the application
     */
    def getUserInfo() {
        springSecurityService.principal 
    }

    def userHasRole(role) {
        def princ = springSecurityService.principal
        if (princ instanceof String) {
            return grailsApplication.config.plugin.springFreshSecurity.guest.roles
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
    def userIsAllowed(object, action) {
        false // Not implemented yet
    }
}