package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators

@Validateable
class PasswordResetFormCommand {
    String newPassword
    String confirmPassword
    
    static constraints = {
    	newPassword(blank: false, minSize: 8, maxSize: 64, validator: CustomValidators.passwordStrength)
        confirmPassword(validator: CustomValidators.confirmNewPassword)
    }
}