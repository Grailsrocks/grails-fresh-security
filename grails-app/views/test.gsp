<!doctype html>
<html>
	<head>
		<title>Test</title>
		<meta name="layout" content="main">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
	</head>
	<body>

        <h1>Fresh Security Test</h1>
        
        <p>You are logged in as: <s:userName/></p>

	    <fresh:loginForm/>

	    <fresh:signupForm/>
	</body>
</html>