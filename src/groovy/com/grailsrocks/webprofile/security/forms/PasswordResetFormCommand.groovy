package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators

@Validateable
class PasswordResetFormCommand {
    String password
    String confirmPassword
    
    static constraints = {
    	password(blank: false, minSize: 8, maxSize: 64, validator: CustomValidators.password)
        confirmPassword(validator: CustomValidators.confirmPassword)
    }
}