package com.grailsrocks.webprofile.security

class FreshSecurityTagLib {
    static namespace = "fresh"
    
    def grailsApplication
    
    def loginForm = { attrs, body ->
        out << g.render(template:'/_fresh_security/loginForm')
    }

    def signupForm = { attrs, body ->
        out << g.render(template:'/_fresh_security/signupForm')
    }
    
    def forgotPasswordForm = { attrs, body ->
        out << g.render(template:'/_fresh_security/forgotPasswordForm')
    }
    
    def resetPasswordForm = { attrs, body ->
        out << g.render(template:'/_fresh_security/resetPasswordForm')
    }
    
    def logoutLink = { attrs ->
		attrs.controller = "auth"
		attrs.action = "logout"
		out << ui.link(attrs)
    }

    def resetPasswordLink = { attrs->
		attrs.controller = "auth"
		attrs.action = "forgotPassword"
		out << ui.link(attrs)
    }

    def ifSignupAllowed = { attrs, body ->
        if (grailsApplication.config.plugin.freshSecurity.signup.allowed) {
            out << body()
        }
    }
    
    def ifUiMessage = { attrs, body ->
        if (flash[FreshSecurityService.FLASH_VAR_UI_MESSAGE]) {
            out << body()
        }
    }

    def ifIdentityMode = { attrs, body ->
        if (grailsApplication.config.plugin.freshSecurity.identity.mode == attrs.value) {
            out << body()
        }
    }

    def uiMessage = { attrs ->
        def msg = flash[FreshSecurityService.FLASH_VAR_UI_MESSAGE]
        if (msg) {
            out << g.message(code:msg, encodeAs:'HTML')
        }
    }
}
