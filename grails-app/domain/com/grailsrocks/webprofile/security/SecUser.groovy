package com.grailsrocks.webprofile.security

class User {
    String userName
    
    String password
    
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
    
    Date dateCreated
    Date lastUpdated
    
    static constraints = {
        userName(unique:true)
    }

    void addRole(String s) {
        // call event
    }
    
	Set<String> getAuthorities() {
        // call event 
        []
	}
}
