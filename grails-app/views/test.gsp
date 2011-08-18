<!doctype html>
<html>
	<head>
		<title>Test</title>
		<meta name="layout" content="main">
		<link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
	</head>
	<body>
	    User: <g:userId/>

	    <fresh:loginForm/>

	    <fresh:signupForm/>
	</body>
</html>