<p:requiresBean name="form" class="com.grailsrocks.webprofile.security.forms.PasswordResetFormCommand"/>

<p><g:message code="plugin.freshSecurity.reset.password.form.text" encodeAs="HTML"/></p>

<g:if test="${form.hasErrors()}">
    <ui:message type="error"><p><g:message code="plugin.freshSecurity.reset.password.screen.has.errors"/></p></ui:message>
</g:if>

<ui:displayMessage/>

<ui:form method="post" id="passwordResetForm" url="[action:'doResetPassword', controller:'auth']">
    <ui:fieldGroup>
        <ui:field bean="${form}" name="newPassword" type="password" tabindex="3"/>
        <ui:field bean="${form}" name="confirmPassword" type="password" tabindex="4"/>
    </ui:fieldGroup>

    <ui:actions>
        <ui:button type="submit" mode="primary" text="action.auth.doResetPassword"/>
    </ui:actions>
</ui:form>
