import grails.plugins.springsecurity.SecurityConfigType

import com.grailsrocks.webprofile.security.*
import groovy.util.ConfigObject

class SpringFreshSecurityGrailsPlugin {
    // the plugin version
    def version = "1.0.BUILD-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
        'pluginPlatformCore':'0.1 > *',
        'springSecurityCore':'1.1 > *'
    ]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/views/test.gsp"
    ]

    def loadBefore = ['springSecurityCore'] // We must apply our config before spring-sec loads its
    
    // TODO Fill in these fields
    def title = "Fresh Spring Security Plugin" // Headline display name of the plugin
    def author = "Marc Palmer"
    def authorEmail = "marc@grailsrocks.com"
    def description = '''\
Security that "just works", backed by Spring Security
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/fresh-spring-security"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.grails-plugins.codehaus.org/browse/grails-plugins/" ]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {        
        userDetailsService(com.grailsrocks.webprofile.security.FreshSecurityUserDetailsService) {
            grailsApplication = ref('grailsApplication')
        }
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithConfigOptions = { 
        'signup.allowed'(defaultValue:true)
        'post.login.url'(defaultValue:'/')
        'post.login.always_default'(defaultValue:true)
    }
    
    def doWithConfig = { config ->

        application {
            // Get our config values and use them to apply to Spring security's config by
            // modifying global config
            // This needs to be set as the default, that user can override using our config
            grails.plugins.springsecurity.interceptUrlMap = [
               '/admin/**':     ['ROLE_ADMIN'],
               '/static/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/js/**':        ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/css/**':       ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/images/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/auth/**':      ['IS_AUTHENTICATED_ANONYMOUSLY'],
            ]
        
            grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
            grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.grailsrocks.webprofile.security.SecUser'
            grails.plugins.springsecurity.userLookup.usernamePropertyName = 'userName'
            grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/auth/loginFail?error='
            grails.plugins.springsecurity.adh.errorPage = '/auth/denied'

            grails.plugins.springsecurity.successHandler.defaultTargetUrl = 
                config.plugin.springFreshSecurity.post.login.url
            grails.plugins.springsecurity.successHandler.alwaysUseDefault = 
                config.plugin.springFreshSecurity.post.login.always_default
        }

    }
    
    def doWithApplicationContext = { applicationContext ->
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
