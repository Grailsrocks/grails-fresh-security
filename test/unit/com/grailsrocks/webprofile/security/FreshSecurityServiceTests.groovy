package com.grailsrocks.webprofile.security

@TestFor(FreshSecurityService)
class FreshSecurityServiceTests {
    @Before void setUp() {
        println "IN SETUP"
        def pluginConfig = new ConfigObject() // empty
        service.metaClass.getPluginConfig = { -> pluginConfig }
    }

    @After void tearDown() {
        println "IN TEARDOWN"
        service.metaClass.getPluginConfig = null
    }

    void testBypassDisabledInConfigAndPatternMatchingNeverRuns() {
        service.pluginConfig.allow.confirm.bypass = false
        service.pluginConfig.confirm.bypass.pattern = ~/.*/

        boolean eventCalled = false
        service.metaClass.event = { Map args -> 
            eventCalled = true
            return [value:null]
        }

        def user = new NewUserAccount(identity:'testuser', confirmBypass:true)

        assert service.isConfirmationRequiredForNewUser(user)
        assert !eventCalled
    }

    void testBypassDisabledInConfigAndAppEventNeverRuns() {
        service.pluginConfig.allow.confirm.bypass = false

        boolean eventCalled = false
        service.metaClass.event = { Map args -> 
            eventCalled = true
            return [value:null]
        }

        def user = new NewUserAccount(identity:'testuser', confirmBypass:true)

        assert service.isConfirmationRequiredForNewUser(user)
        assert !eventCalled
    }

    void testBypassPermittedInConfigDoesNotSimplyAllowBypassOnAll() {
        service.pluginConfig.allow.confirm.bypass = true

        service.metaClass.event = { Map args -> 
            return [value:null]
        }

        def user = new NewUserAccount(identity:'testuser', confirmBypass:null)

        assert service.isConfirmationRequiredForNewUser(user)

        user = new NewUserAccount(identity:'testuser', confirmBypass:false)

        assert service.isConfirmationRequiredForNewUser(user)
    }

    void testBypassPermittedInConfigAndMatchesPattern() {
        service.pluginConfig.allow.confirm.bypass = true
        service.pluginConfig.confirm.bypass.pattern = ~/testuser/

        service.metaClass.event = { Map args -> 
            return [value:null]
        }

        def user = new NewUserAccount(identity:'testuser', confirmBypass:true)

        assert !service.isConfirmationRequiredForNewUser(user)
    }

    void testBypassPermittedInConfigAndAppEventPermits() {
        service.pluginConfig.allow.confirm.bypass = true
        service.pluginConfig.confirm.bypass.pattern = ~/testuser/

        boolean eventCalled = false
        service.metaClass.event = { Map args -> 
            eventCalled = true
            return [value:true]
        }
        def user = new NewUserAccount(identity:'testuser', confirmBypass:null)

        assert !service.isConfirmationRequiredForNewUser(user)
        assert eventCalled
    }

    void testBypassPermittedInConfigAndRequestedAndAppEventPermits() {
        service.pluginConfig.allow.confirm.bypass = true
        service.pluginConfig.confirm.bypass.pattern = ~/testuser/

        boolean eventCalled = false
        service.metaClass.event = { Map args -> 
            eventCalled = true
            return [value:true]
        }
        def user = new NewUserAccount(identity:'testuser', confirmBypass:true)

        assert !service.isConfirmationRequiredForNewUser(user)
        assert eventCalled
    }

    void testBypassPermittedInConfigAndNotRequestedAndAppEventPermits() {
        service.pluginConfig.allow.confirm.bypass = true
        service.pluginConfig.confirm.bypass.pattern = ~/testuser/

        boolean eventCalled = false
        service.metaClass.event = { Map args -> 
            eventCalled = true
            return [value:true]
        }
        def user = new NewUserAccount(identity:'testuser', confirmBypass:false)

        assert !service.isConfirmationRequiredForNewUser(user)
        assert eventCalled
    }

    void testBypassPermittedInConfigAndRequestedAndAppEventVetoes() {
        service.pluginConfig.allow.confirm.bypass = true
        service.pluginConfig.confirm.bypass.pattern = ~/testuser/

        boolean eventCalled = false
        service.metaClass.event = { Map args -> 
            eventCalled = true
            return [value:false]
        }
        def user = new NewUserAccount(identity:'testuser', confirmBypass:true)

        assert service.isConfirmationRequiredForNewUser(user)
        assert eventCalled
    }
}
