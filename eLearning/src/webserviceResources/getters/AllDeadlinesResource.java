package webserviceResources.getters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import net.sf.json.JSONArray;
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

public class AllDeadlinesResource extends ServerResource {

	@Post
	public String getAllDeadlines(Representation entity) {
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");

		String database = ConfigurationSettings.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(schoolId,
				database);

		JSONArray deadlines = DBUtils.getAllDeadlines(dbConnection, userId);

		Calendar cal = new GregorianCalendar();
		int currentDay = cal.get(Calendar.DAY_OF_MONTH);
		int currentMonth = cal.get(Calendar.MONTH) + 1;
		int currentYear = cal.get(Calendar.YEAR);
		String currentDayDate = String.format("%s-%s-%s", currentYear,
				currentMonth, currentDay);
		JSONArray filteredDeadlines = new JSONArray();

		// keep only the valid deadlines
		for (int i = 0; i < deadlines.size(); i++) {
			JSONObject deadline = deadlines.getJSONObject(i);

			String dayDate = deadline.getString("deadline").split(" ")[0];
			if (Date.dayDateBefore(dayDate, currentDayDate)) {
				continue;
			}

			filteredDeadlines.add(deadline);
		}

		// sort the deadlines
		JSONObject[] array = (JSONObject[]) JSONArray
				.toArray(filteredDeadlines);
		Arrays.sort(array, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				String date1 = o1.getString("deadline");
				String date2 = o2.getString("deadline");

				return Date.timeDateBefore(date1, date2) ? -1 : 1;
			}
		});

		// remove unsorted elements and add them in order
		filteredDeadlines.clear();
		for (JSONObject obj : array) {
			filteredDeadlines.add(obj);
		}

		return filteredDeadlines.toString();
	}
}
