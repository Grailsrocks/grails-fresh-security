package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators

@Validateable
class LoginFormCommand {
    String userName
    String password
    Boolean rememberMe
    def freshSecurityService

    static constraints = {
        userName(maxSize: 40, blank: false)
    	password(blank: false, minSize: 8, maxSize: 64, validator: CustomValidators.password)
    	rememberMe(blank: true, nullable: true)
    }
}