<bean:require beanName="form" className="com.grailsrocks.webprofile.security.forms.SignupFormCommand"/>
<g:if test="${form.hasErrors()}">
    <ui:message type="error"><p><g:message code="plugin.fresh.security.signup.screen.has.errors"/></p></ui:message>
</g:if>
<ui:form method="post" id="signupForm" url="[action:'doSignup', controller:'auth']">
<bean:inputTemplate>
    <ui:group error="${errors}">
        <ui:label>${label}</ui:label>
        <ui:field>${field}<g:if test="${errors}"><ui:fieldHint>${errors}</ui:fieldHint></g:if></ui:field>
    </ui:group>
</bean:inputTemplate>
<fieldset>
    <bean:input beanName="form" property="userName"/>
    <bean:input beanName="form" property="email"/>
    <bean:input type="password" beanName="form" property="password"/>
    <bean:input type="password" beanName="form" property="confirmPassword"/>

    <ui:group>
        <ui:label>
            <label for="rememberMe" class="choice">Remember Me</label>
        </ui:label>
        <ui:field>
            <input type="checkbox" class="checkbox" name="rememberMe" id="rememberMe" tabindex="4"/>
        </ui:field>
    </ui:group>

    <ui:actions>
        <ui:button type="submit" mode="primary">Sign up</ui:button>
    </ui:actions>
</fieldset>
</ui:form>
