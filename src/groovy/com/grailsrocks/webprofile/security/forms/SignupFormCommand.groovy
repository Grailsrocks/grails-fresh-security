package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators

@Validateable
class SignupFormCommand {
    String userName
    String email
    String password
    String confirmPassword
    Boolean rememberMe
    Boolean confirmBypass
    
    def freshSecurityService

    static constraints = {
        // @todo externalize these constraints
        userName(maxSize: 40, blank: false, validator: { value, command ->
			if (value) {
			    if (command.freshSecurityService.userExists(value)) {
			        return 'username.taken'
			    } 
			} 
			return null
		})

        email(maxSize: 80, blank: false, email: true)
    	password(blank: false, minSize: 8, maxSize: 64, validator: CustomValidators.passwordStrengthAndUserName)
        confirmPassword(validator: CustomValidators.confirmPassword)
    	rememberMe(blank: true, nullable: true)
    	confirmBypass(blank: true, nullable: true)
    }
}