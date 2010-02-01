class MainController extends BaseController {
    
    def index = { render(view:'welcome') }

    def switchLocalAuthority = {
		session.localAuthority = params.localAuthority
		render(view:'welcome')
    }
}