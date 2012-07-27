package com.grailsrocks.webprofile.security

class FreshSecurityTagLib {
    static namespace = "fresh"
    
    static returnObjectForTags = ['userObject']

    def grailsApplication
    def grailsSecurity
    
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
        out << g.render(/*plugin:'freshSecurity', */ template:'/_fresh_security/resetPasswordForm')
    }

    def userObject = { attrs ->
        def uinf = grailsSecurity.userInfo
        if (uinf) {
            return uinf.userObject
        } else {
            return Collections.EMPTY_MAP
        }
    }
    
    def logoutLink = { attrs ->
		attrs.controller = "auth"
		attrs.action = "logout"
		out << p.smartLink(attrs)
    }

    def resetPasswordLink = { attrs->
		attrs.controller = "auth"
		attrs.action = "forgotPassword"
		out << p.smartLink(attrs)
    }

    def ifRememberMeAllowed = { attrs, body ->
        if (pluginConfig.remember.me.allowed) {
            out << body()
        }
    }

    def ifSignupAllowed = { attrs, body ->
        if (pluginConfig.signup.allowed) {
            out << body()
        }
    }
    
    def ifNotConfirmSignup = { attrs, body ->
        if (!pluginConfig.confirm.email.on.signup) {
            out << body()
        }
    }
    
    def ifIdentityMode = { attrs, body ->
        if (pluginConfig.identity.mode == attrs.value) {
            out << body()
        }
    }
}
