package webserviceResources.setters;

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

public class CourseResourcesResource extends ServerResource {

	@Post
	@Options
	public String uploadCoureResources(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		int weekId = info.getInt("weekId");
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");
		boolean isStudent = info.getInt("student") == 1;
		boolean isOptional = info.getInt("optional") == 1;
		
		JSONArray resources = JSONArray.fromObject(info.getString("resources"));
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		int rows = DBUtils.uploadCourseWeekResources(dbConnection, assocId, assocTableId, weekId, resources);
		
		return rows + "";
	}
}
