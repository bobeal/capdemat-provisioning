import fr.cg95.admin.service.LdapService

class SchoolService{

	boolean transactional = false
	
	LdapService ldapService
	
	def create(localAuthorityName,school){
		return ldapService.createSchoolOrRecreationCenter(localAuthorityName,school)
	}
	
	def delete(localAuthorityName,o){
		 def schoolBranch = "schoolBranch"
		return ldapService.deleteFoundation(localAuthorityName,o,schoolBranch)
	}
	
	def update(localAuthorityName,entry){
		return ldapService.updateSchoolOrRecreationCenter(localAuthorityName,entry)
	}
	
	def getDetails(localAuthorityName,o){
		return ldapService.getSchoolDetails(localAuthorityName,o)
	}
	
	def search(localAuthorityName,o){
		def schoolBranch = "schoolBranch"
		return ldapService.searchFoundation(localAuthorityName,o,schoolBranch)
	}
}