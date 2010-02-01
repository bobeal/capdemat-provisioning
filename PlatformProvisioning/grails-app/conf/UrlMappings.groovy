class UrlMappings {
    static mappings = {
      "/agent/edit/$uid" (controller:"agent", action:"edit"){

      }
      "/$controller/$action?/$id?"{
	      constraints {
			 // apply constraints here
              }
       }
       "500"(view:'/error')
    }
}
