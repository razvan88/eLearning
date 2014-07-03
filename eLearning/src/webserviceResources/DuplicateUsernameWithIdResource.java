package webserviceResources;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class DuplicateUsernameWithIdResource extends ServerResource {

	@Post
	public String duplicateUser(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		String username = info.getString("username");
		int id = info.getInt("id");
		String table = info.getString("table");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		boolean isDuplicate = DBUtils.checkForDuplicateUsernamesWithId(dbConnection, username, id, table);
		return isDuplicate ? "1" : "0";
	}
}
