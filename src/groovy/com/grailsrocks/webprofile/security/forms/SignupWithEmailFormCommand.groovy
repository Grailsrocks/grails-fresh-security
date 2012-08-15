package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

import com.grailsrocks.webprofile.security.CustomValidators
import com.grailsrocks.webprofile.security.NewUserAccount

@Validateable
class SignupWithEmailFormCommand extends NewUserAccount {
    String confirmPassword
    Boolean rememberMe
    
    def freshSecurityService

    static constraints = {
        // @todo externalize these constraints
        identity(maxSize: 80, nullable: false, blank: false, email:true, validator: { value, command ->
			if (value) {
			    if (command.freshSecurityService.userExists(value)) {
			        return 'username.taken'
			    } 
			} 
			return null
		})
 
        confirmPassword(validator: CustomValidators.confirmPassword)
        rememberMe(blank: true, nullable: true)
    }
}