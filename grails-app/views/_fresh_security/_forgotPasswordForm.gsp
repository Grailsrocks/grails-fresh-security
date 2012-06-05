<p:requiresBean name="forgotForm" class="com.grailsrocks.webprofile.security.forms.ForgotPasswordFormCommand"/>

<p><g:message code="plugin.freshSecurity.forgot.password.form.text" encodeAs="HTML"/></p>

<ui:form method="post" id="forgotForm" url="[action:'doForgotPassword']">
<fieldset>
     <ui:field bean="${forgotForm}" name="email" tabindex="1"/>

     <ui:actions>
        <ui:button kind="button" mode="danger" tabindex="2" text="action.auth.doForgotPassword"/>
        <g:message code="ui.or" encodeAs="HTML"/>
        <ui:button kind="anchor" mode="secondary" text="ui.cancel" uri="/"/>        
     </ui:actions>
</fieldset>
</ui:form>
