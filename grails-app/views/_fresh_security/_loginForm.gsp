<div class="login-form">
<p class="form-title"><g:message code="login.form.title" encodeAs="HTML"/></p>
<form method="post" id="loginForm" action="${createLinkTo(dir: 'j_spring_security_check')}">
<fieldset>
<ul>

    <li>
       <label for="j_username" class="desc">
            Username <span class="req">*</span>
        </label>
        <input type="text" class="text medium" name="j_username" id="j_username" tabindex="1" />
    </li>

    <li>
        <label for="j_password" class="desc">
            Password <span class="req">*</span>
        </label>
        <input type="password" class="text medium" name="j_password" id="j_password" tabindex="2" />
    </li>


    <li>
        <input type="checkbox" class="checkbox" name="rememberMe" id="rememberMe" tabindex="3"/>
        <label for="rememberMe" class="choice">Remember Me</label>
    </li>

    <li>
        <input type="submit" name="submit" value="Login" tabindex="4" />
        <fresh:ifSignupAllowed>
        or <g:link controller="auth" action="signup">Sign up</g:link>
        </fresh:ifSignupAllowed>

    </li>
</ul>
</fieldset>
</form>
</div>
