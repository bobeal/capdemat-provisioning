class UserInfoTagLib {
    
    def displayUserInfo = { attrs ->
        if (session.isSuperAdmin) {
            out << "<form id=\"localAuthFormChooser\" action=\"" << grailsAttributes.getApplicationUri(request) << "/main/switchLocalAuthority" << "\""
            out << " method=\"post\" >"
        }
	
        out << "<div class=\"userInfo\">"
    	out << session.user.uid
    	out << " / "
    	
    	if (session.isSuperAdmin) {
            out << "<select name=\"localAuthority\" onchange=\"javascript:document.getElementById('localAuthFormChooser').submit();\">"
            
            for (localAuth in session.localAuthorities) {
                out << "<option value=\"${localAuth.key}\""
                if (session.localAuthority == localAuth.key)
                     out << " selected=\"selected\""
                out << ">${localAuth.value}</option>"
            }
            
            out << "</select>"
    	} else {
            out << session.localAuthority
    	}
    	
    	out << " - <a href=\""
    	out << grailsAttributes.getApplicationUri(request) << "/agent/logout"
    	out << "\">Déconnexion</a>"
    	out << "</div>"

    	if (session.isSuperAdmin) {
            out << "</form>"
    	}
    }
}
