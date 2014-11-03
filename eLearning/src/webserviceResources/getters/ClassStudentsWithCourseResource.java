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

public class ClassStudentsWithCourseResource extends ServerResource {

	@Post
	@Options
	public String getClassStudents(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONArray classStuds = DBUtils.getClassStudents(dbConnection, classId);
		JSONArray filteredStuds = new JSONArray();
		
		for(int i = 0; i < classStuds.size(); i++) {
			JSONObject stud = classStuds.getJSONObject(i);
			int studId = stud.getInt("id");
			JSONArray allCourses = DBUtils.getCoursesList(dbConnection, studId);
			for(int j = 0; j < allCourses.size(); j++) {
				if(allCourses.getJSONObject(j).getInt("id") == courseId) {
					filteredStuds.add(stud);
					break;
				}
			}
		}
		
		return filteredStuds.toString();
	}
	
}
