<p:requiresBean name="loginForm" class="com.grailsrocks.webprofile.security.forms.LoginFormCommand"/>

<ui:form method="post" id="loginForm" url="${createLink(uri: '/j_spring_security_check')}">
    <ui:fieldGroup>
        <ui:field bean="${loginForm}" name="identity" tabindex="1"/>
        <ui:field bean="${loginForm}" name="password" type="password" tabindex="2"/>
        <fresh:ifRememberMeAllowed>
            <ui:field bean="${loginForm}" name="rememberMe" tabindex="3"/>
        </fresh:ifRememberMeAllowed>
    </ui:fieldGroup>
     
    <ui:actions>
        <ui:button type="submit" tabindex="4" text="action.auth.login"/>
        <fresh:ifSignupAllowed>
            or <p:smartLink controller="auth" action="signup"/>
        </fresh:ifSignupAllowed>
    </ui:actions>
</ui:form>
