package com.grailsrocks.webprofile.security

import grails.util.Environment
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.transaction.annotation.*
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder

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

class FreshSecurityService implements InitializingBean {
    static transactional = false
    
    static final String PLUGIN_SCOPE = 'plugin.freshSecurity.'
    static final String CONFIRMATION_HANDLER_PASSWORDRESET = PLUGIN_SCOPE+'password.reset'
    static final String CONFIRMATION_HANDLER_SIGNUP_CONFIRM = PLUGIN_SCOPE+'signup.confirm'
    
    static final String SESSION_VAR_PASSWORD_RESET_MODE = PLUGIN_SCOPE+'password.reset.mode'
    static final String SESSION_VAR_PASSWORD_RESET_IDENTITY = PLUGIN_SCOPE+'password.reset.identity'
    
    static final String PASSWORD_RESET_MODE_GENERATE = "generate"
    static final String PASSWORD_RESET_MODE_SETNEW = "setnew"
    
    def saltSource
    def grailsApplication
    def springSecurityService
    def emailConfirmationService
    def grailsUiHelper
    
    @Transactional
    boolean userExists(identity) {
		findUserByIdentity(identity)
    }

    @Transactional
    def findUserByIdentity(identity) {
        userClass.findByUserName(identity)
    }

    @Transactional
    def findUserByEmail(email) {
        userClass.findByEmail(email)
    }

    void afterPropertiesSet() {
        init()
    }
    
    void init() {
        emailConfirmationService.addConfirmedHandler(this.&handlePasswordResetConfirmation, CONFIRMATION_HANDLER_PASSWORDRESET)
        emailConfirmationService.addConfirmedHandler(this.&handleSignupConfirmation, CONFIRMATION_HANDLER_SIGNUP_CONFIRM)
    }

    /**
     * Check user exists, then set session var to indicate that user is in "reset mode" and
     * redirect to password set screen
     */
    @Transactional
    def handlePasswordResetConfirmation(args) {
        if (findUserByIdentity(args.id)) { 
            def session = RequestContextHolder.requestAttributes.session
    	    
            session[SESSION_VAR_PASSWORD_RESET_MODE] = true
            session[SESSION_VAR_PASSWORD_RESET_IDENTITY] = args.id
            return [controller:'auth', action:'resetPassword']
        } else {
            return [controller:'auth', action:'badRequest']
        }
    }

    /** 
     * Mark their account as enabled, redirect them to login screen
     */
    @Transactional
    def handleSignupConfirmation(args) {
        def user = findUserByIdentity(args.id)
        if (user) { 
            user.accountLocked = false
            
            grailsUiHelper.displayFlashMessage text:PLUGIN_SCOPE+'signup.confirm.completed'
            
            return pluginConfig.post.login.url
        } else {
            return pluginConfig.bad.confirmation.url
        }
    }
    
    /**
     * Invalidate user's password so that the chosen reset flow starts.
     * Configuration determines reset flow, which can be one of "generate", "setnew" or "reminder"
     */
    @Transactional
    boolean userForgotPassword(email) {
        if (log.infoEnabled) {
            log.info "User with email [${email}] requested a password reset"
        }
		def u = findUserByEmail(email)
		if (u) {
		    u.credentialsExpired = true
		    u.save(flush:true)
        
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
            
            return true
        } else {
            return false
        }
    }

    @Transactional
    void resetPassword(userId, String newPassword) {
        def user = findUserByIdentity(userId)
        if (user) {
            if (log.infoEnabled) {
                log.info "Resetting password for user [${userId}]"
            }
            user.password = encodePassword(user.userName, newPassword)
            user.save(flush:true) // Seems like a good plan, right?
        } else {
            if (log.infoEnabled) {
                log.info "Could not reset password for user [${userId}], user not found"
            }
            throw new IllegalArgumentException("Cannot reset password, user not found")
        }
    }
    
    void sendUserPasswordResetNotification(user) {
        emailConfirmationService.sendConfirmation(
            to:user.email, 
            subject:"Set your new password", 
            plugin:'fresh-security', 
            view:'/email-templates/password-reset-confirmation',
            id:user.userName,
            handler:CONFIRMATION_HANDLER_PASSWORDRESET)
    }
    
    void setCurrentUser(userName) {
        springSecurityService.reauthenticate userName
    }
    
    def getIdentityField() {
        pluginConfig.identity.mode == 'email' ? 'email' : 'userName'
    }

    def encodePassword(userInfo, String password) {
        String salt = saltSource instanceof NullSaltSource ? null : userInfo.userName
		springSecurityService.encodePassword(password, salt)
    }

    @Transactional
    def createNewUser(userInfo, request = null) {
        boolean confirmBypass = (Environment.current == Environment.DEVELOPMENT) && userInfo.confirmBypass
        boolean confirmEmail = pluginConfig.confirm.email.on.signup && !confirmBypass
        boolean lockedUntilConfirmEmail = pluginConfig.account.locked.until.email.confirm && !confirmBypass
         
        def identity = pluginConfig.identity.mode == 'email' ? userInfo.email : userInfo.userName
		String password = encodePassword(identity, userInfo.password)
		
		def user = new SecUser(
		        userName: identity,
				password: password, 
				email: userInfo.email,
				accountLocked: lockedUntilConfirmEmail, 
				enabled: true,
				roleList: pluginConfig.'default'.roles)
				
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
                onNewUserSignedUp(user)
                
    		    setCurrentUser(user.userName)
		    }
		}

		return user
    }

    void onNewUserSignedUp(user) {
        if (log.infoEnabled) {
            log.info "New user signed up: [${user.userName}]"
        }
        
        def className = pluginConfig.user.object.class.name
        if (className) {
            if (log.infoEnabled) {
                log.info "Creating new application user object for [${user.userName}] of type [${className}]"
            }
            def cls = grailsApplication.classLoader.loadClass(className)
            def obj = cls.newInstance()

            if (log.infoEnabled) {
                log.debug "Populating new application user object for [${user.userName}] of type [${className}]..."
            }
            // @todo fire event here

            obj.save(flush:true)
            user.userObjectClassName = className
            user.userObjectId = obj.ident().toString()
        }
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