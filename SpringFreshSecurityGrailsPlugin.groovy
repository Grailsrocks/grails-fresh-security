import grails.plugins.springsecurity.SecurityConfigType

import com.grailsrocks.webprofile.security.*
import groovy.util.ConfigObject
import org.codehaus.groovy.grails.plugins.PluginManagerHolder


class SpringFreshSecurityGrailsPlugin {
    // the plugin version
    def version = "1.0.BUILD-SNAPSHOT"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"
    // the other plugins this plugin depends on
    def dependsOn = [
        'pluginPlatform':'0.1 > *',
        'springSecurityCore':'1.1 > *'
    ]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp",
        "grails-app/views/test.gsp"
    ]

    def loadAfter = ['springSecurityCore'] // We must apply our beans AFTER spring-sec declares its own
    
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
        grailsSecurityBridge(com.grailsrocks.webprofile.security.FreshSecurityProvider) {
            springSecurityService = ref('springSecurityService')
            grailsApplication = ref('grailsApplication')
        }
    }

    def doWithDynamicMethods = { ctx ->
        
        // @todo upgrade email conf plugin to use events mechanism, if app uses email conf too, it will break us unless
        // they delegate to us as well if uid is "fresh.security.signup"
        if (ctx.emailConfirmationService) {
            ctx.emailConfirmationService.onConfirmation = { email, uid ->
                log.info("User with id $uid has confirmed their email address $email")
                // now do something…
                // Then return a map which will redirect the user to this destination
                return ctx.grailsApplication.config.plugin.springFreshSecurity.post.signup.url
            }
            ctx.emailConfirmationService.onInvalid = { uid -> 
                log.warn("User with id $uid failed to confirm email address after 30 days")
            }
            ctx.emailConfirmationService.onTimeout = { email, uid -> 
                log.warn("User with id $uid failed to confirm email address after 30 days")
            }
        }
    }

    def doWithConfigOptions = { 
        'guest.roles'(defaultValue:['ROLE_GUEST'], validator: { v -> 
            (v == null || !(v instanceof List)) ? 'A role list is required' : null
        })
        'default.roles'(defaultValue:['ROLE_USER'], validator: { v -> 
            (v == null || !(v instanceof List)) ? 'A role list is required' : null
        })
        'signup.allowed'(defaultValue:true)
        'post.signup.confirm.email'(defaultValue:true, validator: { v ->
            if (v) {
                def hasEmailConf = PluginManagerHolder.pluginManager.hasGrailsPlugin('email-confirmation')
                return hasEmailConf ? null : 'Email-Confirmation plugin must be installed'
            } else {
                return null
            }
        })
        'account.locked.until.email.confirm'(defaultValue:true)
        'post.login.url'(defaultValue:[uri:'/'])
        'post.signup.url'(defaultValue:[uri:'/'])
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
               '/test':         ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/index':        ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/**':           ['IS_AUTHENTICATED_ANONYMOUSLY']
            ]
        
            grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
            grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.grailsrocks.webprofile.security.SecUser'
            grails.plugins.springsecurity.userLookup.usernamePropertyName = 'userName'
            grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/auth/loginFail?error='
            grails.plugins.springsecurity.adh.errorPage = '/auth/denied'
            grails.plugins.springsecurity.auth.loginFormUrl = '/auth'
            grails.plugins.springsecurity.apf.usernameParameter = "user"
            grails.plugins.springsecurity.apf.passwordParameter = "password"

            grails.plugins.springsecurity.successHandler.defaultTargetUrl = 
                config.plugin.springFreshSecurity.post.login.url
            grails.plugins.springsecurity.successHandler.alwaysUseDefault = 
                config.plugin.springFreshSecurity.post.login.always_default

            if (config.grails.validateable.packages instanceof List) {
                config.grails.validateable.packages <<= 'com.grailsrocks.webprofile.security.forms'
            } else {
                config.grails.validateable.packages = ['com.grailsrocks.webprofile.security.forms']
            }
        }
    }
    
    def doWithApplicationContext = { applicationContext ->
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
