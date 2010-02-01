package fr.cg95.admin.business;

import java.io.Serializable;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * @author bor@zenexity.fr
 */
public class Agent implements Serializable {

	private static final long serialVersionUID = 1L;

    private String uid;
    private String password;
    private String lastName;
    private String firstName;
    private String telephoneNumber;
    private String email;
    private String[] groups;
    
    /** full constructor */
    public Agent(String uid, String password, String lastName, String firstName, 
            String phoneNumber, String email, String[] groups) {
        this.uid = uid;
        this.password = password;
        this.lastName = lastName;
        this.firstName = firstName;
        this.telephoneNumber = phoneNumber;
        this.email = email;
        this.groups = groups;
    }

    public Agent() {
    }

    public String getUid() {
        return this.uid;
    }

    public void setUid(String login) {
        this.uid = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getTelephoneNumber() {
        return this.telephoneNumber;
    }

    public void setTelephoneNumber(String phoneNumber) {
        this.telephoneNumber = phoneNumber;
    }

    public String[] getGroups() {
        return groups;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("uid", getUid())
            .toString();
    }
}
