package webservice.application;


import org.restlet.*;
import org.restlet.routing.Router;

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
		
		//router.attach("/chartContent", ChartContentResource.class);
		
		return router;
	}
}
