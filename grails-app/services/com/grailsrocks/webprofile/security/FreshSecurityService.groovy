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
    def emailConfirmationService
    
    @Transactional
    boolean userExists(userName) {
		findUserByUserName(userName)
    }

    @Transactional
    def findUserByUserName(userName) {
        userClass.findByUserName(userName)
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
        def args = [
            plugin:'fresh-security',
            view:'/email-templates/need-to-set-new-password'
        ]
        emailConfirmationService.sendConfirmation(email, "Set your new password", args, user.userName)
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
        // @todo parameterize this somehow, with i18n
        // @todo update email conf to have "purpose" or "reason" and attach name of an event sink to receive the notifications
        // i.e. reason:'fresh.signup.confirm', handler:'fresh.signup.confirmer', id:user.userName, model:[:]
        // use the reason as a convention to locate the view template?
        def args = [
            plugin:'fresh-security',
            view:'/email-templates/signup-confirmation'
        ]
        emailConfirmationService.sendConfirmation(email, "Confirm your new account", args, user.userName)
    }
    
    Class getUserClass() {
		grailsApplication.getDomainClass(
			SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
    }
}