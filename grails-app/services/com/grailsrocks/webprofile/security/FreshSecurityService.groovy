package com.grailsrocks.webprofile.security

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.transaction.annotation.*

class FreshSecurityService {
    static transactional = false
    
    def grailsApplication
    
    @Transactional
    boolean userExists(userName) {
		findUserByUserName(userName)
    }

    def findUserByUserName(userName) {
        // @todo parameterize this field name?
        userClass.findByUserName(userName)
    }

    Class getUserClass() {
		grailsApplication.getDomainClass(
			SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
    }
}