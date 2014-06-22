package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class CourseNameResource extends ServerResource {

	@Post
	public String getCourseName(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		//int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		
		//String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		
		String name = ((JSONObject)(DBCommonOperations.getCoursesInfo(new String[] { courseId + "" }).get(0))).getString("name");
		return name;
	}
}
