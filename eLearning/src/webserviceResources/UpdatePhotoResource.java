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

public class UpdatePhotoResource extends ServerResource{

	@Post
	public String updateColumn(Representation entity) throws IOException {
		Form request = new Form(this.getRequestEntity());
		
		String value = request.getValues("value");
		
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		int userId = info.getInt("userId");
		int schoolId = info.getInt("schoolId");
		String table = info.getString("table");
		String column = info.getString("column");
		
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		return String.format("%d", DBUtils.updateColumn(dbConnection, table, userId, column, value));
	}
}
