package webserviceResources;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBCredentials;
import database.DBUtils;

public class PersonResource extends ServerResource {

	@Post
	public String getPersonInformation(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int userId = info.getInt("personId");
		String table = info.getString("table");
		int schoolId = info.getInt("schoolId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONObject person = new JSONObject();
		if(!table.equalsIgnoreCase("student")) {
			person = DBUtils.getTeacher(dbConnection, schoolId, table, userId);
		} else {
			person = DBUtils.getInformation(dbConnection, table, userId);
			person.put("table", DBCredentials.STUDENT_TABLE);
			person.put("id", userId);
		}
		
		return person.toString();
	}
}
