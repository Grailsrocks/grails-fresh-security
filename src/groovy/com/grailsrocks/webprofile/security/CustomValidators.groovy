package com.grailsrocks.webprofile.security

class CustomValidators {
    static final confirmPassword = { v, obj ->
        obj.password != v ? 'password.must.match' : null
    }

    static final confirmNewPassword = { v, obj ->
        obj.newPassword != v ? 'password.must.match' : null
    }

    static final passwordStrength = { v, obj ->
        /*
		if (instance.userName && instance.userName.equals(password)) {
			return 'password.cannot.be.username'
		}
		*/

        // @todo externalize the strength calculation via event or similar
/*
		if (password &&
				(!password.matches('^.*\\p{Alpha}.*$') ||
				!password.matches('^.*\\p{Digit}.*$') ||
				!password.matches('^.*[!@#$%^&].*$') ) ) {
			return 'password.too.weak'
		}
*/		
		return null
	}

    static final passwordStrengthAndUserName = { password, instance ->
		if (instance.userName && instance.userName.equals(password)) {
			return 'password.cannot.be.username'
		}

        // @todo externalize the strength calculation via event or similar
/*
		if (password &&
				(!password.matches('^.*\\p{Alpha}.*$') ||
				!password.matches('^.*\\p{Digit}.*$') ||
				!password.matches('^.*[!@#$%^&].*$') ) ) {
			return 'password.too.weak'
		}
*/		
		return null
	}
}