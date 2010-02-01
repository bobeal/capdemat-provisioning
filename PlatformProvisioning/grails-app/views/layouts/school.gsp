<html>
	<head>
		<title><g:layoutTitle default="Gestion des ecoles" /></title>
		<link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}"></link>
		<g:layoutHead />		
	</head>
	<body onload="<g:pageProperty name='body.onload'/>">
        <div class="logo">
        	<img src="${createLinkTo(dir:'images',file:'cvq_bandeau_cg95.jpg')}" alt="Logo CG95" />
        </div>	
        <g:displayUserInfo />
		<div class="nav">
			<span class="menuButton">
				<a href="${createLink(controller:'main')}">Accueil</a>
			</span>
			<span class="menuButton">
				<g:link action="search">Chercher une école</g:link>
			</span>
			<span class="menuButton">
				<g:link action="create">Créer une école</g:link>
			</span>
		</div>
		<g:layoutBody />		
	</body>	
</html>