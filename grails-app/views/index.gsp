<html>
	<head>
		<title>Test</title>
		<theme:layout name="main"/>
	</head>
	<body>
        <theme:zone name="body">
            <h1>Fresh Security Test</h1>
        
            <p>You are logged in as: <s:userName/></p>

    	    <fresh:loginForm/>

    	    <fresh:signupForm/>

    	    <fresh:logoutLink>Log out</fresh:logoutLink>
	    </theme:zone>
	</body>
</html>