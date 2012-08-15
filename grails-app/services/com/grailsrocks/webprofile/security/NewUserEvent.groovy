package com.grailsrocks.webprofile.security

/**
 * Wrapper for values passed to newUserCreated listeners
 */
class NewUserEvent {
    SecUser user
    def userObject
    def request
}