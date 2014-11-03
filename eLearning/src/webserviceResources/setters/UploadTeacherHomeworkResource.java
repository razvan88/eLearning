package webserviceResources.setters;

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

public class UploadTeacherHomeworkResource extends ServerResource {

	@Post
	@Options
	public String uploadHomework(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		
		String title = info.getString("name");
		String content = info.getString("text");
		String deadline = info.getString("deadline");
		float maxGrade = (float)info.getDouble("maxGrade");
		String resources = info.getString("resources");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, userId, false, courseId, classId, semester, isOptional);
		int id = DBUtils.uploadTeacherHomework(dbConnection, assocId, isOptional ? 2 : 1,
				title, content, deadline, resources, (float)maxGrade);
		
		return id + "";
	}
}
