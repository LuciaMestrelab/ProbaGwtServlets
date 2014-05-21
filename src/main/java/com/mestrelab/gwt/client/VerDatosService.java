package com.mestrelab.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("verDatos")
public interface VerDatosService extends RemoteService{

	String verDatos(String name) throws IllegalArgumentException;

}
