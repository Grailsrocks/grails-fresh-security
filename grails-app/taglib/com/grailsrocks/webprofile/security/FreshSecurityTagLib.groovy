package com.grailsrocks.webprofile.security

class FreshSecurityTagLib {
    static namespace = "fresh"
    
    def loginForm = { attrs, body ->
        out << g.render(template:'/_fresh_security/loginForm')
    }

    def signupForm = { attrs, body ->
        out << g.render(template:'/_fresh_security/signupForm')
    }
    
    def logoutLink = { attrs, body ->
    }

    def ifSignupAllowed = { attrs, body ->
        if (grailsApplication.config.webprofile.spring.fresh.security.signupAllowed) {
            out << body()
        }
    }
}
