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

public class UpdateAbsenceResource extends ServerResource {

	@Post
	@Options
	@SuppressWarnings("unused")
	public String getClassStudents(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int schoolId = info.getInt("schoolId");
		int classId = info.getInt("classId");
		int courseId = info.getInt("courseId");
		int semester = info.getInt("semester");
		boolean isStudent = info.getInt("student") == 1;
		boolean isOptional = info.getInt("optional") == 1;
		int activityPK = info.getInt("activityPK");
		int studentId = info.getInt("studentId");
		int absenceId = info.getInt("absenceId");
		int isMotivated = info.getInt("isMotivated");
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		//int assocId = DBUtils.getAssocId(dbConnection, studentId, isStudent, courseId, classId, semester, isOptional);
		//int assocTableId = isOptional ? 2 : 1;
		
		DBUtils.updateAbsenceMotivation(dbConnection, activityPK, absenceId, isMotivated);
		return "";
	}
}
