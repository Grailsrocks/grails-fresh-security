package com.grailsrocks.webprofile.security

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators

@Validateable
class NewUserAccount {
    String identity
    String password
    Boolean confirmBypass
    
    static constraints = { 
    	password(blank: false, minSize: 8, maxSize: 64, validator: 
            CustomValidators.passwordStrengthAndUserName)
    	confirmBypass(blank: true, nullable: true)
    }
}