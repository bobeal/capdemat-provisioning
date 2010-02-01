abstract class BaseController {
	def beforeInterceptor = [action:this.&auth,except:['login','handleLogin']]
	
	def auth() {	
			if(!session.user) {
				redirect(controller:'agent',action:'login')
				return false
			}
	}
}

