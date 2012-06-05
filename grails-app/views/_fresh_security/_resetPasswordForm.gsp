<p:requiresBean name="form" class="com.grailsrocks.webprofile.security.forms.PasswordResetFormCommand"/>

<g:if test="${form.hasErrors()}">
    <ui:message type="error"><p><g:message code="plugin.freshSecurity.reset.password.screen.has.errors"/></p></ui:message>
</g:if>

<ui:form method="post" id="passwordResetForm" url="[action:'doResetPassword', controller:'auth']">
<fieldset>
    <ui:field bean="${form}" name="newPassword" type="password" tabindex="3"/>
    <ui:field bean="${form}" name="confirmPassword" type="password" tabindex="4"/>

    <ui:actions>
        <ui:button type="submit" mode="primary" text="action.auth.doResetPassword"/>
    </ui:actions>
</fieldset>
</ui:form>
