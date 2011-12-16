<g:requiresBean name="form" class="com.grailsrocks.webprofile.security.forms.SignupFormCommand"/>
<g:if test="${form.hasErrors()}">
    <ui:message type="error"><p><g:message code="plugin.fresh.security.signup.screen.has.errors"/></p></ui:message>
</g:if>
<ui:form method="post" id="signupForm" url="[action:'doSignup', controller:'auth']">
<fieldset>

    <ui:field bean="${form}" name="userName" tabindex="1"/>
    <ui:field bean="${form}" name="email" tabindex="2"/>
    <ui:field bean="${form}" name="password" type="password" tabindex="3"/>
    <ui:field bean="${form}" name="confirmPassword" type="password" tabindex="3"/>
    <ui:field bean="${form}" name="rememberMe" tabindex="4"/>

    <ui:actions>
        <ui:button type="submit" mode="primary">Sign up</ui:button>
    </ui:actions>
</fieldset>
</ui:form>
