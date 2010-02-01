import fr.cg95.admin.business.School ;
import java.net.URLDecoder;
import org.apache.commons.lang.StringUtils;

class SchoolController extends BaseController{

	SchoolService schoolService
	ProvisioningService provisioningService
	
    def index = { render(view:'search') }
    
    def search = {

		def name = params.name
	    return [ schoolList: schoolService.search(session["localAuthority"],name)]
    }
    
    // called asynchronously, only renders operation result that will be displayed
    // in a special div
    def delete = {
        if (params.id) {//the school's name
            def schoolName = StringUtils.capitalize(URLDecoder.decode(params.id, "UTF-8"))
            
			try {
    	        provisioningService.deleteSchool(session["localAuthority"], schoolName)
        	} catch (Exception e) {
            	renderError(params.id)
        	}
        } else {
            renderError(params.id)
        }
    }
    
    def create = {
		render(view:'createOrUpdate', model:['mode':'create'])
    }

    def edit = {
        def schoolName = URLDecoder.decode(params.id, "UTF-8")
        def school = schoolService.getDetails( session["localAuthority"], schoolName )
	
        if(!school) {
        	flash.message = "Aucune école trouvée avec le nom ${params.id}"
        }
        else {
            render(view:'createOrUpdate', 
                    model:[ 'school':school, 'mode':'update'])
        }
    }
    
    def save = {
        def school = new School()
        school.name = StringUtils.capitalize(URLDecoder.decode(params.name,"UTF-8"))
        school.address = URLDecoder.decode(params.address,"UTF-8")
        school.telephoneNumber = params.telephoneNumber
        school.email=params.email

     
        def resultMessage
        def provisioningResultsMessageMap
        try {
	        if (params.mode == 'create') {
	            def resultsMap = provisioningService.createSchool(session["localAuthority"], school)
	            resultMessage = "L'école <b>${school.name}</b> a bien été créée </b>"
    	        provisioningResultsMessageMap = resultsMap["services"]
	        } else {
	            def resultsMap = provisioningService.updateSchool(session["localAuthority"], school)
	            resultMessage = "L'école <b>" + school.name + "</b> a bien été modifiée"
    	        provisioningResultsMessageMap = resultsMap["services"]
	        }

            render(view:'createOrUpdate', model:[ 'school':school, 'mode':'update',
                                                  'resultMessage':resultMessage,
                                                  'provisioningResultsMessageMap':provisioningResultsMessageMap ])

        } catch (Exception e) {
            e.printStackTrace()
            resultMessage = "Une erreur s'est produite lors de la sauvegarde des données"
			render(view:'createOrUpdate', model:[ 'school':school, 'mode': params.mode,
			                                      'resultMessage':resultMessage ])
        }
    }
}