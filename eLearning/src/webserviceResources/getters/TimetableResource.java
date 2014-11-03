package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBCommonOperations;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class TimetableResource extends ServerResource {

	@Post
	@Options
	public String getTimetable(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		int classId = DBUtils.getClassIdForUser(dbConnection, userId);
		
		JSONObject timetable = DBUtils.getTimetable(dbConnection, classId);
		String className = DBCommonOperations.getClassName(classId);
		timetable.put("class", className);
		return timetable.toString();
	}
}
