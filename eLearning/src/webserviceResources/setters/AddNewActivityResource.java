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

public class AddNewActivityResource extends ServerResource {

	@SuppressWarnings("unused")
	@Post
	public String addNewActivity(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		boolean isStudent = info.getInt("student") == 1;
		boolean isOptional = info.getInt("optional") == 1;
		int studentId = info.getInt("studentId");
		int activityPK = info.getInt("activityPK");
		int newActivityId = info.getInt("newActivityId");
		String date = info.getString("date");
		String name = info.getString("name");
		double max = info.getDouble("max");
		double grade = info.getDouble("grade");
		String note = info.getString("note");
		boolean noEntry = info.getInt("noEntry") == 1;
		
		JSONObject activity = new JSONObject();
		activity.put("id", newActivityId);
		activity.put("date", date);
		activity.put("name", name);
		activity.put("max", max);
		activity.put("grade", grade);
		activity.put("notes", note);
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int row = 0;
		
		if(noEntry) {
			int assocId = DBUtils.getAssocId(dbConnection, studentId, isStudent, courseId, classId, semester, isOptional);
			int assocTableId = isOptional ? 2 : 1;
			
			row = DBUtils.insertNewActivity(dbConnection, assocId, assocTableId, studentId, activity);
		} else {
			row = DBUtils.addNewActivity(dbConnection, activityPK, activity);
		}
		
		return row + "";
	}
}
