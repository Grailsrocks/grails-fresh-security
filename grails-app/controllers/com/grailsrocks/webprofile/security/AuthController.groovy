package com.grailsrocks.webprofile.security

import grails.converters.JSON

import org.codehaus.groovy.grails.plugins.springsecurity.NullSaltSource
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import com.grailsrocks.webprofile.security.forms.*

class AuthController {

    def saltSource
    
	def authenticationTrustResolver

	def springSecurityService

    def grailsApplication

    def emailConfirmationService
    
	def index = {
		if (springSecurityService.isLoggedIn()) {
			redirect uri: grailsApplication.config.plugin.springFreshSecurity.post.login.url
		}
		else {
			redirect action: 'login', params: params
		}
	}

	/**
	 * Show the login page.
	 */
	def login = {

		def config = SpringSecurityUtils.securityConfig

		if (springSecurityService.isLoggedIn()) {
			redirect uri: config.successHandler.defaultTargetUrl
			return
		}

		String view = 'login'
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
		render view: view, model: [postUrl: postUrl,
		                           rememberMeParameter: config.rememberMe.parameter]
	}

	/**
	 * The redirect action for Ajax requests. 
	 */
	def authAjax = {
		response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
		response.sendError HttpServletResponse.SC_UNAUTHORIZED
	}

	/**
	 * Show denied page.
	 */
	def denied = {
		if (springSecurityService.isLoggedIn() &&
				authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
			// have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
			redirect action: full, params: params
		}
	}

	/**
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	 */
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render view: 'login', params: params,
			model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
			        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}

	/**
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def loginFail = {

        // @todo make this not suck
        
		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
		if (exception) {
			if (exception instanceof AccountExpiredException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.expired
			}
			else if (exception instanceof CredentialsExpiredException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.passwordExpired
			}
			else if (exception instanceof DisabledException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.disabled
			}
			else if (exception instanceof LockedException) {
				msg = SpringSecurityUtils.securityConfig.errors.login.locked
			}
			else {
				msg = SpringSecurityUtils.securityConfig.errors.login.fail
			}
		}

		if (springSecurityService.isAjax(request)) {
			render([error: msg] as JSON)
		}
		else {
			flash.message = msg
			redirect action: 'login', params: params
		}
	}

	/**
	 * The Ajax success redirect url.
	 */
	def ajaxSuccess = {
		render([success: true, username: springSecurityService.authentication.name] as JSON)
	}

	/**
	 * The Ajax denied redirect url.
	 */
	def ajaxDenied = {
		render([error: 'access denied'] as JSON)
	}
    
    def signup = {
        [form: new SignupFormCommand()]
    }

    /**
     * Perform signup. We need to support at least four different kinds of sign up:
     *
     * 1. Username + password, no email
     * 2. Username + email + password, email confirmed or not
     * 3. Email + password, email confirmed, username = email
     * 4. Twitter/Facebook OAuth signup/auth
     * 5. Open ID
     */
    def doSignup = { SignupFormCommand form ->
        // @todo WTF move this to a service

        if (log.debugEnabled) {
            log.debug "User signing up: ${form.userName}"
        }
        
        if (form.hasErrors()) {
            if (log.debugEnabled) {
                log.debug "User signing up, form has errors: ${form.userName}"
            }
            render(view:'signup', model:[form:form])
            return
        }
        
        boolean confirmEmail = grailsApplication.config.plugin.springFreshSecurity.confirm.email.on.signup
        boolean lockedUntilConfirmEmail = grailsApplication.config.plugin.springFreshSecurity.confirm.account.locked.until.email.confirm
        
        String salt = saltSource instanceof NullSaltSource ? null : form.userName
		String password = springSecurityService.encodePassword(form.password, salt)
		def user = new SecUser(
		        userName: form.userName,
				password: password, 
				email: form.email,
				accountLocked: confirmEmail ? lockedUntilConfirmEmail : false, 
				enabled: true,
				roleList: grailsApplication.config.plugin.springFreshSecurity.'default'.roles)
				
		// @todo Look at providing hooks for other form variables not included in our SignupFormCommand
		
		if (!user.save()) {
            if (log.debugEnabled) {
                log.debug "User signing up, failed to save user: ${user.userName} - errors: ${user.errors}"
            }
            render(view:'signup', model:[form:form])
		} else {
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
	        session['spring.fresh.security.new.sign.up'] = true
	        session['spring.fresh.security.email.confirm.pending'] = confirmEmail

            println "WTF: ${grailsApplication.mainContext.userDetailsService}"
            
            // Force the new user to be logged in if email confirmation is not required
            if (!confirmEmail) {
                if (log.debugEnabled) {
                    log.debug "User signing up, logging them in automatically: ${user.userName}"
                }
    		    springSecurityService.reauthenticate user.userName
		    }

            if (log.debugEnabled) {
                log.debug "User signed up, redirecting to post signup url: ${user.userName}"
            }
	        redirect grailsApplication.config.plugin.springFreshSecurity.post.signup.url
		}
    }
}