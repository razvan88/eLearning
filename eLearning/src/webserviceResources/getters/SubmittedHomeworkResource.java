package webserviceResources.getters;

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

public class SubmittedHomeworkResource extends ServerResource {

	@Post
	public String getSubmittedHomework(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int homeworkId = info.getInt("homeworkId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int tccId = DBUtils.getTeachClassCourseId(dbConnection, classId, courseId);
		JSONArray obj = DBUtils.getSubmittedHomework(dbConnection, homeworkId, tccId);
		
		return obj.toString();
	}
}