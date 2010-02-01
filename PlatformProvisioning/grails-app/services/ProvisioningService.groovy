import fr.cg95.cvq.exporter.service.bo.IProvisioningService

class ProvisioningService {
	
	boolean transactional = false
	
	AgentService agentService
	SchoolService schoolService
	RecreationCenterService recreationCenterService
	IProvisioningService cvqProvisioningService
	
	def createAgent(localAuthority, agent) {
	    def agentUid = agentService.create(localAuthority, agent)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["agentUid"] = agentUid
		provisioningResultsMap["services"] = [:]
	 	
	    try {
	        cvqProvisioningService.createAgent(localAuthority, agentUid, agent.firstName,
	                agent.lastName, agent.groups)
	        provisioningResultsMap["services"]["CAP-Demat"] = "OK"
	    } catch (Exception e) {
	        println "Error while creating agent"
	        e.printStackTrace()
	        provisioningResultsMap["services"]["CAP-Demat"] = "KO"
	    }
	    
		return provisioningResultsMap
	}

	def updateAgent(localAuthority, agent) {
	    agentService.update(localAuthority, agent)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
	 	
	    try {
	        cvqProvisioningService.modifyAgent(localAuthority, agent.uid, agent.uid, 
	                agent.firstName, agent.lastName, agent.groups)
	        provisioningResultsMap["services"]["CAP-Demat"] = "OK"
	    } catch (Exception e) {
	        println "Error while updating agent"
	        e.printStackTrace()
	        provisioningResultsMap["services"]["CAP-Demat"] = "KO"
	    }
	    
		return provisioningResultsMap
	}

	def deleteAgent(localAuthority, agentUid) {
	    agentService.delete(localAuthority, agentUid)
	    
	    try {
	        cvqProvisioningService.deleteAgent(localAuthority, agentUid)
	    } catch (Exception e) {
	        println "Error while deleting agent"
	        e.printStackTrace()
	    }	    
	}
	
	def createSchool(localAuthority,school){
		schoolService.create(localAuthority,school)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		
		try {
	        cvqProvisioningService.createSchool(localAuthority, school.name, school.address)
	        provisioningResultsMap["services"]["CAP-Demat"] = "OK"
	    } catch (Exception e) {
	        println "Error while creating school in CVQ"
	        e.printStackTrace()
	        provisioningResultsMap["services"]["CAP-Demat"] = "KO"
	    }
	    
		return provisioningResultsMap
	}
	
	def updateSchool(localAuthority,school){
	 	schoolService.update(localAuthority,school)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
	 	
	 	try {
	        cvqProvisioningService.modifySchool(localAuthority, school.name, school.name, school.address)
	        provisioningResultsMap["services"]["CAP-Demat"] = "OK"
	    } catch (Exception e) {
	        println "Error while updating school in CVQ"
	        e.printStackTrace()
	        provisioningResultsMap["services"]["CAP-Demat"] = "KO"
	    }
	    
		return provisioningResultsMap
	}
	
	def deleteSchool(localAuthority,o){
		schoolService.delete(localAuthority,o)
		
		try {
	        cvqProvisioningService.deleteSchool(localAuthority, o)
	    } catch (Exception e) {
	        println "Error while deleting school in CVQ"
	        e.printStackTrace()
	    }	 
	}
	
	def createRecreationCenter(localAuthority,recCenter){
		recreationCenterService.create(localAuthority,recCenter)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		
		try {
	        cvqProvisioningService.createRecreationCenter(localAuthority, recCenter.name, recCenter.address)
	        provisioningResultsMap["services"]["CAP-Demat"] = "OK"
	    } catch (Exception e) {
	        println "Error while creating recreation center in CVQ"
	        e.printStackTrace()
	        provisioningResultsMap["services"]["CAP-Demat"] = "KO"
	    }
	    
		return provisioningResultsMap
	}
	
	def updateRecreationCenter(localAuthority,recCenter){
		recreationCenterService.update(localAuthority,recCenter)
	 	def provisioningResultsMap = [:]
		provisioningResultsMap["services"] = [:]
		
		try {
	        cvqProvisioningService.modifyRecreationCenter(localAuthority, recCenter.name, recCenter.name, recCenter.address)
	        provisioningResultsMap["services"]["CAP-Demat"] = "OK"
	    } catch (Exception e) {
	        println "Error while updating recreation center in CVQ"
	        e.printStackTrace()
	        provisioningResultsMap["services"]["CAP-Demat"] = "KO"
	    }
	    
		return provisioningResultsMap
	}
	
	def deleteRecreationCenter(localAuthority,o){
		recreationCenterService.delete(localAuthority,o)
		
		try {
	        cvqProvisioningService.deleteRecreationCenter(localAuthority, o)
	    } catch (Exception e) {
	        println "Error while deleting recreation center in CVQ"
	        e.printStackTrace()
	    }	
	}
}