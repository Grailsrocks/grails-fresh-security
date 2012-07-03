package com.grailsrocks.webprofile.security

import grails.util.Environment
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.transaction.annotation.*
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import java.util.regex.Pattern

class FreshSecurityService implements InitializingBean {
    static transactional = false
    
    static final String PLUGIN_SCOPE = 'plugin.freshSecurity'
    
    static final String SESSION_VAR_PASSWORD_RESET_MODE = PLUGIN_SCOPE+'.password.reset.mode'
    static final String SESSION_VAR_PASSWORD_RESET_IDENTITY = PLUGIN_SCOPE+'.password.reset.identity'
    
    static final String PASSWORD_RESET_MODE_GENERATE = "generate"
    static final String PASSWORD_RESET_MODE_SETNEW = "setnew"
    
    def saltSource
    def grailsApplication
    def springSecurityService
    def emailConfirmationService
    def grailsUiHelper
    def userDetailsService
    
    @Transactional
    boolean userExists(identity) {
		findUserByIdentity(identity)
    }

    @Transactional
    def findUserByIdentity(identity) {
        userClass.findByIdentity(identity)
    }

    @Transactional
    def findUserByEmail(email) {
        userClass.findByEmail(email)
    }

    void afterPropertiesSet() {
    }
    
    /**
     * Check user exists, then set session var to indicate that user is in "reset mode" and
     * redirect to password set screen
     */
    @Transactional
    @grails.events.Listener(namespace=FreshSecurityService.PLUGIN_SCOPE, topic='password.reset.confirmed')
    def passwordResetConfirmed(args) {
        if (log.debugEnabled) {
            log.debug "User password reset confirmed: ${args}"
        }
        if (findUserByIdentity(args.id)) { 
            def session = RequestContextHolder.requestAttributes.session
    	    
            session[SESSION_VAR_PASSWORD_RESET_MODE] = true
            session[SESSION_VAR_PASSWORD_RESET_IDENTITY] = args.id

            if (log.infoEnabled) {
                log.info "User password reset confirmed for [${args}], redirecting to reset password screen"
            }
            return [controller:'freshSecurityAuth', action:'resetPassword']
        } else {
            if (log.errorEnabled) {
                log.error "User password reset confirmed for [${args}] but user could not be found with the identity [${args.id}]"
            }
            return [controller:'freshSecurityAuth', action:'badRequest', params:[reason:'password.reset.no.such.user']]
        }
    }

    /** 
     * Mark their account as enabled, redirect them to login screen
     */
    @Transactional
    @grails.events.Listener(namespace=FreshSecurityService.PLUGIN_SCOPE, topic='new.user.confirmed')
    def newUserConfirmed(args) {
        def user = findUserByIdentity(args.id)
        if (user) { 
            user.accountLocked = false
            
            onNewUserSignedUp(user, null)

            grailsUiHelper.displayFlashMessage text:PLUGIN_SCOPE+'.signup.confirm.completed'
            def redirectArgs = event('newUserConfirmedPage', user).value
            if (log.debugEnabled) {
                log.debug "Redirecting new user, app event returned redirect args: ${redirectArgs}"
            }
            return redirectArgs ?: pluginConfig.post.login.url
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
            user.password = encodePassword(user.identity, newPassword)
            user.save(flush:true) // Seems like a good plan, right?
            
            event('passwordWasReset', user)
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
            view:'/email-templates/password-reset-confirmation',
            id:user.identity,
            event:'password.reset',
            eventNamespace:PLUGIN_SCOPE)
    }
    
    void logout() {
        SecurityContextHolder.context.authentication = null
    }
    
    void setCurrentUser(identity) {
        if (identity != null) {
            springSecurityService.reauthenticate identity
        } else {
            logout()
        }
    }
    
    def encodePassword(userInfo, String password) {
        String salt = saltSource instanceof NullSaltSource ? null : userInfo.identity
		springSecurityService.encodePassword(password, salt)
    }

    @Transactional
    def createNewUser(userInfo, request = null) {
        return createNewUserWithObject(userInfo, null, request)
    }
    
    @Transactional
    def createNewUserWithObject(userInfo, userObject, request = null) {
        def identity = pluginConfig.identity.mode == 'email' ? userInfo.email : userInfo.identity
        if (log.debugEnabled) {
            log.debug "Creating new user ${identity}..."
        }
        
        boolean allowsBypass = pluginConfig.allow.confirm.bypass instanceof Pattern ? 
            userInfo.email ==~ pluginConfig.allow.confirm.bypass : pluginConfig.allow.confirm.bypass.toBoolean()
            
        boolean confirmBypass = allowsBypass && userInfo.confirmBypass
        boolean confirmEmail = pluginConfig.confirm.email.on.signup && !confirmBypass
        boolean lockedUntilConfirmEmail = pluginConfig.account.locked.until.email.confirm && !confirmBypass
         
		String password = encodePassword(identity, userInfo.password)
		
		def user = new SecUser(
		        identity: identity,
				password: password, 
				email: userInfo.email,
				accountLocked: lockedUntilConfirmEmail, 
				enabled: true,
				roleList: pluginConfig.'default'.roles)
				
		if (user.save()) {
            if (log.debugEnabled) {
                log.debug "User signing up, saved user: ${user.identity}"
            }
		    if (confirmEmail) {
                if (log.debugEnabled) {
                    log.debug "User signing up, sending email confirmation: ${user.identity}"
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
                    log.debug "User signing up, logging them in automatically: ${user.identity}"
                }
                onNewUserSignedUp(user, userObject)
                
    		    setCurrentUser(user.identity)
		    }
		}

		return user
    }

    @Transactional
    void onNewUserSignedUp(user, userObject) {
        if (log.infoEnabled) {
            log.info "New user signed up: [${user.identity}]"
        }
        
        boolean createdUserObject
        if (!userObject && !user.userObjectId) {
            def className = pluginConfig.user.object.class.name
            if (className) {
                if (log.infoEnabled) {
                    log.info "Creating new application user object for [${user.identity}] of type [${className}]"
                }
                def cls = grailsApplication.classLoader.loadClass(className)
                userObject = cls.newInstance()
                createdUserObject = true
            }
        
    
            if (log.infoEnabled) {
                log.debug "Populating new application user object for [${user.identity}] of type [${className}]..."
            }
        }
        
        // Let app unit user object or other side effects, or define user object
        def eventObj = new NewUserEvent(user:user, userObject:userObject)
        event('newUserCreated', eventObj)
        
        if (eventObj.userObject) {
            if (eventObj.userObject.save(flush:true)) {
                user.userObjectClassName = eventObj.userObject.getClass().name
                user.userObjectId = eventObj.userObject.ident().toString()
            } else {
                log.warn "Could not save Application's user object [${eventObj.userObject}] - errors: [${eventObj.userObject.errors}]"
            }
        }
    }
    
    @Transactional
    boolean deleteUser(identity) {
        if (log.infoEnabled) {
            log.info "Removing user account: [${identity}]"
        }
        
        def user = userDetailsService.loadUserByUsername(identity) 
        if (user) {
            // Delete the app's object
            def userObj = user.userObject
            if (userObj) {
                userObj.delete(flush:true)
            }
            
            // Delete our security user object
            def dbUser = findUserByIdentity(identity)
            if (dbUser) {
                dbUser.delete(flush:true)
            }
        
            return true
        } else {
            if (log.infoEnabled) {
                log.info "Could not remove user account [${identity}], no such account"
            }
            return false
        }
    }

    void sendNewAccountConfirmationEmail(user) {
        if (log.infoEnabled) {
            log.info "Sending another account signup confirmation email to [${user.email}]"
        }
        sendSignupConfirmation(user)
    }

    void sendSignupConfirmation(user) {
        if (log.infoEnabled) {
            log.info "Sending account signup confirmation email to [${user.email}]"
        }
        emailConfirmationService.sendConfirmation(to:user.email, subject:"Confirm your new account", 
            view:'/email-templates/signup-confirmation',
            id:user.identity,
            event:'new.user',
            eventNamespace:PLUGIN_SCOPE)
    }
    
    Class getUserClass() {
		grailsApplication.getDomainClass(
			SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
    }
}