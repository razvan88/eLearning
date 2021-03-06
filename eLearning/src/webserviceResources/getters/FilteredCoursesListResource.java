package webserviceResources.getters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class FilteredCoursesListResource extends ServerResource {

	@Post
	@Options
	public String getFilteredCoursesList(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONArray courses = DBUtils.getCoursesList(dbConnection, userId);
		JSONArray filteredCourses = new JSONArray();
		for(int i = 0; i < courses.size(); i++) {
			JSONObject crs = courses.getJSONObject(i);
			if(crs.getInt("semester") == semester) {
				JSONObject c = new JSONObject();
				
				c.put("id", crs.getInt("id"));
				c.put("name", crs.getString("name"));
				
				filteredCourses.add(c);
			}
		}
		
		return filteredCourses.toString();
	}
}
