package webserviceResources.setters;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import utils.Date;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class StartMessageResource extends ServerResource {

	@Post
	public String startMessage(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int initiatorId = info.getInt("initiatorId");
		String initiatorTable = info.getString("initiatorTable");
		int responderId = info.getInt("responderId");
		String responderTable = info.getString("responderTable");
		int schoolId = info.getInt("schoolId");
		String date = Date.getSQLDateNow();
		String content = info.getString("content");

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		int messageId = DBUtils.getConversationIdBetween(dbConnection,
				initiatorId, initiatorTable, responderId, responderTable);
		boolean added = false;
		if (messageId < 0) {
			// other messages do not exist
			added = DBUtils.uploadNewMessage(dbConnection, initiatorId,
					initiatorTable, responderId, responderTable, date, content);
		} else {
			// other messages exist, so append this one
			JSONObject message = new JSONObject();
			message.put("content", content);
			message.put("timestamp", date);
			message.put("sender_index", DBUtils.getSenderIndexForMessage(
					dbConnection, messageId, initiatorId, initiatorTable));

			added = DBUtils.uploadMessage(dbConnection, messageId, message) == 1;
		}

		return added ? "1" : "0";
	}
}
