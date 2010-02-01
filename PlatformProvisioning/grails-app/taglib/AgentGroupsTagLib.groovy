class AgentGroupsTagLib {
    
    def groupsMultipleSelect = { attrs ->
        out << "<select name=\"${attrs['name']}\" multiple>"
        
        for (refGroup in attrs['reference']) {
			out << "<option value=\"${refGroup.key}\""
			for (agentGroup in attrs['agentGroups'])
			    if (agentGroup == refGroup.key)
			        out << " selected=\"selected\""
			out << ">${refGroup.value}</option>"           
        }
        
        out << "</select>"
    }
}