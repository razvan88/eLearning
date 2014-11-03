package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class FinalizeSemesterResource extends ServerResource {

	@Post
	@Options
	public String finalizeSemester(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		boolean removeTimetables = info.getInt("removeTimetables") == 1;
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		//finalize semester
		DBUtils.computeAverage(dbConnection);
		
		//change semester
		DBUtils.cheangeSemester(dbConnection);
		
		//remove timetables
		if(removeTimetables) {
			DBUtils.removeTimetables(dbConnection);
		}
		
		return "";
	}
}
