package webserviceResources.getters;


import net.sf.json.JSONArray;

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

public class SchoolNewsResource extends ServerResource {
	
	@Post
	@Options
	public String getInformation(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		
		int schoolId = Integer.parseInt(request.getValues("schoolId"));
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		JSONArray news = DBUtils.getSchoolNews(dbConnection);
		
		return news.toString();
	}
}
