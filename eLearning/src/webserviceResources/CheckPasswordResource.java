package webserviceResources;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class CheckPasswordResource extends ServerResource {

	@Post
	public String getInforamtion(Representation entity) throws IOException {
		Form request = new Form(this.getRequestEntity());
		String pass = request.getValues("password");
		
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		int userId = info.getInt("userId");
		int schoolId = info.getInt("schoolId");
		String table = info.getString("table");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		boolean validPass = DBUtils.checkPassword(dbConnection, table, userId, pass);
		return validPass ? "1" : "0";
	}
	
}
