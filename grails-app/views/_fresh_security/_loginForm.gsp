<ui:form method="post" id="loginForm" action="${createLinkTo(dir: 'j_spring_security_check')}">
<fieldset>
    <ui:group>
        <ui:label>
            <label for="loginUser" class="desc">Username <span class="req">*</span></label>
        </ui:label>
        <ui:field><input type="text" name="user" id="loginUser" tabindex="1" /></ui:field>
    </ui:group>

    <ui:group>
        <ui:label>
            <label for="loginPassword" class="desc">Password <span class="req">*</span></label>
        </ui:label>
        <ui:field><input type="password" name="password" id="loginPassword" tabindex="2" /></ui:field>
    </ui:group>

    <ui:group>
        <ui:label>
            <label for="rememberMe" class="choice">Remember Me</label>
        </ui:label>
        <ui:field>
            <input type="checkbox" class="checkbox" name="rememberMe" id="rememberMe" tabindex="3"/>
        </ui:field>
    </ui:group>

    <ui:actions>
        <ui:button type="submit" tabindex="4">Log in</ui:button>
        <fresh:ifSignupAllowed>
        or <g:link controller="auth" action="signup">Sign up</g:link>
        </fresh:ifSignupAllowed>
    </ui:actions>
</fieldset>
</ui:form>
