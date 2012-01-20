package com.grailsrocks.webprofile.security

import grails.converters.JSON

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils

import org.springframework.security.authentication.AccountExpiredException
import org.springframework.security.authentication.CredentialsExpiredException
import org.springframework.security.authentication.DisabledException
import org.springframework.security.authentication.LockedException
import org.springframework.security.core.context.SecurityContextHolder as SCH
import org.springframework.security.web.WebAttributes
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

import com.grailsrocks.webprofile.security.forms.*

class FreshSecurityAuthController {

	def authenticationTrustResolver

    def freshSecurityService
	def springSecurityService

    def grailsApplication

	def index = {
		if (securityIdentity) {
		    goToPostLoginPage()
		}
		else {
			redirect action: 'login', params: params
		}
	}

    def logout = {
        redirect(uri:'/j_spring_security_logout')
    }
	
    /**
	 * Show the login page.
	 */
	def login = {

		def config = SpringSecurityUtils.securityConfig

		if (securityIdentity) {
		    goToPostLoginPage()
			return
		}

        def userName = session['SPRING_SECURITY_LAST_USERNAME']
        
		String view = 'login'
		String postUrl = "${request.contextPath}${config.apf.filterProcessesUrl}"
		render( view: view, model: [
		    postUrl: postUrl,
		    loginForm: new LoginFormCommand(identity:userName),
            rememberMeParameter: config.rememberMe.parameter])
	}

	/*
	 * The redirect action for Ajax requests. 
	def authAjax = {
		response.setHeader 'Location', SpringSecurityUtils.securityConfig.auth.ajaxLoginFormUrl
		response.sendError HttpServletResponse.SC_UNAUTHORIZED
	}
	 */

	/*
	 * Show denied page.
	def denied = {
		if (springSecurityService.isLoggedIn() &&
				authenticationTrustResolver.isRememberMe(SCH.context?.authentication)) {
			// have cookie but the page is guarded with IS_AUTHENTICATED_FULLY
			redirect action: full, params: params
		}
	}
	 */

	/* @todo we need this
	 * Login page for users with a remember-me cookie but accessing a IS_AUTHENTICATED_FULLY page.
	def full = {
		def config = SpringSecurityUtils.securityConfig
		render view: 'login', params: params,
			model: [hasCookie: authenticationTrustResolver.isRememberMe(SCH.context?.authentication),
			        postUrl: "${request.contextPath}${config.apf.filterProcessesUrl}"]
	}
	 */

	/*
	 * Callback after a failed login. Redirects to the auth page with a warning message.
	 */
	def loginFail = {
        // @todo make this not suck
        
		def username = session[UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY]
		String msg = ''
		def exception = session[WebAttributes.AUTHENTICATION_EXCEPTION]
		if (exception) {
		    msg = "error."+exception.class.simpleName
		}

		if (springSecurityService.isAjax(request)) {
			render([error: msg] as JSON)
		}
		else {
            displayFlashMessage text:FreshSecurityService.PLUGIN_SCOPE+msg, type:'error'
			redirect action: 'login', params: params
		}
	}

    def firstLogin = {
        displayMessage text:FreshSecurityService.PLUGIN_SCOPE+'first.login', type:'info'
    }
    
    def badRequest = {
    }
    
    def forgotPassword = { 
    }

    def doForgotPassword = { ForgotPasswordFormCommand form ->
        if (log.infoEnabled) {
            log.info "User submitted request for password reset with email [${form.email}]"
        }
        if (!form.hasErrors()) {
            def emailKnown = freshSecurityService.userForgotPassword(form.email)
            if (!emailKnown) {
                if (log.warnEnabled) {
                    log.warn "User forgot password but email [${form.email}] is not associated with any user account"
                }
                displayMessage text:FreshSecurityService.PLUGIN_SCOPE+'forgot.password.unknown.email'
                render(view:'forgotPassword', model:[form:form])
            } else {
                if (log.infoEnabled) {
                    log.info "User password reset confirmation mail sent to email [${form.email}]"
                }
                displayFlashMessage text:FreshSecurityService.PLUGIN_SCOPE+'password.reset.confirm.sent'
                goToDefaultPage()
            }
        } else {
            if (log.debugEnabled) {
                log.debug "User forgot password but there were errors in their form: ${form.dump()}"
            }
            render(view:'forgotPassword', model:[forgotForm:form])
        }
    }

    private void goToDefaultPage() {
        redirect(pluginConfig.post.login.url)
    }
    
    private void goToPostLoginPage() {
        redirect(pluginConfig.post.login.url)
    }
    
    def resetPassword = {
        if (!session[FreshSecurityService.SESSION_VAR_PASSWORD_RESET_MODE]) {
            displayFlashMessage text:FreshSecurityService.PLUGIN_SCOPE+'password.reset.not.allowed', type:'error'
            redirect(action:'badRequest')
        } 
    }
    
    def doResetPassword = { PasswordResetFormCommand form ->
        def userIdentity = session[FreshSecurityService.SESSION_VAR_PASSWORD_RESET_IDENTITY]
        if (log.infoEnabled) {
            log.info "Request to reset password for user [${userIdentity}]"
        }
        if (!session[FreshSecurityService.SESSION_VAR_PASSWORD_RESET_MODE]) {
            if (log.infoEnabled) {
                log.info "Request to reset password but user is not in reset mode"
            }
            displayFlashMessage text:FreshSecurityService.PLUGIN_SCOPE+'password.reset.not.allowed', type:'error'
            redirect(action:'badRequest')
        } else {
            if (!form.hasErrors()) {
                if (log.infoEnabled) {
                    log.info "Request to reset password for user [${userIdentity}] being processed"
                }
                freshSecurityService.resetPassword(userIdentity, form.newPassword)
                session[FreshSecurityService.SESSION_VAR_PASSWORD_RESET_MODE] = false
                displayFlashMessage text:FreshSecurityService.PLUGIN_SCOPE+'password.reset.complete'
                goToPostLoginPage()
            } else {
                if (log.infoEnabled) {
                    log.info "Request to reset password for user [${userIdentity}] had errors"
                }
                displayMessage text:FreshSecurityService.PLUGIN_SCOPE+'password.reset.invalid', type:'error'
                // Blank out the password values
                form.newPassword = ''
                form.confirmPassword = ''
                render(view:'resetPassword', model:[form:form])
            }
        }
    }
    
	/*
	 * The Ajax success redirect url.
	def ajaxSuccess = {
		render([success: true, username: springSecurityService.authentication.name] as JSON)
	}
	 */

	/*
	 * The Ajax denied redirect url.
	def ajaxDenied = {
		render([error: 'access denied'] as JSON)
	}
	 */

	/**
     * Show the default dedicated signup screen
     */
    def signup = {
        if (!pluginConfig.signup.allowed) {
            response.sendError(400, "No signups allowed")
        }
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
        if (!pluginConfig.signup.allowed) {
            response.sendError(400, "No signups allowed")
            return
        }

        if (log.debugEnabled) {
            log.debug "User signing up: ${form.identity}"
        }
        
        if (form.hasErrors()) {
            if (log.debugEnabled) {
                log.debug "User signing up, form has errors: ${form.errors}"
            }
            render(view:'signup', model:[form:form])
            return
        }
        
        def user = freshSecurityService.createNewUser(form, request)

		// @todo Look at providing hooks for other form variables not included in our SignupFormCommand
		
		if (user.hasErrors()) {
            if (log.debugEnabled) {
                log.debug "User signing up, failed to save user: ${user.identity} - errors: ${user.errors}"
            }
            render(view:'signup', model:[form:form])
		} else {
            if (log.debugEnabled) {
                log.debug "User signed up, redirecting to post signup url: ${user.identity}"
            }
            displayFlashMessage text:FreshSecurityService.PLUGIN_SCOPE+(user.accountLocked ? 'signup.confirm.required' : 'signup.complete'), 
                type:'info'
            goToPostLoginPage()
		}
    }
}