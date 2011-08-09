package com.grailsrocks.webprofile.security

class FreshSecurityService {
    static transactional = false
    
    def grailsApplication
    
    void configureSpringSecurity() {
        // Get our config values and use them to apply to Spring security's config by
        // modifying global config
        def config = grailsApplication.config
        
        if (config.grails.plugins.springsecurity.interceptUrlMap instanceof ConfigObject) {
            // This needs to be set as the default, that use can override
            config.grails.plugins.springsecurity.interceptUrlMap = [
               '/admin/**':     ['ROLE_ADMIN'],
               '/static/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/js/**':        ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/css/**':       ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/images/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/auth/**':     ['IS_AUTHENTICATED_ANONYMOUSLY'],
            ]
        }
        
        if (config.grails.plugins.springsecurity.securityConfigType instanceof ConfigObject) {
            config.grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
        }
        if (config.grails.plugins.springsecurity.userLookup.userDomainClassName instanceof ConfigObject) {
            config.grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.grailsrocks.webprofile.security.User'
        }
        if (config.grails.plugins.springsecurity.userLookup.authorityJoinClassName instanceof ConfigObject) {
            config.grails.plugins.springsecurity.userLookup.authorityJoinClassName = 'com.grailsrocks.webprofile.security.UserRole'
        }
        if (config.grails.plugins.springsecurity.userLookup.className instanceof ConfigObject) {
            config.grails.plugins.springsecurity.authority.className = 'com.grailsrocks.webprofile.security.User.Role'
        }
        if (config.grails.plugins.springsecurity.userLookup.usernamePropertyName instanceof ConfigObject) {
            config.grails.plugins.springsecurity.userLookup.usernamePropertyName = 'username'
        }
        
    }
}