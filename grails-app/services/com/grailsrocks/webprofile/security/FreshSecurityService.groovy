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
    
    static final String CONFIRMATION_HANDLER_PASSWORDRESET = 'plugin.fresh.security.password.reset'
    static final String CONFIRMATION_HANDLER_SIGNUP_CONFIRM = 'plugin.fresh.security.signup.confirm'
    
    def saltSource
    def grailsApplication
    def springSecurityService
    def emailConfirmationService
    
    @Transactional
    boolean userExists(userName) {
		findUserByUserName(userName)
    }

    @Transactional
    def findUserByUserName(userName) {
        userClass.findByUserName(userName)
    }

    void init() {
        emailConfirmationService.addConfirmedHandler(CONFIRMATION_HANDLER_PASSWORDRESET) {
            println "IN PASSWORD RESET HANDLER!"
            [uri:'/']
        }
        emailConfirmationService.addConfirmedHandler(CONFIRMATION_HANDLER_SIGNUP_CONFIRM) {
            println "IN SIGNUP CONFIRM!"
            [uri:'/']
        }
    }
    
    /**
     * Invalidate user's password so that the chosen reset flow starts.
     * Configuration determines reset flow, which can be one of "generate", "setnew" or "reminder"
     */
    void userForgotPassword(userName) {
		def u = findUserByUserName(userName)
		if (u) {
		    u.credentialsExpired = true
		    u.save(flush:true)
		} 
        
        def resetMode = grailsApplication.config.plugin.freshSecurity.password.reset.mode

        switch (resetMode) {
            case FreshSecurityService.PASSWORD_RESET_MODE_GENERATE:
                throw new IllegalArgumentException("Not implemented!")
                break;
            case FreshSecurityService.PASSWORD_RESET_MODE_SETNEW:
                sendUserPasswordResetNotification(u)
                break;
            default:
                throw new IllegalArgumentException("No password reset mode known with id [${resetMode}]")
        }
    }

    void sendUserPasswordResetNotification(user) {
        emailConfirmationService.sendConfirmation(
            to:email, 
            subject:"Set your new password", 
            plugin:'fresh-security', 
            view:'/email-templates/password-reset-confirmation',
            id:user.userName,
            handler:CONFIRMATION_HANDLER_PASSWORDRESET)
    }
    
    def setUserAsLoggedIn(userName) {
        springSecurityService.reauthenticate userName
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
                    sendSignupConfirmation(user)
                } else {
                    throw new IllegalArgumentException("Fresh Security is configured to send email confirmations but the email-confirmation plugin is not installed")
                }
	        }
	        if (request) {
    	        request.session['plugin.fresh.security.new.sign.up'] = true
    	        request.session['plugin.fresh.security.email.confirm.pending'] = confirmEmail
	        }

            // Force the new user to be logged in if email confirmation is not required
            if (!confirmEmail) {
                if (log.debugEnabled) {
                    log.debug "User signing up, logging them in automatically: ${user.userName}"
                }
    		    setUserAsLoggedIn(user.userName)
		    }
		}

		return user
    }

    void sendSignupConfirmation(user) {
        emailConfirmationService.sendConfirmation(to:user.email, subject:"Confirm your new account", 
            plugin:'fresh-security', 
            view:'/email-templates/signup-confirmation',
            id:user.userName,
            handler:CONFIRMATION_HANDLER_SIGNUP_CONFIRM)
    }
    
    Class getUserClass() {
		grailsApplication.getDomainClass(
			SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
    }
}