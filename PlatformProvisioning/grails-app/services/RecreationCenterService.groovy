import fr.cg95.admin.service.LdapService

class RecreationCenterService{
	boolean transactional = false
	
	LdapService ldapService
	
	def create(localAuthorityName,recCenter){
		return ldapService.createSchoolOrRecreationCenter(localAuthorityName,recCenter)
	}
	
	def delete(localAuthorityName,o){
		return ldapService.deleteFoundation(localAuthorityName,o,"")
	}
	
	def update(localAuthorityName,recCenter){
		return ldapService.updateSchoolOrRecreationCenter(localAuthorityName,recCenter)
	}
	
	def getDetails(localAuthorityName,o){
		return ldapService.getRecreationCenterDetails(localAuthorityName,o)
	}
	
	def search(localAuthorityName,o){
		return ldapService.searchFoundation(localAuthorityName,o,"")
	}

}