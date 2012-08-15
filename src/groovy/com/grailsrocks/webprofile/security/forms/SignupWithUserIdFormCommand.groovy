package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators
import com.grailsrocks.webprofile.security.NewUserAccount

@Validateable
class SignupWithUserIdFormCommand extends NewUserAccount {
    String email
    String confirmPassword
    Boolean rememberMe
    
    def freshSecurityService

    static constraints = {
        // @todo externalize these constraints
        identity(maxSize: 40, blank: false, validator: { value, command ->
			if (value) {
			    if (command.freshSecurityService.userExists(value)) {
			        return 'username.taken'
			    } 
			} 
			return null
		})
 
        email(maxSize: 80, blank: false, email: true)
        confirmPassword(validator: CustomValidators.confirmPassword)
    	rememberMe(blank: true, nullable: true)
    }
}