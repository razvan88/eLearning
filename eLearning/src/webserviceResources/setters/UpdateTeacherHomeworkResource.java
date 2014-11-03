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

public class UpdateTeacherHomeworkResource extends ServerResource {

	@Post
	@Options
	public String updateHomework(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int homeworkId = info.getInt("homeworkId");
		String title = info.getString("name");
		String content = info.getString("text");
		String deadline = info.getString("deadline");
		String resources = info.getString("resources");
		float maxGrade = (float) info.getDouble("maxGrade");

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int rowsAffected = DBUtils.updateTeacherHomework(dbConnection,
				homeworkId, title, content, deadline, resources, maxGrade);

		return rowsAffected + "";
	}
}
