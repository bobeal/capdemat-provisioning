import fr.cg95.admin.business.RecreationCenter;
import java.net.URLDecoder;
import org.apache.commons.lang.StringUtils;

class RecreationCenterController extends BaseController{
	RecreationCenterService recreationCenterService
	ProvisioningService provisioningService
	
    def index = { render(view:'search') }
    
    def search = {

		def name = params.name
	    return [ recCenterList: recreationCenterService.search(session["localAuthority"],name)]
    }
    
    // called asynchronously, only renders operation result that will be displayed
    // in a special div
    def delete = {
        if (params.id) {//the recreation center's name
        	def recCenterName = StringUtils.capitalize(URLDecoder.decode(params.id, "UTF-8"))
    
			try {
    	        provisioningService.deleteRecreationCenter(session["localAuthority"], recCenterName)
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
        def recCenterName = URLDecoder.decode(params.id, "UTF-8")
        def recCenter = recreationCenterService.getDetails( session["localAuthority"], recCenterName )
	
        if(!recCenter) {
        	flash.message = "Aucun centre de loisirs trouvé avec le nom ${params.id}"
        }
        else {
            render(view:'createOrUpdate', 
                    model:[ 'recCenter':recCenter, 'mode':'update'])
        }
    }
    
    def save = {
        def recCenter = new RecreationCenter()
        recCenter.name = StringUtils.capitalize(URLDecoder.decode(params.name,"UTF-8"))
        recCenter.address = URLDecoder.decode(params.address,"UTF-8")
        recCenter.telephoneNumber = params.telephoneNumber
        recCenter.email=params.email
     
        def resultMessage
        def provisioningResultsMessageMap
        try {
	        if (params.mode == 'create') {
	            def resultsMap = provisioningService.createRecreationCenter(session["localAuthority"], recCenter)
	            resultMessage = "Le centre de loisirs <b>${recCenter.name}</b> a bien été créé"
    	        provisioningResultsMessageMap = resultsMap["services"]
	        } else {
	            def resultsMap = provisioningService.updateRecreationCenter(session["localAuthority"], recCenter)
	            resultMessage = "Le centre de loisirs <b>" + recCenter.name + "</b> a bien été modifié"
    	        provisioningResultsMessageMap = resultsMap["services"]
	        }

            render(view:'createOrUpdate', model:[ 'recCenter':recCenter, 'mode':'update',
                                                  'resultMessage':resultMessage,
                                                  'provisioningResultsMessageMap':provisioningResultsMessageMap ])

        } catch (Exception e) {
            e.printStackTrace()
            resultMessage = "Une erreur s'est produite lors de la sauvegarde des données"
			render(view:'createOrUpdate', model:[ 'recCenter':recCenter, 'mode': params.mode,
			                                      'resultMessage':resultMessage ])
        }
    }
}