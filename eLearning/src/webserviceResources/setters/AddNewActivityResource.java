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

public class AddNewActivityResource extends ServerResource {

	@Post
	@Options
	public String addNewActivity(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
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
		boolean noEntry = info.getInt("noEntry") == 1;
		String column = info.getString("columnType");
		int newId = info.getInt("newEntryId");
		String newDate = info.getString("date");
		
		JSONObject activity = new JSONObject();
		String dbColumnName = "";
		
		if(column.equals("activity")) {			
			activity.put("id", newId);
			activity.put("date", newDate);
			activity.put("name", info.getString("name"));
			activity.put("max", info.getDouble("max"));
			activity.put("grade", info.getDouble("grade"));
			activity.put("note", info.getString("note"));
			
			dbColumnName = "activities";
		}
		if(column.equals("grade")) {			
			activity.put("id", newId);
			activity.put("date", newDate);
			activity.put("name", info.getString("name"));
			activity.put("isSemestrialPaper", info.getInt("isSemestrialPaper"));
			activity.put("grade", info.getDouble("grade"));
			activity.put("note", info.getString("note"));
			
			dbColumnName = "grades";
		}
		if(column.equals("absence")) {			
			activity.put("id", newId);
			activity.put("date", newDate);
			activity.put("motivation", info.getString("motivation"));
			activity.put("isMotivated", info.getInt("isMotivated"));
			
			dbColumnName = "absences";
		}
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int row = 0;
		
		if(noEntry) {
			int assocId = DBUtils.getAssocId(dbConnection, studentId, isStudent, courseId, classId, semester, isOptional);
			int assocTableId = isOptional ? 2 : 1;
			//row will be the new id
			row = DBUtils.insertNewActivity(dbConnection, assocId, assocTableId, studentId, dbColumnName, activity);
		} else {
			row = DBUtils.addNewActivity(dbConnection, activityPK, dbColumnName, activity);
		}
		
		return row + "";
	}
}
