package fr.cg95.admin.business;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class Foundation implements Serializable{
	
	private String name            = null  ;
	private String address         = null  ;
	private String telephoneNumber = null  ;
	private String email           = null  ;
	
	
	public Foundation() {
		super();
		
	}
	
	public Foundation(String name, String address, String telephoneNumber, String email) {
		super();
		
		this.name = name;
		this.address = address;
		this.telephoneNumber = telephoneNumber;
		this.email = email;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getTelephoneNumber() {
		return this.telephoneNumber;
	}
	
	public void setTelephoneNumber(String telephoneNumber) {
		this.telephoneNumber = telephoneNumber;
	}
	
	public String toString() {
		
		return new ToStringBuilder(this)
		.append(name).append(address)
		.toString() ;
	}
}
