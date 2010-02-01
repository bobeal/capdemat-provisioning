<html>
    <head>
         <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
         <meta name="layout" content="agent" />
         <title>Création/Modification d'un agent</title>
		 <g:javascript library="prototype" />
		 <g:javascript src="validation.js" />
         <g:javascript>
         	Validation.add('IsShort', '', function(v) {
				return ((v == null) || (v.length < 8) || /^\s+$/.test(v));
			});
         	Validation.add('IsNotNullShort', '', function(v) {
				return ((v.length < 8) || /^\s+$/.test(v));
			});

			Validation.addAllThese([
				['validate-pwd-length', 'Votre mot de passe est trop court', function(v) {
					return !Validation.get('IsShort').test(v);
				}],
				['validate-equalto', 'Les mots de passe doivent correspondre', function(v) {
					var elmEqualto = $$("input.validate-equalto");
					return (elmEqualto[0].value) === (elmEqualto[1].value);
				}],
				['validate-equalto-if-filled', '', function(v) {
					if (v == null || v.length == 0)
						return true;
					var elmEqualto = $$("input.validate-equalto-if-filled");
					if (!((elmEqualto[0].value) === (elmEqualto[1].value))) {
						//alert("not matching");
						return false;
					}
					if (v.length < 8) {
						//alert("length is bad");
						return false;
					}
					return true;
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
				<h1>Création d'un agent</h1>
        	</g:if>
        	<g:if test="${mode == 'update'}">
				<h1>Modification de l'agent ${agent.uid}</h1>
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
               <input type="hidden" name="uid" value="${agent?.uid}" />
               <div class="dialog">
                <table>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='lastName'>Nom :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'> 
							<input type='text' id='lastName' name='lastName' value='${agent?.lastName}' 
								class='required' title='Le nom est obligatoire'/>
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='lastName'>Prénom :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='text' id='firstName' name='firstName' value='${agent?.firstName}' 
								class='required' title='Le prénom est obligatoire' />
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='password'>Mot de passe :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'> 
							<input type='password' id='password' name='password' 
								class='<g:if test="${mode == 'create'}">required validate-pwd-length validate-equalto</g:if><g:else>validate-equalto-if-filled</g:else>' 
								title='Le mot de passe est obligatoire et doit faire plus de 8 caractères' />
							<g:if test="${mode == 'update'}">
								(laisser le champ vide si vous ne souhaitez pas le changer)
							</g:if>
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='password2'>Mot de passe (confirmation) :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='password' id='password2' name='password2' 
								class='<g:if test="${mode == 'create'}">required validate-equalto</g:if><g:else>validate-equalto-if-filled</g:else>' 
								title='Les deux mots de passe doivent étre identiques' />
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='email'>Email :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='text' id='email' name='email' value='${agent?.email}' 
								class='validate-email' title='Le mail a un format invalide' />
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='telephoneNumber'>Numéro de téléphone :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<input type='text' id='telephoneNumber' name='telephoneNumber' 
								value='${agent?.telephoneNumber}' class='validate-phone'
								title='Le numéro de téléphone doit étre composé de 10 chiffres'/>
						</td>
					</tr>
					<tr class='prop'>
						<td valign='top' style='text-align:left;' width='20%'>
							<label for='groups'>Groupes :</label>
						</td>
						<td valign='top' style='text-align:left;' width='80%'>
							<g:groupsMultipleSelect name="groups" reference="${groups}"
								agentGroups="${agent?.groups}"/>
						</td>
					</tr>
               </table>
               </div>
               <div class="buttons">
                     <span class="formButton">
                        <input type="submit" value="Enregistrer"></input>
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
            