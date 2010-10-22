import org.codehaus.groovy.grails.commons.ConfigurationHolder as CH

class ProvisioningService {
	
	boolean transactional = false
	
	AgentService agentService
	SchoolService schoolService
	RecreationCenterService recreationCenterService

	def cvqURL = "http://${CH.config.cvq.service.name}:${CH.config.cvq.service.port}"
	def provisioningPassword = CH.config.cvq.service.password

	def createAgent(localAuthority, agent) {
	    def agentUid = agentService.create(localAuthority, agent)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["agentUid"] = agentUid
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/createAgent",
				body : [
					"login" : agentUid, "firstName" : agent.firstName,
					"lastName" : agent.lastName, "groups" : agent.groups as List
				]
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 201 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}

	def updateAgent(localAuthority, agent) {
	    agentService.update(localAuthority, agent)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/modifyAgent/${agent.uid}",
				body : [
					"login" : agent.uid, "firstName" : agent.firstName,
					"lastName" : agent.lastName, "groups" : agent.groups as List
				]
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 200 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}

	def deleteAgent(localAuthority, agentUid) {
	    agentService.delete(localAuthority, agentUid)
		def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/deleteAgent/${agentUid}"
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 200 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
	
	def createSchool(localAuthority,school){
		schoolService.create(localAuthority,school)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/createSchool",
				body : ["name" : school.name, "address" : school.address]
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 201 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
	
	def updateSchool(localAuthority,school){
	 	schoolService.update(localAuthority,school)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/modifySchool/${school.name}",
				body : ["name" : school.name, "address" : school.address]
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 200 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
	
	def deleteSchool(localAuthority,o){
		schoolService.delete(localAuthority,o)
		def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/deleteSchool/${o}"
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 200 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
	
	def createRecreationCenter(localAuthority,recCenter){
		recreationCenterService.create(localAuthority,recCenter)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/createRecreationCenter",
				body : ["name" : recCenter.name, "address" : recCenter.address]
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 201 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
	
	def updateRecreationCenter(localAuthority,recCenter){
		recreationCenterService.update(localAuthority,recCenter)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/modifyRecreationCenter/${recCenter.name}",
				body : ["name" : recCenter.name, "address" : recCenter.address]
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 200 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
	
	def deleteRecreationCenter(localAuthority,o){
		recreationCenterService.delete(localAuthority,o)
		def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		withHttp(uri: cvqURL) {
			auth.basic "provisioning", provisioningPassword
			post(
				path : "/${CH.config.cvq.service.context_path}/${localAuthority}/deleteRecreationCenter/${o}"
			) { resp ->
				provisioningResultsMap["services"]["CAP-Demat"] = resp.status == 200 ? "OK" : "KO"
			}
		}
		return provisioningResultsMap
	}
}