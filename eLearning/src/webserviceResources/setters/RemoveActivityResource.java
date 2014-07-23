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

public class RemoveActivityResource extends ServerResource {

	@Post
	public String removeActivity(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int activityPK = info.getInt("activityPK");
		int entryId = info.getInt("entryId");
		String column = info.getString("columnType");
		
		String dbColumnName = "";
		if(column.equals("activity")) {
			dbColumnName = "activities";
		}
		if(column.equals("grade")) {
			dbColumnName = "grades";
		}
		if(column.equals("absence")) {
			dbColumnName = "absences";
		}
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int row = DBUtils.removeActivity(dbConnection, activityPK, dbColumnName, entryId);
		return row + "";
	}
}
