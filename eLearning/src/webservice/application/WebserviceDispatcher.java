package webservice.application;


import org.restlet.*;
import org.restlet.routing.Router;

import webserviceResources.LoginCheckResource;
import webserviceResources.SchoolsListResource;

/**
 * Used to create a root restlet that will receive all the
 * incoming requests
 * 
 * @author Razvan Nedelcu
 */
public class WebserviceDispatcher extends Application{
	
	@Override
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());
		
		router.attach("/getSchoolsList", SchoolsListResource.class);
		router.attach("/checkLogin", LoginCheckResource.class);
		
		return router;
	}
}
