package com.grailsrocks.webprofile.security

import grails.util.Environment
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.springframework.transaction.annotation.*
import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.springframework.beans.factory.InitializingBean
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UsernameNotFoundException
import java.util.regex.Pattern

class FreshSecurityService implements InitializingBean {
    static transactional = false
    
    static final String PLUGIN_EVENT_NAMESPACE = 'freshSecurity'
    
    static final String SESSION_VAR_PASSWORD_RESET_MODE = 'password.reset.mode'
    static final String SESSION_VAR_PASSWORD_RESET_IDENTITY = 'password.reset.identity'
    
    static final String PASSWORD_RESET_MODE_GENERATE = "generate"
    static final String PASSWORD_RESET_MODE_SETNEW = "setnew"
    
    def saltSource
    def grailsApplication
    def springSecurityService
    def emailConfirmationService
    def grailsUiExtensions
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
    @grails.events.Listener(namespace=FreshSecurityService.PLUGIN_EVENT_NAMESPACE, topic='password.reset.confirmed')
    def passwordResetConfirmed(args) {
        if (log.debugEnabled) {
            log.debug "User password reset confirmed: ${args}"
        }
        if (findUserByIdentity(args.id)) { 
            def session = grailsUiExtensions.getPluginSession('platformUi')
    	    
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
    @grails.events.Listener(namespace=FreshSecurityService.PLUGIN_EVENT_NAMESPACE, topic='new.user.confirmed')
    def newUserConfirmed(args) {
        def user = findUserByIdentity(args.id)
        if (user) { 
            if (log.debugEnabled) {
                log.debug "New user account was confirmed [${user}], unlocking account and completing sign up callbacks"
            }
            user.accountLocked = false
            
            onNewUserSignedUp(user, null)

            grailsUiExtensions.displayFlashMessage(text:'signup.confirm.completed', 'freshSecurity')
            def redirectArgs = event(topic:'newUserConfirmedPage', 
                namespace:FreshSecurityService.PLUGIN_EVENT_NAMESPACE, fork:false, data:user).value
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
                log.info "Resetting password for user [${userId}] and unlocking their account"
            }
            user.password = encodePassword(user.identity, newPassword)
            user.accountLocked = false
            user.credentialsExpired = false
            user.passwordExpired = false
            user.save(flush:true) // Seems like a good plan, right?
            
            event(topic:'passwordWasReset', 
                namespace:FreshSecurityService.PLUGIN_EVENT_NAMESPACE, 
                fork:false,
                data:user)
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
            eventNamespace:PLUGIN_EVENT_NAMESPACE)
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
    def createNewUser(NewUserAccount userInfo, request = null) {
        return createNewUserWithObject(userInfo, null, request)
    }
    
    @Transactional
    def createNewUserWithObject(NewUserAccount userInfo, userObject, request = null) {
        def identity = userInfo.identity
        if (log.debugEnabled) {
            log.debug "Creating new user ${identity}"
        }
        
        boolean allowsBypass = pluginConfig.allow.confirm.bypass instanceof Pattern ? 
            userInfo.identity ==~ pluginConfig.allow.confirm.bypass : pluginConfig.allow.confirm.bypass.toBoolean()
            
        boolean confirmBypass = allowsBypass && userInfo.confirmBypass
        boolean confirmEmail = pluginConfig.confirm.email.on.signup && !confirmBypass

        if (log.debugEnabled) {
            log.debug "Accounts are locked until email confirmation? ${pluginConfig.account.locked.until.email.confirm}"
        }

        boolean lockedUntilConfirmEmail = pluginConfig.account.locked.until.email.confirm && !confirmBypass
         
		String password = encodePassword(identity, userInfo.password)
		
        if (log.debugEnabled) {
            log.debug "Instantiating new user ${identity}, locked until email confirm?: ${lockedUntilConfirmEmail}"
        }

		def user = new SecUser(
		        identity: identity,
				password: password, 
				accountLocked: lockedUntilConfirmEmail, 
				enabled: true,
				roleList: pluginConfig.'default'.roles)

        // Copy email if we have it in the form
        if (userInfo.metaClass.hasProperty(userInfo, 'email')) {
            user.email = userInfo.email
        }
        if (pluginConfig.identity.mode == 'email') {
            user.email = identity
        }
				
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
    	        def session = grailsUiExtensions.getPluginSession('platformUi')
    	        session['new.sign.up'] = true
    	        session['email.confirm.pending'] = confirmEmail
	        }

            // Force the new user to be logged in if email confirmation is not required
            if (!confirmEmail) {
                if (log.debugEnabled) {
                    log.debug "User signing up, logging them in automatically: ${user.identity}"
                }
                onNewUserSignedUp(user, userObject, request)
                
    		    setCurrentUser(user.identity)
		    }
		}

		return user
    }

    @Transactional
    void onNewUserSignedUp(SecUser user, userObject, request = null) {
        if (log.infoEnabled) {
            log.info "New user signed up with identity [${user.identity}] from host [${request?.remoteAddr}]"
        }
        
        boolean createdUserObject
        // If the user was not signed up with a userObject provided, create one now
        if (!userObject) {
            if (!user.userObjectId) {
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
            } else {
                userObject = user.userObject
            }
        }
        
        // Let app populate user object or other side effects, or define user object
        def eventObj = new NewUserEvent(user:user, userObject:userObject, request:request)
        event(topic:'newUserCreated', namespace:FreshSecurityService.PLUGIN_EVENT_NAMESPACE, 
            fork:false, data:eventObj)
        
        // Save the app's user object and bind it to the FreshSecurity user
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
            log.info "Removing user account [${identity}]"
        }
        
        def user
        try {
            user = userDetailsService.loadUserByUsername(identity) 

        } catch (UsernameNotFoundException unfe) {
            log.warn "Cannot remove user account [${identity}] because it does not exist"
            return false
        }

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
        emailConfirmationService.sendConfirmation(to: user.email, subject:"Confirm your new account", 
            view:'/email-templates/signup-confirmation',
            id:user.identity,
            event:'new.user',
            eventNamespace:PLUGIN_EVENT_NAMESPACE)
    }
    
    Class getUserClass() {
		grailsApplication.getDomainClass(
			SpringSecurityUtils.securityConfig.userLookup.userDomainClassName).clazz
    }
}