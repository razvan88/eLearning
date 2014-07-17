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

public class TeacherHomeworkListResource extends ServerResource {

	@Post
	public String teacherHomeworkList(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		boolean isOptional = info.getInt("optional") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, 0 /*not used*/, false, courseId, classId, semester, isOptional);
		JSONArray homeworkList = DBUtils.getHomeworkListForTeacher(dbConnection, assocId, isOptional ? 2 : 1);
		
		return homeworkList.toString();
	}
}
