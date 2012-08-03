<p:requiresBean name="forgotForm" class="com.grailsrocks.webprofile.security.forms.ForgotPasswordFormCommand"/>

<p><g:message code="plugin.freshSecurity.forgot.password.form.text" encodeAs="HTML"/></p>

<ui:form method="post" id="forgotForm" url="[action:'doForgotPassword']">
     <ui:fieldGroup>
        <ui:field bean="${forgotForm}" name="email" tabindex="1"/>
     </ui:fieldGroup>

     <ui:actions>
        <ui:button kind="button" mode="primary" tabindex="2" text="action.auth.doForgotPassword"/>
        <p:text code="ui.or" encodeAs="HTML"/>
        <ui:button kind="anchor" mode="secondary" text="ui.cancel" uri="/"/>        
     </ui:actions>
</ui:form>
