package com.grailsrocks.webprofile.security

/**
 * The domain class encapsulating the user
 */
class SecUser {
    static transients = ['userObject']
    
    String identity

    String email
    
    String password
    String roles
    
	boolean enabled
	boolean accountExpired
	boolean credentialsExpired
	boolean accountLocked
	boolean passwordExpired
    
    Date dateCreated
    Date lastUpdated
    
    String userObjectClassName
    String userObjectId
    
    static constraints = {
        identity(unique:true, nullable: false, blank: false, maxSize:80)
        email(email:true, nullable: true, blank: false, maxSize:80)
        password(nullable: false, blank: false, maxSize:80)
        roles(nullable: true)
        userObjectClassName(nullable: true)
        userObjectId(nullable: true)
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
	
	void setUserObject(obj) {
	    if (obj) {
	        userObjectClassName = obj.getClass().name
	        userObjectId = obj.ident()
        } else {
	        userObjectClassName = null
	        userObjectId = null
        }
	}
}
