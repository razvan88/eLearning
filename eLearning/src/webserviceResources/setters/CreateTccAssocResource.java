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

public class CreateTccAssocResource extends ServerResource {

	@Post
	@Options
	public String getTccAssocs(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int studentId = info.getInt("studentId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		int teacherId = info.getInt("teacherId");
		int oldTcc = info.getInt("oldTcc");
		boolean isOptional = info.getInt("optional") == 1;

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int row = isOptional ? DBUtils.createTccAssocForStudent(dbConnection, courseId, teacherId, semester, studentId, oldTcc) : 
					DBUtils.createTccAssocForGroups(dbConnection, classId, courseId, teacherId, semester, oldTcc);
		return row + "";
	}
}
