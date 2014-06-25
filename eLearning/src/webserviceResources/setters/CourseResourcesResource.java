package webserviceResources.setters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class CourseResourcesResource extends ServerResource {

	@Post
	public String uploadCoureResources(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		int weekId = info.getInt("weekId");
		JSONArray resources = JSONArray.fromObject(info.getString("resources"));
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		int rows = DBUtils.uploadCourseWeekResources(dbConnection, tccId, weekId, resources);
		
		return rows + "";
	}
}
