package webserviceResources.getters;


import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class SchoolsListResource extends ServerResource {
	
	@Get
	@Options
	public String getSchoolsList(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
		
		return DBCommonOperations.getJsonSchools().toString();
	}
	
}
