package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators

@Validateable
class LoginFormCommand {
    String identity
    String password
    Boolean rememberMe

    static constraints = {
        identity(maxSize: 40, blank: false)
    	password(blank: false, minSize: 8, maxSize: 64, validator: CustomValidators.passwordStrength)
    	rememberMe(blank: true, nullable: true)
    }
}