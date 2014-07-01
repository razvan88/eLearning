package webserviceResources;

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

public class DuplicateUsernameResource extends ServerResource {

	@Post
	public String duplicateUser(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		JSONArray usernames = JSONArray.fromObject(info.getString("usernames"));
		
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int duplicateIndex = DBUtils.checkForDuplicateUsernames(dbConnection, usernames);
		return duplicateIndex + "";
	}
}
