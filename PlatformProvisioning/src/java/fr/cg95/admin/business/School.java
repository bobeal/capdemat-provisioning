package fr.cg95.admin.business;

import java.io.Serializable;

public class School extends Foundation implements Serializable {
	
    private static final long serialVersionUID = 1L;
	
	public School() {
		super();
		
	}
	
	public School(String name, String address, String telephoneNumber, String email) {
		super(name,address,telephoneNumber,email);
	}

}
