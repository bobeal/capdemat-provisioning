package fr.cg95.admin.service;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.perl.Perl5Util;

import sun.misc.BASE64Encoder;
import fr.cg95.admin.business.Agent;
import fr.cg95.admin.business.Foundation;
import fr.cg95.admin.business.RecreationCenter;
import fr.cg95.admin.business.School;

public class LdapService {

    private String host;
    private int port;
    private String securityPrincipal;
    private String securityCredentials;
    private String securityAuthentication = "simple";
    private String ditRoot;
    private String peopleBranch;
    private String groupBranch;
    private String schoolBranch;
    private String recreationCenterBranch;
    private String administratorGroup;
    private String providerUrl;
    private Map cachedGroupsMap = Collections.synchronizedMap(new HashMap());
    private static final String ADD_TO = "addTo";
    private static final String REMOVE_FROM = "removeFrom";
    private static HashMap uidNormalizationMap = new HashMap();

    static {
        uidNormalizationMap.put(" ", "");
        uidNormalizationMap.put("'", "");
        uidNormalizationMap.put("à", "a");
        uidNormalizationMap.put("â", "a");
        uidNormalizationMap.put("ç", "c");
        uidNormalizationMap.put("é", "e");
        uidNormalizationMap.put("è", "e");
        uidNormalizationMap.put("ê", "e");
        uidNormalizationMap.put("î", "i");
        uidNormalizationMap.put("ô", "o");
        uidNormalizationMap.put("ù", "u");
        uidNormalizationMap.put("û", "u");
    }
    private static Perl5Util perlUtil = new Perl5Util();
    private static Logger logger = Logger.getLogger(LdapService.class);

    /**
     * Initialize connection with LDAP directory and feed some application caches.
     */
    public void init() throws Exception {

        logger.debug("init() Initializing LDAP context to host " + host + " and port " + port);

        // calculate LDAP context
        providerUrl = new StringBuffer("ldap://").append(host).append(":").append(port).append("/").toString();

        // create the groups cache
        Map localAuthMap = getAllLocalAuthorities();
        Iterator localAuthMapIt = localAuthMap.keySet().iterator();
        while (localAuthMapIt.hasNext()) {
            String currLocalAuth = (String) localAuthMapIt.next();
            logger.debug("init() feeding groups cache for " + currLocalAuth);
            Map localAuthGroups = getAllGroups(currLocalAuth);
            cachedGroupsMap.put(currLocalAuth, localAuthGroups);
        }
    }

    private DirContext gimmeAContext() throws Exception {

        DirContext ctx = null;
        try {
            InitialContext iCtx = new InitialContext();
            ctx = (DirContext) iCtx.lookup(providerUrl.toString());
            ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, securityPrincipal);
            ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, securityCredentials);
            ctx.addToEnvironment(Context.SECURITY_AUTHENTICATION, securityAuthentication);
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Unable to initialize LDAP context");
        }

        return ctx;
    }

    private void closeContext(DirContext ctx) {
        try {
            if (ctx != null) {
                ctx.close();
            }
        } catch (NamingException ne) {
            logger.error("closeContext() Error while closing context");
            ne.printStackTrace();
        }
    }

    private void closeNamingEnumeration(NamingEnumeration namingEnumeration) {
        try {
            if (namingEnumeration != null) {
                namingEnumeration.close();
            }
        } catch (NamingException ne) {
            logger.error("closeNamingEnumeration() Error while closing naming enumeration");
            ne.printStackTrace();
        }
    }

    public Agent authenticateAgent(final String login, final String password,
        final String localAuthorityName, final boolean superAdmin)
        throws Exception {

        logger.debug("authenticateAgent() authenticating " + login + " on local authority " + localAuthorityName);

        DirContext dirContext = gimmeAContext();
        NamingEnumeration agentsEnumeration = null;
        try {

            SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);
            StringBuffer ldapQueryBuffer = new StringBuffer();
            StringBuffer filterBuffer = new StringBuffer();
            if (superAdmin) {
                ldapQueryBuffer.append("(&(objectClass=organizationalRole)(cn=").append(login).append("))");
                filterBuffer.append(ditRoot);
            } else {
                ldapQueryBuffer.append("(&(objectClass=inetOrgPerson)(uid=").append(login).append("))");
                filterBuffer.append(getDn(null, null, null, localAuthorityName, ditRoot));
            }

            agentsEnumeration =
                dirContext.search(filterBuffer.toString(), ldapQueryBuffer.toString(), controls);

            if (!agentsEnumeration.hasMore()) {
                return null;
            }

            SearchResult result = (SearchResult) agentsEnumeration.next();
            if (superAdmin) {
                if (password.equals(securityCredentials)) {
                    Agent agent = new Agent();
                    agent.setUid(login);
                    return agent;
                } else {
                    return null;
                }
            } else {
                byte[] pwdBytes = (byte[]) result.getAttributes().get("userPassword").get();
                String ldapPassword = new String(pwdBytes);
                logger.debug("authenticateAgent() retrieved raw LDAP password : " + ldapPassword);
                String purgedLdapPassword = ldapPassword.substring(5);
                logger.debug("authenticateAgent() retrieved LDAP password : " + purgedLdapPassword);
                String encodedUserPassword = encryptPassword(password);
                logger.debug("authenticateAgent() comparing with : " + encodedUserPassword);

                if (!purgedLdapPassword.equals(encodedUserPassword)) {
                    return null;
                } else {
                    Agent agent = fillAgentFromAttributes(localAuthorityName, result.getAttributes());
                    String[] agentGroups = agent.getGroups();
                    for (int i = 0; i < agentGroups.length; i++) {
                        if (administratorGroup.equals(agentGroups[i])) {
                            return agent;
                        }
                    }
                    logger.debug("authenticateAgent() agent does not belong to authorized group");
                    return null;
                }
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while authenticating agent");
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(agentsEnumeration);
        }
    }

    public Map getAllLocalAuthorities() throws Exception {

        Map resultMap = new HashMap();

        DirContext dirContext = gimmeAContext();
        NamingEnumeration localAuthoritiesEnumeration = null;
        try {

            SearchControls controls = getSearchControls(SearchControls.ONELEVEL_SCOPE);
            localAuthoritiesEnumeration = dirContext.search(ditRoot, "(objectClass=organization)", controls);

            while (localAuthoritiesEnumeration.hasMore()) {
                SearchResult result = (SearchResult) localAuthoritiesEnumeration.next();
                Attributes attrs = result.getAttributes();
                resultMap.put((String) attrs.get("dc").get(),
                    (String) attrs.get("o").get());
                logger.info("getAllLocalAuthorities() Got object name : " + result.getName());
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving local authorities");
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(localAuthoritiesEnumeration);
        }

        return resultMap;
    }

    public List searchAgents(final String localAuthorityName, final String lastName,
        final String firstName, final String uid) throws Exception {

        ArrayList resultsList = new ArrayList();

        DirContext dirContext = gimmeAContext();
        NamingEnumeration agentsEnumeration = null;
        try {

            SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);
            StringBuffer ldapQueryBuffer = new StringBuffer().append("(&(objectClass=inetOrgPerson)");
            if (uid != null && !uid.equals("")) {
                ldapQueryBuffer.append("(uid=").append(uid).append("*)");
            }
            if (firstName != null && !firstName.equals("")) {
                ldapQueryBuffer.append("(givenName=").append(firstName).append("*)");
            }
            if (lastName != null && !lastName.equals("")) {
                ldapQueryBuffer.append("(sn=").append(lastName).append("*)");
            }
            ldapQueryBuffer.append(")");
            String filter = getDn(null, null, peopleBranch, localAuthorityName, ditRoot);
            agentsEnumeration = dirContext.search(filter, ldapQueryBuffer.toString(), controls);

            while (agentsEnumeration.hasMore()) {
                SearchResult result = (SearchResult) agentsEnumeration.next();
                resultsList.add(fillAgentFromAttributes(localAuthorityName, result.getAttributes()));
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving agents");
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(agentsEnumeration);
        }

        return resultsList;
    }

    private SearchControls getSearchControls(int level) {
        SearchControls controls = new SearchControls();
        controls.setSearchScope(level);
        return controls;
    }

    private Agent fillAgentFromAttributes(final String localAuthorityName, final Attributes attrs)
        throws NamingException, Exception {

        Agent agent = new Agent();

        String uid = (String) attrs.get("uid").get();
        agent.setUid(uid);
        if (attrs.get("givenName") != null) {
            agent.setFirstName((String) attrs.get("givenName").get());
        }
        if (attrs.get("sn") != null) {
            agent.setLastName((String) attrs.get("sn").get());
        }
        if (attrs.get("mail") != null) {
            agent.setEmail((String) attrs.get("mail").get());
        }
        if (attrs.get("telephoneNumber") != null) {
            agent.setTelephoneNumber((String) attrs.get("telephoneNumber").get());
        }
        agent.setGroups(getAgentGroups(localAuthorityName, uid));

        return agent;
    }

    public Agent getAgentDetails(final String localAuthorityName, final String agentUid)
        throws Exception {

        DirContext dirContext = gimmeAContext();
        try {
            String dn = getDn(null, agentUid, peopleBranch, localAuthorityName, ditRoot);
            logger.error("getAgentDetails() looking agent in " + dn);
            Attributes attrs = dirContext.getAttributes(dn);
            if (attrs != null) {
                return fillAgentFromAttributes(localAuthorityName, attrs);
            } else {
                logger.warn("getAgentDetails() Agent " + agentUid + " not found");
                return null;
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving agent data : " + ne.getMessage());
        } finally {
            closeContext(dirContext);
        }
    }

    public String[] getAgentGroups(String localAuthorityName, String agentUid)
        throws NamingException, Exception {

        logger.debug("getAgentGroups() retrieving groups for agent " + agentUid + " on local authority " + localAuthorityName);

        Set resultGroups = new HashSet();

        DirContext dirContext = gimmeAContext();
        NamingEnumeration namingEnumeration = null;
        try {

            SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);
            String ldapQuery = "(&(objectClass=posixGroup)(memberUid=" + agentUid + "))";
            String ldapFilter = getDn(null, null, groupBranch, localAuthorityName, ditRoot);
            namingEnumeration = dirContext.search(ldapFilter, ldapQuery, controls);
            while (namingEnumeration.hasMore()) {
                SearchResult result = (SearchResult) namingEnumeration.next();
                String groupName = (String) result.getAttributes().get("cn").get();
                logger.debug("getAgentGroups() adding group " + groupName);
                resultGroups.add(groupName);
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw ne;
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(namingEnumeration);
        }

        return (String[]) resultGroups.toArray(new String[resultGroups.size()]);
    }

    public Map getAllGroups(String localAuthorityName)
        throws Exception {

        synchronized (cachedGroupsMap) {
            Map localAuthGroups = (Map) cachedGroupsMap.get(localAuthorityName);
            if (localAuthGroups != null) {
                logger.debug("getAllGroups() returning cached groups list");
                return localAuthGroups;
            }
        }
        Map resultGroups = new HashMap();
        DirContext dirContext = gimmeAContext();
        NamingEnumeration namingEnumeration = null;
        try {
            SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);
            String ldapQuery = "(objectClass=posixGroup)";
            String ldapFilter = getDn(null, null, groupBranch, localAuthorityName, ditRoot);
            namingEnumeration = dirContext.search(ldapFilter, ldapQuery, controls);
            while (namingEnumeration.hasMore()) {
                SearchResult result = (SearchResult) namingEnumeration.next();
                Attributes attrs = result.getAttributes();
                String groupName = (String) attrs.get("cn").get();
                if (attrs.get("description") != null) {
                    resultGroups.put(groupName,
                        (String) attrs.get("description").get());
                } else {
                    resultGroups.put(groupName, groupName);
                }
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving groups list");
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(namingEnumeration);
        }

        synchronized (cachedGroupsMap) {
            cachedGroupsMap.put(localAuthorityName, resultGroups);
        }

        return resultGroups;
    }

    private String gimmeAnUid(final String localAuthorityName, final String firstName,
        final String lastName)
        throws Exception {

        String transFirstName = firstName.trim().toLowerCase();
        String transLastName = lastName.trim().toLowerCase();
        Iterator uidNormalizationMapIt = uidNormalizationMap.keySet().iterator();
        while (uidNormalizationMapIt.hasNext()) {
            String oldChar = (String) uidNormalizationMapIt.next();
            String newChar = (String) uidNormalizationMap.get(oldChar);
            String pattern = new StringBuffer("s/").append(oldChar).append("/").append(newChar).append("/g").toString();
            transFirstName = perlUtil.substitute(pattern, transFirstName);
            transLastName = perlUtil.substitute(pattern, transLastName);
        }

        String baseUid = transFirstName + "." + transLastName;
        logger.debug("gimmeAnUid() Normalized base UID : " + baseUid);
        String generatedUid = null;

        DirContext dirContext = gimmeAContext();
        NamingEnumeration namingEnumeration = null;
        try {

            SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);
            String ldapQuery = "(&(objectClass=inetOrgPerson)(uid=" + baseUid + "*))";
            String baseBranch = getDn(null, null, peopleBranch, localAuthorityName, ditRoot);
            namingEnumeration = dirContext.search(baseBranch, ldapQuery, controls);
            if (!namingEnumeration.hasMore()) {
                return baseUid;
            } else {
                Set foundUids = new HashSet();
                while (namingEnumeration.hasMore()) {
                    SearchResult result = (SearchResult) namingEnumeration.next();
                    foundUids.add((String) result.getAttributes().get("uid").get());
                }
                if (!foundUids.contains(baseUid)) {
                    return baseUid;
                } else {
                    int i = 2;
                    generatedUid = baseUid + String.valueOf(i);
                    while (foundUids.contains(generatedUid)) {
                        i++;
                        generatedUid = baseUid + String.valueOf(i);
                    }

                    return generatedUid;
                }
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving agent data");
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(namingEnumeration);
        }
    }

    public String createAgent(String localAuthorityName, Agent agent)
        throws Exception {

        String shaEncodedPassword = null;
        try {
            shaEncodedPassword = encryptPassword(agent.getPassword());
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("No SHA-1 algorithm available !");
        }

        Attributes attributes = new BasicAttributes();
        Attribute classAttr = new BasicAttribute("objectClass");
        classAttr.add("top");

        classAttr.add("person");
        classAttr.add("inetOrgPerson");
        attributes.put(classAttr);

        synchronized (this) {

            String generatedUid =
                gimmeAnUid(localAuthorityName, agent.getFirstName(), agent.getLastName());

            attributes.put("uid", generatedUid);
            attributes.put("cn", StringUtils.capitalize(agent.getFirstName()) + " " + StringUtils.capitalize(agent.getLastName()));
            attributes.put("givenName", StringUtils.capitalize(agent.getFirstName()));
            attributes.put("sn", StringUtils.capitalize(agent.getLastName()));
            if (agent.getTelephoneNumber() != null && !agent.getTelephoneNumber().equals("")) {
                attributes.put("telephoneNumber", agent.getTelephoneNumber());
            }
            if (agent.getEmail() != null && !agent.getEmail().equals("")) {
                attributes.put("mail", agent.getEmail());
            }
            attributes.put("userPassword", "{sha}" + shaEncodedPassword);

            DirContext dirContext = gimmeAContext();

            try {
                dirContext.createSubcontext(getDn(null, generatedUid, peopleBranch, localAuthorityName, ditRoot), attributes);
            } catch (NamingException ne) {
                throw new Exception("Error while creation agent " + ne.getMessage());
            } finally {
                closeContext(dirContext);
            }

            agent.setUid(generatedUid);
        }

        updateGroupsMembership(localAuthorityName, agent.getUid(), agent.getGroups(), ADD_TO);

        return agent.getUid();
    }

    public void updateAgent(final String localAuthorityName, final Agent agent)
        throws Exception {

        // first, update agent groups
        List newGroups = null;
        if (agent.getGroups() != null) {
            List backedNewGroups = Arrays.asList(agent.getGroups());
            newGroups = new ArrayList(backedNewGroups);
        } else {
            newGroups = new ArrayList();
        }
        List oldGroups = Arrays.asList(getAgentGroups(localAuthorityName, agent.getUid()));
        List groupsToRemoveFrom = new ArrayList();
        for (int i = 0; i < oldGroups.size(); i++) {
            String oldGroup = (String) oldGroups.get(i);
            if (!newGroups.remove(oldGroup)) {
                groupsToRemoveFrom.add(oldGroup);
            }
        }
        updateGroupsMembership(localAuthorityName, agent.getUid(),
            (String[]) newGroups.toArray(new String[newGroups.size()]), ADD_TO);
        updateGroupsMembership(localAuthorityName, agent.getUid(),
            (String[]) groupsToRemoveFrom.toArray(new String[groupsToRemoveFrom.size()]),
            REMOVE_FROM);

        // then, update agent information
        String dn = getDn(null, agent.getUid(), peopleBranch, localAuthorityName, ditRoot);
        DirContext dirContext = gimmeAContext();
        Attributes attrs = dirContext.getAttributes(dn);
        List modificationItems = new ArrayList();
        try {
            prepareAgentUpdate(agent, attrs, modificationItems);
            ModificationItem[] modifsItems = new ModificationItem[modificationItems.size()];
            for (int i = 0; i < modificationItems.size(); i++) {
                modifsItems[i] = (ModificationItem) modificationItems.get(i);
            }
            dirContext.modifyAttributes(dn, modifsItems);

        } catch (NamingException ne) {
            throw new Exception("Error while retrieving group information", ne);
        } finally {
            closeContext(dirContext);
        }

    }

    private void prepareAgentUpdate(final Agent agent, Attributes attrs, List modificationItems)
        throws NamingException, NoSuchAlgorithmException, UnsupportedEncodingException {

        Attribute lastNameAttribute = attrs.get("sn");
        setNewAttributeValue(agent.getLastName(), modificationItems, lastNameAttribute);

        Attribute firstNameAttribute = attrs.get("givenName");
        setNewAttributeValue(agent.getFirstName(), modificationItems, firstNameAttribute);

        if (agent.getPassword() != null && !agent.getPassword().equals("")) {
            Attribute userPasswordAttribute = attrs.get("userPassword");
            String ldapPassword = new String((byte[]) userPasswordAttribute.get());
            String purgedLdapPassword = ldapPassword.substring(5);
            logger.debug("updateAgent() current userPassword : " + purgedLdapPassword);

            String encodedUserPassword = encryptPassword(agent.getPassword());
            logger.debug("updateAgent() new userPassword : " + encodedUserPassword);
            if (!purgedLdapPassword.equals(encodedUserPassword)) {
                userPasswordAttribute.set(0, "{sha}" + encodedUserPassword);
                modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, userPasswordAttribute));
                logger.debug("updateAgent() replacing userPassword with : " + encodedUserPassword);
            }
        }

        if (agent.getEmail() != null && !agent.getEmail().equals("")) {
            Attribute emailAttribute = attrs.get("mail");
            if (emailAttribute == null) {
                emailAttribute = new BasicAttribute("mail", agent.getEmail());
                modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, emailAttribute));
                logger.debug("updateAgent() adding email : " + agent.getEmail());
            } else {
                setNewAttributeValue(agent.getEmail(), modificationItems, emailAttribute);
            }
        } else if (attrs.get("mail") != null) {
            Attribute pwdAttribute = attrs.get("mail");
            setNewAttributeValue(null, modificationItems, pwdAttribute);
        }

        if (agent.getTelephoneNumber() != null && !agent.getTelephoneNumber().equals("")) {
            Attribute telephoneNumberAttribute = attrs.get("telephoneNumber");
            if (telephoneNumberAttribute == null) {
                telephoneNumberAttribute = new BasicAttribute("telephoneNumber", agent.getTelephoneNumber());
                modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, telephoneNumberAttribute));
                logger.debug("updateAgent() adding telephoneNumber : " + agent.getTelephoneNumber());
            } else {
                setNewAttributeValue(agent.getTelephoneNumber(), modificationItems, telephoneNumberAttribute);
            }

        } else if (attrs.get("telephoneNumber") != null) {
            Attribute telAttribute = attrs.get("telephoneNumber");
            setNewAttributeValue(null, modificationItems, telAttribute);
        }
    }

    public void deleteAgent(final String localAuthorityName, final String agentUid)
        throws Exception {

        removeAgentFromGroups(localAuthorityName, agentUid);
        DirContext dirContext = gimmeAContext();
        try {

            dirContext.destroySubcontext(getDn(null, agentUid, peopleBranch, localAuthorityName, ditRoot));
        } catch (Exception e) {
            logger.error("Error while removing agent");
            e.printStackTrace();
            throw e;
        } finally {
            closeContext(dirContext);
        }
    }

    private String getDn(String o, String uid, String branch, String dc, String rootDit) {
        StringBuffer dn = new StringBuffer();

        if (o != null && !"".equals(o)) {
            dn.append("o=").append(o).append(",");
        }
        if (uid != null && !"".equals(uid)) {
            dn.append("uid=").append(uid).append(",");
        }
        if (branch != null && !"".equals(branch)) {
            dn.append(branch).append(",");
        }
        if (dc != null && !"".equals(dc)) {
            dn.append("dc=").append(dc).append(",");
        }
        if (rootDit != null && !"".equals(rootDit)) {
            dn.append(rootDit);
        }
        return dn.toString();
    }

    private String encryptPassword(final String password)
        throws NoSuchAlgorithmException, UnsupportedEncodingException {

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] bytePass = md.digest(password.getBytes("UTF-8"));
        BASE64Encoder b = new BASE64Encoder();

        return b.encode(bytePass);
    }

    private void removeAgentFromGroups(final String localAuthorityName, final String agentUid)
        throws Exception {

        NamingEnumeration namingEnumeration = null;
        DirContext dirContext = gimmeAContext();
        try {

            SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);

            String ldapQuery = "(&(objectClass=posixGroup)(memberUid=" + agentUid + "))";
            String ldapFilter = getDn(null, null, null, localAuthorityName, ditRoot);
            namingEnumeration = dirContext.search(ldapFilter, ldapQuery, controls);
            while (namingEnumeration.hasMore()) {
                SearchResult result = (SearchResult) namingEnumeration.next();
                Attribute attribute = result.getAttributes().get("memberUid");
                attribute.remove(agentUid);
                doLdapModification(dirContext, DirContext.REPLACE_ATTRIBUTE, ldapFilter, result, attribute);
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw ne;
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(namingEnumeration);
        }
    }

    private void updateGroupsMembership(final String localAuthorityName, final String uid,
        final String[] groups, final String operation)
        throws Exception {

        if (groups == null || groups.length == 0) {
            return;
        }
        for (int i = 0; i < groups.length; i++) {
            String groupCn = groups[i];
            try {
                updateGroupMembership(localAuthorityName, groupCn, uid, operation);
            } catch (Exception e) {
                logger.error("User has not been added to / removed from group " + groupCn + " (cause : " + e.getCause().getMessage() + ")");
                throw e;
            }
        }
    }

    private void updateGroupMembership(final String localAuthorityName, final String groupCn,
        final String uid, final String operation)
        throws Exception {

        if (operation.equals(ADD_TO)) {
            logger.debug("addToGroup() adding user " + uid + " to group " + groupCn);
        } else {
            logger.debug("addToGroup() removing user " + uid + " from group " + groupCn);
        }

        NamingEnumeration namingEnumeration = null;
        DirContext dirContext = gimmeAContext();
        try {
            SearchControls searchControl = getSearchControls(SearchControls.SUBTREE_SCOPE);
            String ldapQuery = "(&(objectclass=posixGroup) (cn=" + groupCn + "))";
            String ldapBase = getDn(null, null, null, localAuthorityName, ditRoot);
            namingEnumeration = dirContext.search(ldapBase, ldapQuery, searchControl);
            while (namingEnumeration.hasMore()) {
                SearchResult result = (SearchResult) namingEnumeration.next();
                Attribute attribute = result.getAttributes().get("memberUid");
                if (operation.equals(ADD_TO)) {
                    if (attribute != null) {
                        attribute.add(uid);
                        doLdapModification(dirContext, DirContext.REPLACE_ATTRIBUTE, ldapBase, result, attribute);
                    } else {
                        attribute = new BasicAttribute("memberUid", uid);
                        doLdapModification(dirContext, DirContext.ADD_ATTRIBUTE, ldapBase, result, attribute);
                    }
                } else {
                    attribute.remove(uid);
                    doLdapModification(dirContext, DirContext.REPLACE_ATTRIBUTE, ldapBase, result, attribute);
                }
            }
        } catch (NamingException ne) {
            throw new Exception("Error while retrieving group information", ne);
        } finally {
            closeContext(dirContext);
            closeNamingEnumeration(namingEnumeration);
        }
    }

    private void doLdapModification(DirContext dirContext, int op, String ldapBase,
        SearchResult result, Attribute attribute) throws NamingException {

        ModificationItem mods[] = new ModificationItem[1];
        mods[0] = new ModificationItem(op, attribute);
        dirContext.modifyAttributes(result.getName() + "," + ldapBase, mods);
    }

    private void populateFoundationObject(SearchResult result, Foundation entity, List resultsList)
        throws NamingException {
        Attributes attrs = result.getAttributes();

        if (attrs.get("o") != null) {
            entity.setName((String) attrs.get("o").get());
        }
        if (attrs.get("street") != null) {
            entity.setAddress((String) attrs.get("street").get());
        }
        if (attrs.get("telephoneNumber") != null) {
            entity.setTelephoneNumber((String) attrs.get("telephoneNumber").get());
        }
        if (attrs.get("mail") != null) {
            entity.setEmail((String) attrs.get("mail").get());
        }

        resultsList.add(entity);
    }

    public School getSchoolDetails(String localAuthorityName, String o)
        throws Exception {

        DirContext dirContext = gimmeAContext();
        try {
            String dn = getDn(o, null, schoolBranch, localAuthorityName, ditRoot);

            return (School) executeFoundationQueryDetails(new School(), o, dirContext, dn);
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving school data");
        } finally {
            closeContext(dirContext);
        }
    }

    public RecreationCenter getRecreationCenterDetails(String localAuthorityName,
        String o) throws Exception {

        DirContext dirContext = gimmeAContext();
        try {
            String dn = getDn(o, null, recreationCenterBranch, localAuthorityName, ditRoot);

            return (RecreationCenter) executeFoundationQueryDetails(new RecreationCenter(), o, dirContext, dn);
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving recreation center data");
        } finally {
            closeContext(dirContext);
        }

    }

    private Foundation executeFoundationQueryDetails(Foundation entity, String o,
        DirContext dirContext, String dn) throws NamingException {

        Attributes attrs = dirContext.getAttributes(dn);
        if (attrs != null) {

            if (attrs.get("o") != null) {
                entity.setName((String) attrs.get("o").get());
            }
            if (attrs.get("street") != null) {
                entity.setAddress((String) attrs.get("street").get());
            }
            if (attrs.get("telephoneNumber") != null) {
                entity.setTelephoneNumber((String) attrs.get("telephoneNumber").get());
            }
            if (attrs.get("mail") != null) {
                entity.setEmail((String) attrs.get("mail").get());
            }

            return entity;
        } else {
            logger.warn("getRecreationDetails() recreation center " + o + " not found");
            return null;
        }
    }

    public void createSchoolOrRecreationCenter(String localAuthorityName, Foundation entity)
        throws Exception {

        Attributes attributes = new BasicAttributes();
        Attribute classAttr = new BasicAttribute("objectClass");
        classAttr.add("top");

        classAttr.add("organization");
        classAttr.add("extensibleObject");
        attributes.put(classAttr);
        String dn = null;

        DirContext dirContext = gimmeAContext();

        if (entity != null) {
            prepareQuery(entity, attributes);
            dn = getDnFromEntity(localAuthorityName, entity);

            try {
                dirContext.createSubcontext(dn, attributes);
            } catch (NamingException ne) {
                throw new Exception("Error while creation school or recreation center " + ne.getMessage());
            } finally {
                closeContext(dirContext);
            }
        }

    }

    private String getDnFromEntity(String localAuthorityName, Foundation entity) {
        StringBuffer dn = new StringBuffer("o=");
        dn.append(entity.getName()).append(",").append((entity instanceof School)
            ? schoolBranch
            : recreationCenterBranch).append(",dc=").append(localAuthorityName).append(",").append(ditRoot);
        return dn.toString();
    }

    private void prepareQuery(Foundation entity, Attributes attributes) {
        attributes.put("o", StringUtils.capitalize(entity.getName()));
        if (entity.getTelephoneNumber() != null && !entity.getTelephoneNumber().equals("")) {
            attributes.put("telephoneNumber", entity.getTelephoneNumber());
        }
        if (entity.getEmail() != null && !entity.getEmail().equals("")) {
            attributes.put("mail", entity.getEmail());
        }

        attributes.put("street", entity.getAddress());
    }

    public void deleteFoundation(String localAuthorityName, String o, String branch)
        throws Exception {

        DirContext dirContext = gimmeAContext();
        try {
            String dn = getDn(o, null, "schoolBranch".equals(branch)
                ? schoolBranch
                : recreationCenterBranch, localAuthorityName, ditRoot);
            dirContext.destroySubcontext(dn);
        } catch (Exception e) {
            logger.error("Error while removing Foundation");
            e.printStackTrace();
            throw e;
        } finally {
            closeContext(dirContext);
        }
    }

    public void updateSchoolOrRecreationCenter(final String localAuthorityName,
        final Foundation entry)
        throws Exception {

        String dn = getDnFromEntity(localAuthorityName, entry);
        // then, update agent information
        DirContext dirContext = gimmeAContext();
        List modificationItems = new ArrayList();
        try {
            Attributes attrs = dirContext.getAttributes(dn);
            if (entry != null) {
                prepareFoundationUpdate(entry, attrs, modificationItems);
            }

            ModificationItem[] modifsItems = new ModificationItem[modificationItems.size()];
            for (int i = 0; i < modificationItems.size(); i++) {
                modifsItems[i] = (ModificationItem) modificationItems.get(i);
            }
            dirContext.modifyAttributes(dn, modifsItems);

        } catch (NamingException ne) {
            throw new Exception("Error while retrieving group information", ne);
        } finally {
            closeContext(dirContext);
        }
    }

    private void prepareFoundationUpdate(final Foundation entry, Attributes attrs,
        List modificationItems)
        throws NamingException {

        Attribute nameAttribute = attrs.get("o");
        setNewAttributeValue(StringUtils.capitalize(entry.getName()),
            modificationItems, nameAttribute);

        Attribute addrAttribute = attrs.get("street");
        setNewAttributeValue(StringUtils.capitalize(entry.getAddress()),
            modificationItems, addrAttribute);

        if (entry.getTelephoneNumber() != null && !entry.getTelephoneNumber().equals("")) {
            Attribute telephoneNumberAttribute = attrs.get("telephoneNumber");
            if (telephoneNumberAttribute == null) {
                telephoneNumberAttribute = new BasicAttribute("telephoneNumber",
                    entry.getTelephoneNumber());
                modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
                    telephoneNumberAttribute));
                logger.debug("updateRecreationCenter() adding telephoneNumber : " + entry.getTelephoneNumber());
            } else {
                setNewAttributeValue(entry.getTelephoneNumber(), modificationItems,
                    telephoneNumberAttribute);
            }
        } else if (attrs.get("telephoneNumber") != null) {
            Attribute telAttribute = attrs.get("telephoneNumber");
            setNewAttributeValue(null, modificationItems, telAttribute);
        }
        if (entry.getEmail() != null && !"".equals(entry.getEmail())) {
            Attribute emailAttribute = attrs.get("mail");
            if (emailAttribute == null) {
                emailAttribute = new BasicAttribute("mail", entry.getEmail());
                modificationItems.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, emailAttribute));
                logger.debug("updateScoolOrRecreationCenter() adding email : " + entry.getEmail());
            } else {
                setNewAttributeValue(entry.getEmail(), modificationItems, emailAttribute);
            }
        } else if (attrs.get("mail") != null) {
            Attribute mailAttribute = attrs.get("mail");
            setNewAttributeValue(null, modificationItems, mailAttribute);
        }
    }

    private void setNewAttributeValue(final String entryAttribue, List modificationItems,
        Attribute attribute)
        throws NamingException {

        if (!((String) attribute.get()).equals(entryAttribue)) {
            attribute.set(0, entryAttribue);
            modificationItems.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, attribute));
            logger.debug("updateRecreationCenter() replacing" + entryAttribue + " with : " + entryAttribue);
        }
    }

    public List searchFoundation(String localAuthorityName, String o, String branch)
        throws Exception {

        DirContext dirContext = gimmeAContext();
        List resultsList = new ArrayList();
        try {

            NamingEnumeration foundationEnum =
                getFoundationNamingEnumeration(localAuthorityName, o,
                "schoolBranch".equals(branch)
                ? schoolBranch
                : recreationCenterBranch, dirContext);

            while (foundationEnum.hasMore()) {
                SearchResult result = (SearchResult) foundationEnum.next();
                Foundation foundation = null;
                if ("schoolBranch".equals(branch)) {
                    foundation = new School();
                } else {
                    foundation = new RecreationCenter();
                }
                populateFoundationObject(result, foundation, resultsList);
            }
        } catch (NamingException ne) {
            ne.printStackTrace();
            throw new Exception("Error while retrieving recreation center data");
        } finally {
            closeContext(dirContext);
        }

        return resultsList;
    }

    private NamingEnumeration getFoundationNamingEnumeration(String localAuthorityName,
        String o, String branch, DirContext dirContext)
        throws NamingException {

        SearchControls controls = getSearchControls(SearchControls.SUBTREE_SCOPE);

        StringBuffer ldapQueryBuffer = new StringBuffer("(&(objectClass=organization)");
        if (o != null && !o.equals("")) {
            ldapQueryBuffer.append("(o=*").append(o).append("*)");
        }
        ldapQueryBuffer.append(")");
        String filter = getDn(null, null, branch, localAuthorityName, ditRoot);
        NamingEnumeration recCenterEnumeration =
            dirContext.search(filter, ldapQueryBuffer.toString(), controls);
        return recCenterEnumeration;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setSecurityPrincipal(String securityPrincipal) {
        this.securityPrincipal = securityPrincipal;
    }

    public void setSecurityCredentials(String securityCredentials) {
        this.securityCredentials = securityCredentials;
    }

    public void setDitRoot(String ditRoot) {
        this.ditRoot = ditRoot;
    }

    public void setPeopleBranch(String peopleBranch) {
        this.peopleBranch = peopleBranch;
    }

    public void setRecreationCenterBranch(String recreationCenterBranch) {
        this.recreationCenterBranch = recreationCenterBranch;
    }

    public void setSchoolBranch(String schoolBranch) {
        this.schoolBranch = schoolBranch;
    }

    public void setAdministratorGroup(String administratorGroup) {
        this.administratorGroup = administratorGroup;
    }

    public void setGroupBranch(String groupBranch) {
        this.groupBranch = groupBranch;
    }
}
