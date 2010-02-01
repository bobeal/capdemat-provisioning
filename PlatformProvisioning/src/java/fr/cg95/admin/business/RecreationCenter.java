package fr.cg95.admin.business;

import java.io.Serializable;

public class RecreationCenter extends Foundation implements Serializable{

    private static final long serialVersionUID = 1L;
	
	public RecreationCenter() {
		super();
		
	}

	public RecreationCenter(String name, String address, String telephoneNumber,String email) {
		super(name,address,telephoneNumber,email);
		
	}
	
}
