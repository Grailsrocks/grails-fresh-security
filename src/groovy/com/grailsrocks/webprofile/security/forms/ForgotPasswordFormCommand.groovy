package com.grailsrocks.webprofile.security.forms

import org.codehaus.groovy.grails.validation.Validateable

@Validateable
class ForgotPasswordFormCommand {
    String email

    static constraints = {
        email(maxSize: 80, blank: false, nullable: false, email:true)
    }
}