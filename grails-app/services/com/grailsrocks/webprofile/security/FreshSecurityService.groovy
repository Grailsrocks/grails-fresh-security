package com.grailsrocks.webprofile.security

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.transaction.annotation.*
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource

/*
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
*/

class FreshSecurityService {
    static transactional = false
    
    def saltSource
    def grailsApplication
    def springSecurityService
    
    @Transactional
    boolean userExists(userName) {
		findUserByUserName(userName)
    }

    @Transactional
    def findUserByUserName(userName) {
        // @todo parameterize this field name?
        userClass.findByUserName(userName)
    }

    @Transactional
    def createNewUser(userInfo, request = null) {
        def conf = grailsApplication.config.plugin.freshSecurity
        boolean confirmEmail = conf.confirm.email.on.signup
        boolean lockedUntilConfirmEmail = conf.confirm.account.locked.until.email.confirm
        
        String salt = saltSource instanceof NullSaltSource ? null : userInfo.userName
		String password = springSecurityService.encodePassword(userInfo.password, salt)
		def user = new SecUser(
		        userName: userInfo.userName,
				password: password, 
				email: userInfo.email,
				accountLocked: confirmEmail ? lockedUntilConfirmEmail : false, 
				enabled: true,
				roleList: conf.'default'.roles)
				
		if (user.save()) {
            if (log.debugEnabled) {
                log.debug "User signing up, saved user: ${user.userName}"
            }
		    if (confirmEmail) {
                if (log.debugEnabled) {
                    log.debug "User signing up, sending email confirmation: ${user.userName}"
                }
                if (emailConfirmationService) {
    		        emailConfirmationService?.sendConfirmation()
                } else {
                    throw new IllegalArgumentException("Spring Fresh Security is configured to send email confirmations but the email-confirmation plugin is not installed")
                }
	        }
	        if (request) {
    	        request.session['spring.fresh.security.new.sign.up'] = true
    	        request.session['spring.fresh.security.email.confirm.pending'] = confirmEmail
	        }

            // Force the new user to be logged in if email confirmation is not required
            if (!confirmEmail) {
                if (log.debugEnabled) {
                    log.debug "User signing up, logging them in automatically: ${user.userName}"
                }
    		    springSecurityService.reauthenticate user.userName
		    }
		}

		return user
    }

    Class getUserClass() {
		grailsApplication.getDomainClass(
			SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
    }
}