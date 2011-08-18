<div class="login-form">
<p class="form-title"><g:message code="login.form.title" encodeAs="HTML"/></p>
<form method="post" id="loginForm" action="${createLinkTo(dir: 'j_spring_security_check')}">
<fieldset>
<ul>

    <li>
       <label for="loginUser" class="desc">
            Username <span class="req">*</span>
        </label>
        <input type="text" name="user" id="loginUser" tabindex="1" />
    </li>

    <li>
        <label for="loginPassword" class="desc">
            Password <span class="req">*</span>
        </label>
        <input type="password" name="password" id="loginPassword" tabindex="2" />
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
