package com.grailsrocks.webprofile.security

class SecUser {
    String userName
    
    String password
    String roles
    
	boolean enabled
	boolean accountExpired
	boolean accountLocked
	boolean passwordExpired
    
    Date dateCreated
    Date lastUpdated
    
    static constraints = {
        userName(unique:true)
        roles(nullable: true)
    }

    void addRole(String s) {
        // call event
        roles = roles + '|' + s
    }
    
    void setRoleList(List<String> s) {
        // call event
        roles = s.join('|')
    }
    
	Set<String> getAuthorities() {
        // call event ?
        roles.tokenize('|') as Set
	}
}
