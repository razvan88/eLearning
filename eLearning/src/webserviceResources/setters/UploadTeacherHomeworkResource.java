package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class UploadTeacherHomeworkResource extends ServerResource {

	@Post
	public String uploadHomework(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		//int userId = info.getInt("userId");
		
		String title = info.getString("name");
		String content = info.getString("text");
		String deadline = info.getString("deadline");
		float maxGrade = (float)info.getDouble("maxGrade");
		String resources = info.getString("resources");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId,
				courseId);
		int id = DBUtils.uploadTeacherHomework(dbConnection, tccId,
				title, content, deadline, resources, (float)maxGrade);
		
		return id + "";
	}
}
