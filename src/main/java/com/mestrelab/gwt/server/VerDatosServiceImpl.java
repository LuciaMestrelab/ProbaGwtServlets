package com.mestrelab.gwt.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.mestrelab.gwt.client.VerDatosService;
import com.mestrelab.gwt.shared.FieldVerifier;

@SuppressWarnings("serial")
public class VerDatosServiceImpl extends RemoteServiceServlet implements VerDatosService{


	public String verDatos(String name) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		if(!FieldVerifier.isValidName(name)){
			throw new IllegalArgumentException("Name must be at least 5 character long");
		}
		
		return "Hello, " + name;
	}


}
