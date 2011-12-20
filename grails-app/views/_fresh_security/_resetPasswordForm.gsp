<g:requiresBean name="form" class="com.grailsrocks.webprofile.security.forms.ResetPasswordFormCommand"/>
<g:if test="${form.hasErrors()}">
    <ui:message type="error"><p><g:message code="plugin.fresh.security.signup.screen.has.errors"/></p></ui:message>
</g:if>
<ui:form method="post" id="passwordResetForm" url="[action:'doPasswordReset', controller:'auth']">
<fieldset>
    <ui:field bean="${form}" name="password" type="password" tabindex="3"/>
    <ui:field bean="${form}" name="confirmPassword" type="password" tabindex="4"/>

    <ui:actions>
        <ui:button type="submit" mode="primary" text="action.set.new.password"/>
    </ui:actions>
</fieldset>
</ui:form>
