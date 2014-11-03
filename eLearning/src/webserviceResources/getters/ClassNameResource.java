package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class ClassNameResource extends ServerResource {

	@Post
	@Options
	public String getClassName(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		//int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		
		//String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		
		String className = DBCommonOperations.getGroupName(classId);
		return className;
	}
}
