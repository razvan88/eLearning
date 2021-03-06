package webserviceResources.getters;

import net.sf.json.JSONArray;
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

public class GetGradesArchiveByCnpResource extends ServerResource {

	@Post
	@Options
	public String getGradesArchive(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		String cnp = info.getString("studentCnp");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONObject student = DBUtils.getStudentByCnp(dbConnection, cnp);
		if(!student.containsKey("id")) {
			return "[]";
		}
		
		int studentId = student.getInt("id");
		JSONArray responses = DBUtils.getGradesArchive(dbConnection, studentId);
		return responses.toString();
	}
}
