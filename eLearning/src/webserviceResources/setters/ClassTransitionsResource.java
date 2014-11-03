package webserviceResources.setters;

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

public class ClassTransitionsResource extends ServerResource {

	@Post
	@Options
	public String modifyTeacher(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		JSONArray classes = JSONArray.fromObject(info.getString("classes"));
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		//do class transitions
		int rows = DBUtils.doClassTransitions(dbConnection, classes);
		
		//remove associations
		DBUtils.removeAssociations(dbConnection);
		
		//remove last-year students
		DBUtils.removeLastYearStudents(dbConnection);
		
		//remove timetables
		DBUtils.removeTimetables(dbConnection);
		
		//remove courses list
		DBUtils.removeCoursesList(dbConnection);
		
		//remove homework tables
		DBUtils.removeHomeworkTables(dbConnection);
		
		//remove holidays table
		DBUtils.removeHolidays(dbConnection);
		
		//remove resources
		DBUtils.removeResources(dbConnection);
		
		return rows + "";
	}
}
