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

public class UploadTeacherFeedbackResource extends ServerResource {

	@Post
	public String getFeedbackRequest(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int courseId = info.getInt("courseId");
		int classId = info.getInt("classId");
		boolean isStudent = info.getInt("student") == 1;
		boolean isOptional = info.getInt("optional") == 1;
		int semester = info.getInt("semester");
		int userId = info.getInt("userId");
		int isAvailable = info.getInt("isAvailable");
		JSONArray jsonAspects = JSONArray.fromObject(info.getString("aspects"));
		StringBuffer stringAspects = new StringBuffer();

		stringAspects.append("[");
		for (int i = 0; i < jsonAspects.size(); i++) {
			stringAspects.append("\"");
			stringAspects.append((String) (jsonAspects.get(i)));
			stringAspects.append("\"");
			if (i + 1 < jsonAspects.size()) {
				stringAspects.append(",");
			}
		}
		stringAspects.append("]");

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int assocId = DBUtils.getAssocId(dbConnection, userId, isStudent, courseId, classId, semester, isOptional);
		int assocTableId = isOptional ? 2 : 1;
		boolean success = false;

		// check if it exists
		int feedbackId = DBUtils.getTeacherFeedbackId(dbConnection, assocId, assocTableId);
		if (feedbackId < 0) {
			// insert
			success = DBUtils.uploadTeacherFeedback(dbConnection, assocId, assocTableId,
					isAvailable, stringAspects.toString());
		} else {
			// update
			success = DBUtils.updateTeacherFeedback(dbConnection, feedbackId,
					isAvailable, stringAspects.toString());
		}

		return success ? "1" : "0";
	}
}
