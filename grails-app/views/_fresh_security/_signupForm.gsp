<div class="signup-form">
<p class="form-title"><g:message code="signup.form.title" encodeAs="HTML"/></p>
<g:form method="post" id="signupForm" url="[action:'doSignup', controller:'auth']">
<bean:require beanName="form" className="com.grailsrocks.webprofile.security.forms.SignupFormCommand"/>
<fieldset>
<ul>
    <li><bean:input beanName="form" property="userName"/></li>
    <li><bean:input beanName="form" property="email"/></li>
    <li><bean:input type="password" beanName="form" property="password"/></li>
    <li><bean:input type="password" beanName="form" property="confirmPassword"/></li>

    <li>
        <input type="checkbox" class="checkbox" name="rememberMe" id="rememberMe" tabindex="4"/>
        <label for="rememberMe" class="choice">Remember Me</label>
    </li>

    <li>
        <input type="submit" name="submit" value="Sign up" tabindex="5" />

    </li>
</ul>
</fieldset>
</g:form>
</div>
