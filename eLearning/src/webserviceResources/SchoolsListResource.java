package webserviceResources;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class SchoolsListResource extends ServerResource {
	
	@Post
	public String getJsonContent(Representation entity) throws IOException {
		String stringUserInput = new Form(this.getRequestEntity()).getValues("userData");
		JSONObject jsonUserInput = JSONObject.fromObject(stringUserInput);
		
		return null;
	}
	
	@Get
	public String getSchoolsList(Representation entity) {
		return DBCommonOperations.getJsonSchools().toString();
	}
	
}
