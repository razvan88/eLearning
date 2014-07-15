package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class OldTccIdResource extends ServerResource {

	@Post
	public String getClassForStudent(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int semester = info.getInt("semester");
		int courseId = info.getInt("courseId");
		boolean isOptional = info.getInt("optional") == 1;
		int classId = info.getInt("classId");
		int studentId = info.getInt("studentId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int id = DBUtils.getOldTccId(dbConnection, courseId, classId, studentId, semester, isOptional);
		return id + "";
	}
}
