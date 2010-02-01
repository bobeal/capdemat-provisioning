import fr.cg95.admin.service.LdapService

class AgentService {
	
	boolean transactional = false
	
	LdapService ldapService
	
	def getAllLocalAuthorities() {
	    return ldapService.getAllLocalAuthorities()
	}
	
	def getAllGroups(localAuthority) {
	    return ldapService.getAllGroups(localAuthority)
	}
	
	def authenticate(login, password, localAuthority, superAdmin) {
	    return ldapService.authenticateAgent(login, password, localAuthority, superAdmin)
	}
	
	def create(localAuthority, agent) {
	    return ldapService.createAgent(localAuthority, agent)
	}

	def retrieve(localAuthority, uid) {
	    return ldapService.getAgentDetails(localAuthority, uid)
	}

	def search(localAuthority, lastName, firstName, uid) {
	    return ldapService.searchAgents(localAuthority, lastName, firstName, uid)
	}
	
	def update(localAuthority, agent) {
	    return ldapService.updateAgent(localAuthority, agent)
	}
	
	def delete(localAuthority, agent) {
	    return ldapService.deleteAgent(localAuthority, agent)
	}
}

