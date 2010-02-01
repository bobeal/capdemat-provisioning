<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="school" />
		<title>Recherche d'écoles</title>
		<g:javascript library="prototype" />
		<g:javascript>
			var tryingDeleteSchool = '';
			function displaySuccessDelete() {
				document.getElementById('deleteResultStatus').innerHTML = "<div class=\"message\">L\'école " + tryingDeleteSchool + " a bien été supprimée</div>";
				document.getElementById(tryingDeleteSchool).style.display = "none";
				tryingDeleteSchool = '';
			}
			function displayFailureDelete() {
				document.getElementById('deleteResultStatus').innerHTML = "<div class=\"errors\">Une erreur s\'est produite lors de la suppression de l\'école " + tryingDeleteSchool + "</div>";
				tryingDeleteSchool = '';
			}
		</g:javascript>
	</head>
	<body>
		<div class="body">
			<h1>Recherche d'écoles</h1>
			<div id="deleteResultStatus"></div>
			
            <g:form action="search" method="post" >
               <div class="dialog">
                <table>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='name'>Nom :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'> 
							<input type='text' name='name' value='${name}' />
						</td>
					</tr>
               </table>
               </div>
               <div class="buttons">
                     <span class="formButton">
                        <input type="submit" value="Chercher"></input>
                     </span>
                     <span class="formButton">
                        <input type="reset" value="Annuler"></input>
                     </span>
               </div>
            </g:form>
			<g:if test="${schoolList}">
				<table>
					<tr>
						<th>Nom</th>
						<th>Adresse</th>
						<th>Numéro de téléphone</th>
						<th>Adresse mail</th>
						<th></th>
					</tr>
					<g:each in="${schoolList}">
					<tr id="${it.name}">
						<td>${it.name}</td>
						<td>${it.address}</td>
						<td>${it.telephoneNumber}</td>
						<td>${it.email}</td>

						<td class="actionButtons">
							<span class="actionButton">
								<g:link action="edit" id="${it.name}">Modifier</g:link>
								<a href="javascript:void(0);" onclick="if(confirm('Voulez-vous supprimer l\'école ${it.name} ?')) { tryingDeleteSchool = '${it.name}'; ${remoteFunction(action:"delete",id:it.name,onSuccess="displaySuccessDelete()",onFailure="displayFailureDelete()")} } return false;">Supprimer</a>
							</span>
						</td>
					</tr>
					</g:each>
				</table>
			</g:if>
		</div>
	</body>
</html>