<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="school" />
         <title>Création/Modification d'une Ã©cole</title>
		 <g:javascript library="prototype" />
		 <g:javascript src="validation.js" />
         <g:javascript>
			Validation.addAllThese([
				['validate-not-empty', 'Le nom est obligatoire', function(v) {
					return !Validation.get('IsEmpty').test(v);
				}],
				['validate-phone', '', function(v) {
					return Validation.get('IsEmpty').test(v) || /^\d{10}$/.test(v);
				}]
			]);
         </g:javascript>       
    </head>
    <body>
        <div class="body">
        	<g:if test="${mode == 'create'}">
				<h1>Création d'une école</h1>
        	</g:if>
        	<g:if test="${mode == 'update'}">
				<h1>Modification de l'école "${school.name}"</h1>
        	</g:if>
            <g:if test="${resultMessage}">
                 <div class="message">
                 	${resultMessage}
                 	<g:if test="${provisioningResultsMessageMap}">
	                 	<br/>Statuts provisioning :
                 		<g:each in="${provisioningResultsMessageMap}">
                 			<br/>&nbsp;&nbsp;&nbsp;${it.key}&nbsp;:&nbsp;${it.value}
                 		</g:each>
    				</g:if>             	
                 </div>
            </g:if>
           <g:form action="save" method="post" >
               <input type="hidden" name="mode" value="${mode}" />
               <g:if test="${mode == 'update'}" >
               		<input type="hidden" name="name" value="${school.name}" />
               </g:if>
               <div class="dialog">
                <table>
                	<g:if test="${mode == 'create'}" >
							<tr class='prop'>
								<td valign='top' style='text-align:left;' width='20%'>
									<label for='name'>Nom :</label>
								</td>
								<td valign='top' style='text-align:left;' width='80%'> 
									<input type='text' id='name' name='name' value='${school?.name}' 
										class='required validate-not-empty' title='Le nom est obligatoire'/>
								</td>
							</tr>
					</g:if>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='address'>Adresse :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='text' id='address' name='address' value='${school?.address}' 
								class='required validate-not-empty' title='Adresse obligatoire' />
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='telephoneNumber'>Numéro de téléphone :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='text' id='telephoneNumber' name='telephoneNumber' 
								value='${school?.telephoneNumber}' class='validate-phone'
								title='Le numéro de téléphone doit être composé de 10 chiffres'/> (0101010101)
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='email'>Email :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='text' id='email' name='email' 
								value='${school?.email}' class='validate-email'
								title='Le mail a un format invalide'/>
						</td>
					</tr>
               </table>
               </div>
               <div class="buttons">
                     <span class="formButton">
                        <input type="submit" value="Enregistrer"></input>
                     </span>
                     <span class="formButton">
                        <input type="reset" value="Annuler"></input>
                     </span>
               </div>
            </g:form>
			<g:javascript>
				<g:if test="${session.isSuperAdmin}">
					new Validation(document.forms[1], {useTitles:true});
				</g:if>
				<g:else>
					new Validation(document.forms[0], {useTitles:true});
				</g:else>
			</g:javascript>
        </div>
    </body>
</html>