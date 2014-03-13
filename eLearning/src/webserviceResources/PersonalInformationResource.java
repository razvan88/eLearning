package webserviceResources;

import java.io.IOException;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBCommonOperations;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class PersonalInformationResource extends ServerResource{
	
	@Post
	public String getInformation(Representation entity) throws IOException {
		Form request = new Form(this.getRequestEntity());
		
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		int userId = info.getInt("userId");
		int schoolId = info.getInt("schoolId");
		String table = info.getString("table");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONObject json = DBUtils.getInformation(dbConnection, table, userId);
		
		JSONObject school = DBCommonOperations.getSchoolInfo(schoolId);
		json.put("schoolName", school.getString("name"));
		json.put("schoolType", school.getString("type"));
		try {
			json.put("schoolBranch", school.getString("branch"));
		} catch(Exception e) {
			// it's OK. No branch for this school
		}
		json.put("city", school.getString("city"));
		
		return json.toString();
	}
	
}
