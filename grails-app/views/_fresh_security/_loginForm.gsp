<g:requiresBean name="loginForm" class="com.grailsrocks.webprofile.security.forms.LoginFormCommand"/>
<fresh:ifUiMessage>
    <ui:message><fresh:uiMessage/></ui:message>
</fresh:ifUiMessage>

<ui:form method="post" id="loginForm" url="${createLinkTo(dir: '/j_spring_security_check')}">
<fieldset>
     <ui:field bean="${loginForm}" name="userName" tabindex="1"/>
     <ui:field bean="${loginForm}" name="password" tabindex="2"/>
     <ui:field bean="${loginForm}" name="rememberMe" tabindex="3"/>

     <ui:actions>
        <ui:button type="submit" tabindex="4">Log in</ui:button>
        <fresh:ifSignupAllowed>
        or <g:link controller="auth" action="signup">Sign up</g:link>
        </fresh:ifSignupAllowed>
     </ui:actions>
</fieldset>
</ui:form>
