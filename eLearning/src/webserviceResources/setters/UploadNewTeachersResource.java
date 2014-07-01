package webserviceResources.setters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class UploadNewTeachersResource extends ServerResource {

	@Post
	public String uploadNewGroup(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		JSONArray teachers = JSONArray.fromObject(info.getString("teachers"));

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int rows = DBUtils.populateTeachers(dbConnection, teachers);

		/*
		 * intoarce ultimul index adaugat, ca sa pot afisa in gui ceva de genul:
		 * "nu s-au putut adauga decat primii X elevi"
		 */
		return rows + "";
	}
}