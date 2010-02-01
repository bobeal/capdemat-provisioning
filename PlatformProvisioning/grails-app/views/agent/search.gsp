<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<meta name="layout" content="agent" />
		<title>Recherche d'agents</title>
		<g:javascript library="prototype" />
		<g:javascript>
			var tryingDeleteAgent = '';
			function displaySuccessDelete() {
				document.getElementById('deleteResultStatus').innerHTML = "<div class=\"message\">L\'agent " + tryingDeleteAgent + " a bien été supprimé</div>";
				document.getElementById(tryingDeleteAgent).style.display = "none";
				tryingDeleteAgent = '';
			}
			function displayFailureDelete() {
				document.getElementById('deleteResultStatus').innerHTML = "<div class=\"errors\">Une erreur s\'est produite lors de la suppression de l\'agent " + tryingDeleteAgent + "</div>";
				tryingDeleteAgent = '';
			}
		</g:javascript>
	</head>
	<body>
		<div class="body">
			<h1>Recherche d'agents</h1>
			<div id="deleteResultStatus"></div>
			
            <g:form action="search" method="post" >
               <div class="dialog">
                <table>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='lastName'>Nom :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'> 
							<input type='text' name='lastName' value='${lastName}' />
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='lastName'>Prénom :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'> 
							<input type='text' name='firstName' value='${firstName}' />
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='uid'>UID :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'> 
							<input type='text' name='uid' value='${uid}' />
						</td>
					</tr>
               </table>
               </div>
               <div class="buttons">
                     <span class="formButton">
                        <input type="submit" value="Chercher" />
                     </span>
               </div>
            </g:form>

			<g:if test="${agentList}">
				<table>
					<tr>
						<th>UID</th>
						<th>Nom</th>
						<th>Prénom</th>
						<th>Mail</th>
						<th>Numéro de téléphone</th>
						<th>Groupes</th>
						<th></th>
					</tr>
					<g:each in="${agentList}">
						<tr id="${it.uid}">
							<td>${it.uid}</td>
							<td>${it.lastName}</td>
							<td>${it.firstName}</td>
							<td>${it.email}</td>
							<td>${it.telephoneNumber}</td>
							<td>
								<g:each var="group" in="${it.groups}">
									${groups.get(group)}<br/>
								</g:each>
							</td>
							<td class="actionButtons">
								<span class="actionButton">
									<g:link action="edit" id="${it.uid}">Modifier</g:link>
									<a href="javascript:void(0);" onclick="if(confirm('Voulez-vous supprimer l\'agent ${it.uid} ?')) { tryingDeleteAgent = '${it.uid}'; ${remoteFunction(action:"delete",id:it.uid,onSuccess="displaySuccessDelete()",onFailure="displayFailureDelete()")} } return false;">Supprimer</a>
								</span>
							</td>
						</tr>
					</g:each>
				</table>
			</g:if>
		</div>
	</body>
</html>
