package webservice.application;


import org.restlet.routing.Filter;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;


public class CORSFilter extends Filter {

    public CORSFilter(Context context) {
        super(context);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
    	final String HEADERS = "org.restlet.http.headers";
    	
        Form responseHeaders = (Form) response.getAttributes().get(HEADERS);
		if (responseHeaders == null) {
		    responseHeaders = new Form();
		    response.getAttributes().put(HEADERS, responseHeaders);
		}
	
		responseHeaders.add("Access-Control-Allow-Headers", 
				"access-control-allow-origin, " +
				"access-control-allow-credentials," +
				"access-control-allow-methods, " +
				"access-control-allow-headers, " +
				"content-type, " +
				"accept, " +
				"authorization, " +
				"x-requested-with");
		responseHeaders.add("Access-Control-Allow-Origin", 
				"*");
		responseHeaders.add("Access-Control-Allow-Methods",
				"GET, POST");
		responseHeaders.add("Access-Control-Max-Age", 
				"1209600");
    }
   
}
