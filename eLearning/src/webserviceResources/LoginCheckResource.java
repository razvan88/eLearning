package webserviceResources;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

import utils.ConfigurationSettings;

public class LoginCheckResource extends ServerResource {

	@Post
	public boolean getJsonContent(Representation entity) throws IOException {
		String stringUserInput = new Form(this.getRequestEntity()).getValues("userData");
		JSONObject jsonUserInput = JSONObject.fromObject(stringUserInput);
		
		int schoolId = jsonUserInput.getInt("schoolId");
		String username = jsonUserInput.getString("username");
		String password = jsonUserInput.getString("password");
		String dbName = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		
		DBConnection dbConn = DBConnectionManager.getConnection(schoolId, dbName);
		boolean login = DBUtils.checkLogin(dbConn, dbName, username, password);
		
		return login;
	}
	
}