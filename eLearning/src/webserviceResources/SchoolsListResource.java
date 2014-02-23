package webserviceResources;


import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class SchoolsListResource extends ServerResource {
	
	@Get
	public String getSchoolsList(Representation entity) {
		return DBCommonOperations.getJsonSchools().toString();
	}
	
}
