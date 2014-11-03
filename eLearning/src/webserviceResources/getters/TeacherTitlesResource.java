package webserviceResources.getters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class TeacherTitlesResource extends ServerResource {

	@Post
	@Options
	@SuppressWarnings("unused")
	public String getTeacherTitles(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		JSONArray titles = DBCommonOperations.getTitles();
		return titles.toString();
	}
}
