package com.grailsrocks.webprofile.security

import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.plugins.springsecurity.GrailsUserDetailsService

import org.springframework.security.core.authority.GrantedAuthorityImpl
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.beans.factory.InitializingBean

import org.apache.commons.logging.LogFactory

class FreshSecurityUserDetailsService implements GrailsUserDetailsService, InitializingBean {

    def log = LogFactory.getLog("grails.app.service."+FreshSecurityUserDetailsService.name)

    /**
    * Some Spring Security classes (e.g. RoleHierarchyVoter) expect at least one role, so
    * we give a user with no granted roles this one which gets past that restriction but
    * doesn't grant anything.
    */
    static final List NO_ROLES = [new GrantedAuthorityImpl(SpringSecurityUtils.NO_ROLE)]

    def grailsApplication

    Class domainClass

    void afterPropertiesSet() {
        def conf = grailsApplication.config
        def clsname = conf.grails.plugins.springsecurity.userLookup.userDomainClassName
        println "User classname is: ${clsname}"
        domainClass = grailsApplication.getDomainClass(clsname).clazz
    }

    UserDetails loadUserByUsername(String username, boolean loadRoles)
            throws UsernameNotFoundException {
        return loadUserByUsername(username)
    }

    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        domainClass.withTransaction { status ->

            def user = domainClass.findByUserName(username)
            if (!user) throw new UsernameNotFoundException('User not found', username)

            def authorities = user.authorities.collect {new GrantedAuthorityImpl(it.authority)}

            return new FreshSecurityUserDetails(user, authorities)
        }
    }
}
