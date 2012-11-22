import grails.plugins.springsecurity.SecurityConfigType

import java.util.regex.Pattern
import groovy.util.ConfigObject

import org.codehaus.groovy.grails.plugins.PluginManagerHolder

import com.grailsrocks.webprofile.security.*

class FreshSecurityGrailsPlugin {
    // the plugin version
    def version = "1.0.2-RC1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.7 > *"

    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/domain/test/**/*.*",
        "grails-app/views/error.gsp",
        "grails-app/views/index.gsp",
        "grails-app/conf/PluginConfig.groovy"
    ]

    def loadAfter = ['springSecurityCore', 'emailConfirmation', 'platformCore'] // We must apply our beans AFTER spring-sec declares its own
    
    def title = "Fresh Security Plugin" // Headline display name of the plugin
    def author = "Marc Palmer"
    def authorEmail = "marc@grailsrocks.com"
    def description = '''\
Security that "just works", backed by Spring Security
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/fresh-security"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
    def organization = [ name: "Grails Rocks", url: "http://grailsrocks.com/" ]

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Marc Palmer", email: "marc@grailsrocks.com" ]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPFRESHSECURITY" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "http://github.com/grailsrocks/grails-fresh-security" ]

    def doWithWebDescriptor = { xml ->
    }

    def doWithSpring = {        
        userDetailsService(com.grailsrocks.webprofile.security.FreshSecurityUserDetailsService) {
            grailsApplication = ref('grailsApplication')
        }
        grailsSecurityBridge(com.grailsrocks.webprofile.security.FreshSecurityProvider) {
            springSecurityService = ref('springSecurityService')
            grailsApplication = ref('grailsApplication')
            freshSecurityService = ref('freshSecurityService')
        }
    }

    def doWithDynamicMethods = { ctx ->
    }

    def doWithConfigOptions = { 
        'guest.roles'(type:List, defaultValue:['ROLE_GUEST'], validator: { v -> 
            (v == null) ? 'A role list is required' : null
        })
        'default.roles'(type:List, defaultValue:['ROLE_USER'], validator: { v -> 
            (v == null) ? 'A role list is required' : null
        })
        'signup.allowed'(defaultValue:true)
        'signup.command.class.for.identity.mode.userid'(type:String, defaultValue:'com.grailsrocks.webprofile.security.forms.SignupWithUserIdFormCommand', 
            validator: { v -> v ? null : 'A value is required'})
        'signup.command.class.for.identity.mode.email'(type:String, defaultValue:'com.grailsrocks.webprofile.security.forms.SignupWithEmailFormCommand', 
            validator: { v -> v ? null : 'A value is required'})
        'remember.me.allowed'(defaultValue:true, type:Boolean)
        'confirm.email.on.signup'(defaultValue:false, type:Boolean, validator: { v ->
            if (v) {
                def hasEmailConf = PluginManagerHolder.pluginManager.hasGrailsPlugin('email-confirmation')
                return hasEmailConf ? null : 'Email-Confirmation plugin must be installed for confirmations to be enabled'
            } else {
                return null
            }
        })
        'identity.mode'(defaultValue:'userid', type:String, validator: { v -> v in ['email', 'userid'] ? null : 'Must be [email] or [userid]'} )
        'password.reset.mode'(defaultValue:'setnew', type:String, validator: { v -> v in ['setnew', 'generate'] ? null : 'Must be [setnew] or [generate]'})
        'account.locked.until.email.confirm'(defaultValue:false, type:Boolean)
        'post.login.url'(defaultValue:[uri:'/'], type:Map)
        'post.signup.url'(defaultValue:[uri:'/'], type:Map)
        'bad.confirmation.url'(defaultValue:[uri:'/bad-confirmation'], type:Map)
        'user.object.class.name'(defaultValue:'', type:String)
        'allow.confirm.bypass'(defaultValue:false, type:Boolean)
        'confirm.bypass.pattern'(defaultValue:null, type:Pattern)
    }
    
    def doWithConfig = { config ->

        application {
            // Get our config values and use them to apply to Spring security's config by
            // modifying global config
            // This needs to be set as the default, but user can override using sconfig
            grails.plugins.springsecurity.interceptUrlMap = [
               '/admin/**':     ['ROLE_ADMIN'],
               '/static/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/js/**':        ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/css/**':       ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/images/**':    ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/auth/**':      ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/index':        ['IS_AUTHENTICATED_ANONYMOUSLY'],
               '/**':           ['IS_AUTHENTICATED_ANONYMOUSLY']
            ]
        
            grails.plugins.springsecurity.securityConfigType = SecurityConfigType.InterceptUrlMap
            grails.plugins.springsecurity.userLookup.userDomainClassName = 'com.grailsrocks.webprofile.security.SecUser'
            grails.plugins.springsecurity.userLookup.usernamePropertyName = 'identity'  

            // @todo these have been hardcoded to "auth", need to pull that from config
            grails.plugins.springsecurity.failureHandler.defaultFailureUrl = '/auth/loginFail'
            //grails.plugins.springsecurity.adh.errorPage = null
            //grails.plugins.springsecurity.failureHandler.useForward = true // force render of 403 response, not redirect to errorPage
            //grails.plugins.springsecurity.adh.errorPage = '/auth/denied'
            grails.plugins.springsecurity.auth.loginFormUrl = '/auth'

            grails.plugins.springsecurity.apf.usernameParameter = "identity"
            grails.plugins.springsecurity.apf.passwordParameter = "password"
            grails.plugins.springsecurity.rememberMe.parameter = "rememberMe"

            // Lock down everything
            grails.plugins.springsecurity.rejectIfNoRule = true

            if (config.grails.validateable.packages instanceof List) {
                config.grails.validateable.packages <<= 'com.grailsrocks.webprofile.security.forms'
            } else {
                config.grails.validateable.packages = ['com.grailsrocks.webprofile.security.forms']
            }
            
        }
        
        // Configure ourselves based on other app config settings
        freshSecurity {
            // Force confirm email to true if using email as id
            if (config.plugin.freshSecurity.identity.mode == 'email') {
                account.locked.until.email.confirm = true
                confirm.email.on.signup = true
            }

            if (config.plugin.freshSecurity.account.locked.until.email.confirm) {
                confirm.email.on.signup = true
            }
        }
    }
    
    def doWithApplicationContext = { applicationContext ->
    }

    def onChange = { event ->
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    def onShutdown = { event ->
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
