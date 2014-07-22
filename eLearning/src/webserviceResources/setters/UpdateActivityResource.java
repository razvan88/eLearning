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

public class UpdateActivityResource extends ServerResource {

	@Post
	public String removeActivity(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int activityPK = info.getInt("activityPK");
		int activityId = info.getInt("activityId");
		String date = info.getString("date");
		String name = info.getString("name");
		String note = info.getString("note");
		double max = info.getDouble("max");
		double grade = info.getDouble("grade");
		
		JSONObject activity = new JSONObject();
		activity.put("id", activityId);
		activity.put("date", date);
		activity.put("name", name);
		activity.put("max", max);
		activity.put("grade", grade);
		activity.put("note", note);
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int row = DBUtils.updateActivity(dbConnection, activityPK, activity);
		return row + "";
	}
}
