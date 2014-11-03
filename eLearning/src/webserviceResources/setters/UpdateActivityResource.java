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

public class UpdateActivityResource extends ServerResource {

	@Post
	@Options
	public String removeActivity(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int activityPK = info.getInt("activityPK");
		int entryId = info.getInt("entryId");
		String column = info.getString("columnType");
		String dbColumnName = "";
		
		JSONObject activity = new JSONObject();
		
		//create activity object
		if(column.equals("activity")) {
			activity.put("id", entryId);
			activity.put("date", info.getString("date"));
			activity.put("name", info.getString("name"));
			activity.put("max", info.getDouble("max"));
			activity.put("grade", info.getDouble("grade"));
			activity.put("note", info.getString("note"));
			
			dbColumnName = "activities";
		}
		
		//create grade object
		if(column.equals("grade")) {
			activity.put("id", entryId);
			activity.put("date", info.getString("date"));
			activity.put("name", info.getString("name"));
			activity.put("grade", info.getDouble("grade"));
			activity.put("note", info.getString("note"));
			activity.put("isSemestrialPaper", info.getInt("isSemestrialPaper"));
			
			dbColumnName = "grades";
		}
		
		//create absence object
		if(column.equals("absence")) {
			activity.put("id", entryId);
			activity.put("date", info.getString("date"));
			activity.put("motivation", info.getString("motivation"));
			activity.put("isMotivated", info.getDouble("isMotivated"));
			
			dbColumnName = "absences";
		}
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int row = DBUtils.updateActivity(dbConnection, activityPK, dbColumnName, activity);
		return row + "";
	}
}
