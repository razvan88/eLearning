package webserviceResources;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;
import utils.ConfigurationSettings;

public class LoginCheckResource extends ServerResource {

	@Post
	@Options
	public String checkInput(Representation entity) throws IOException {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		String stringUserInput = new Form(this.getRequestEntity()).getValues("userData");
		JSONObject jsonUserInput = JSONObject.fromObject(stringUserInput);
		
		int schoolId = jsonUserInput.getInt("schoolId");
		String username = jsonUserInput.getString("username");
		String password = jsonUserInput.getString("password");
		String dbName = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		
		DBConnection dbConn = DBConnectionManager.getConnection(schoolId, dbName);
		JSONObject result = DBUtils.checkLogin(dbConn, username, password);
		result.put("schoolId", schoolId);
		
		return result == null ? new JSONObject().toString() : result.toString();
	}
	
}
