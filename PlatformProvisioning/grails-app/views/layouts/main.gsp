<html>
	<head>
		<title><g:layoutTitle default="Grails" /></title>
		<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}"></link>
		<g:layoutHead />		
	</head>
	<body>
        <div class="logo">
        	<img src="${createLinkTo(dir:'images',file:'cvq_bandeau_cg95.jpg')}" alt="Logo CG95" />
        </div>
        <g:displayUserInfo />
		<div class="nav">
			<span class="menuButton">
				<g:link controller="agent">Gestion des agents</g:link>
			</span>
			<span class="menuButton">
				<g:link controller="school">Gestion des écoles</g:link>
			</span>
			<span class="menuButton">
				<g:link controller="recreationCenter">Gestion des centres de loisirs</g:link>
			</span>
			<span class="empty">
				&nbsp;
			</span>
		</div>
		<g:layoutBody />		
	</body>	
</html>