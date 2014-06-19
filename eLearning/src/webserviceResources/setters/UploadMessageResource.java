package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import utils.Date;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class UploadMessageResource extends ServerResource {

	@Post
	public String uploadMessage(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int messageId = info.getInt("messageId");
		int schoolId = info.getInt("schoolId");
		JSONObject messageContent = info.getJSONObject("message");
		
		messageContent.put("timestamp", Date.getSQLDateNow());
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int result = DBUtils.uploadMessage(dbConnection, messageId, messageContent);
		return String.format("%d", result);
	}
}
