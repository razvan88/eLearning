package webserviceResources.getters;

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

public class HomeworkNameAndMaxGradeResource extends ServerResource {

	@Post
	@Options
	public String getHomeworkNameAndGrade(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int homeworkId = info.getInt("homeworkId");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		JSONObject obj = DBUtils.getHomeworkNameAndMaxGrade(dbConnection, homeworkId);
		
		return obj.toString();
	}
}
