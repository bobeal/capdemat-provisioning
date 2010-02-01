import fr.cg95.admin.business.Agent
import java.net.URLDecoder

class AgentController extends BaseController {
    
    AgentService agentService
    ProvisioningService provisioningService

    def index = { 
    	redirect(controller:'agent',action:'search',params:params)
    	return false
    }

    def login = {
            
        if(session.user) {
            redirect(controller:'main')
            return false
    	} else {
            def localAuthorities = agentService.getAllLocalAuthorities()
    	    return ['localAuthorities':localAuthorities ]
    	}
    }

    def handleLogin = {

        if(params.login && params.pwd) {
            log.debug "Super admin : '${params.superAdmin}'"
            def agent = agentService.authenticate(params.login, params.pwd,
                params.localAuthority, params.superAdmin == 'on' ? true : false)
            if(agent) {
                session.user = agent
                session.localAuthority = params.localAuthority
                session.isSuperAdmin = params.superAdmin
                if (session.isSuperAdmin)
                session.localAuthorities = agentService.getAllLocalAuthorities()
                    redirect(controller:'main')
                return false
            } else {
               	flash.message = "Echec d'authentification pour le login '${params.login}' sur la ville '${params.localAuthority}'"
                def localAuthorities = agentService.getAllLocalAuthorities()
               	render(view:'login', model:['localAuthorities':localAuthorities ])
            }
        } else {
            flash.message = 'Identifiant ou mot de passe non fourni'
            def localAuthorities = agentService.getAllLocalAuthorities()
        	render(view:'login', model:['localAuthorities':localAuthorities ])
        }
    }

    def logout = {
    	session.user = null
    	session.localAuthority = null
    	session.isSuperAdmin = null
    	redirect(action:'login')
    	return false
    }
    
    def search = {

        def lastName = params.lastName
        def firstName = params.firstName
        def uid = params.uid
		
        return [ agentList: agentService.search(session["localAuthority"],
                lastName, firstName, uid), 'lastName': lastName, 'firstName': firstName,
	        'uid': uid, 'groups':agentService.getAllGroups(session["localAuthority"]) ]
    }

    // called asynchronously, only renders operation result that will be displayed
    // in a special div
    def delete = {
        if (params.id) {
            try {
    	        provisioningService.deleteAgent(session["localAuthority"], params.id)
            } catch (Exception e) {
            	renderError(params.id)
            }
        } else {
            renderError(params.id)
        }
    }

    def create = {
        def groups = agentService.getAllGroups(session["localAuthority"])
        render(view:'createOrUpdate', model:['mode':'create', 'groups':groups ])
    }

    def edit = {
        def agentUid = URLDecoder.decode(params.uid, "UTF-8")
        log.debug "got agent uid : ${agentUid}"
        def agent = agentService.retrieve(session["localAuthority"], agentUid)
        def groups = agentService.getAllGroups(session["localAuthority"])

        if(!agent) {
            flash.message = "Aucun agent trouvé avec l'UID ${params.id}"
            redirect(action:list)
            return false
        }
        else {
            render(view:'createOrUpdate', 
                model:[ 'agent':agent, 'mode':'update', 'groups':groups ])
        }
    }

    def save = {
        def agent = new Agent()
        agent.uid = params.uid
        agent.firstName = params.firstName
        agent.lastName = params.lastName
        agent.email = params.email
        agent.telephoneNumber = params.telephoneNumber
        // WTF ??!
        if (params.groups && params.groups.class.name == 'java.lang.String') {
            agent.groups = new String[1];
            agent.groups[0] = params.groups
        } else {
            agent.groups = params.groups
        }
        agent.password = params.password
        
        def groups = agentService.getAllGroups(session["localAuthority"])
		
        def resultMessage
        def provisioningResultsMessageMap
        try {
	        if (params.mode == 'create') {
    	        def resultsMap = provisioningService.createAgent(session["localAuthority"], agent)
    	        agent.uid = resultsMap["agentUid"]
    	        provisioningResultsMessageMap = resultsMap["services"]
	            resultMessage = "L'agent a été créé avec l'identifiant <b>" + agent.uid + "</b>"
	        } else {
	            def resultsMap = provisioningService.updateAgent(session["localAuthority"], agent)
    	        provisioningResultsMessageMap = resultsMap["services"]
	            resultMessage = "L'agent " + agent.uid + " a bien été modifié"
	        }

            render(view:'createOrUpdate', model:[ 'agent':agent, 'mode':'update', 'groups':groups,
                                                  'resultMessage':resultMessage,
                                                  'provisioningResultsMessageMap':provisioningResultsMessageMap])

        } catch (Exception e) {
            e.printStackTrace()
            resultMessage = "Une erreur s'est produite lors de la sauvegarde des données"
			render(view:'createOrUpdate', model:[ 'agent':agent, 'mode': params.mode, 'groups':groups,
			                                      'resultMessage':resultMessage ])
        }
    }
}