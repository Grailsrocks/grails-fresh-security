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
    
    def logoutLink = { attrs, body ->
		attrs.controller = "auth"
		attrs.action = "logout"
		out << g.link(attrs, body)
    }

    def ifSignupAllowed = { attrs, body ->
        if (grailsApplication.config.plugin.freshSecurity.signup.allowed) {
            out << body()
        }
    }
    
    def ifUiMessage = { attrs, body ->
        if (flash['plugin.fresh.security.message']) {
            out << body()
        }
    }

    def ifIdentityMode = { attrs, body ->
        if (grailsApplication.config.plugin.freshSecurity.identity.mode == attrs.value) {
            out << body()
        }
    }

    def uiMessage = { attrs ->
        def msg = flash['plugin.fresh.security.message']
        if (msg) {
            out << g.message(code:msg, encodeAs:'HTML')
        }
    }
}
