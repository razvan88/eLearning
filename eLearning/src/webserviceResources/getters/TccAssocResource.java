package webserviceResources.getters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class TccAssocResource extends ServerResource {

	@Post
	public String getTccAssocs(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		JSONObject assoc = DBUtils.getTccAssoc(dbConnection, classId, courseId, semester);
		return assoc.toString();
	}
}
