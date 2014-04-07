package webserviceResources.setters;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class UploadMessageResource extends ServerResource {

	@Post
	public String uploadMessage(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));
		
		int messageId = info.getInt("messageId");
		int schoolId = info.getInt("schoolId");
		JSONObject messageContent = info.getJSONObject("message");
		
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTimeInMillis(System.currentTimeMillis());
		StringBuffer buffer = new StringBuffer();
		buffer.append(calendar.get(Calendar.YEAR));
		buffer.append("-");
		int month = calendar.get(Calendar.MONTH) + 1;
		buffer.append((month < 10 ? "0" : "") + month);
		buffer.append("-");
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		buffer.append((day < 10 ? "0" : "") + day);
		buffer.append(" ");
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		buffer.append((hour < 10 ? "0" : "") + hour);
		buffer.append(":");
		int minute = calendar.get(Calendar.MINUTE);
		buffer.append((minute < 10 ? "0" : "") + minute);
		buffer.append(":");
		int second = calendar.get(Calendar.SECOND);
		buffer.append((second < 10 ? "0" : "") + second);
		messageContent.put("timestamp", buffer.toString());
		
		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId, database);
		
		int result = DBUtils.uploadMessage(dbConnection, messageId, messageContent);
		return String.format("%d", result);
	}
}
