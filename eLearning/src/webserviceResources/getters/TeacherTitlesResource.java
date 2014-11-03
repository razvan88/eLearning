package webserviceResources.getters;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import database.DBCommonOperations;

public class TeacherTitlesResource extends ServerResource {

	@Post
	@SuppressWarnings("unused")
	public String getTeacherTitles(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		JSONArray titles = DBCommonOperations.getTitles();
		return titles.toString();
	}
}
