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

public class SemesterStructureResource extends ServerResource {

	@Post
	public String uploadSemesterStructure(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		String date1 = info.getString("date1");
		String date2 = info.getString("date2");
		String week1 = info.getString("week1");
		String week2 = info.getString("week2");
		String holiday1 = info.getString("holiday1");
		String holiday2 = info.getString("holiday2");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int rows = DBUtils.uploadSemesterStructure(dbConnection, date1, week1, holiday1, date2, week2, holiday2);
		return rows + "";
	}
}
