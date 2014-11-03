package webserviceResources.getters;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;

import net.sf.ezmorph.bean.MorphDynaBean;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.resource.Options;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

import utils.ConfigurationSettings;
import utils.Date;
import database.DBConnection;
import database.DBConnectionManager;
import database.DBUtils;

public class AllDeadlinesResource extends ServerResource {

	@Post
	@Options
	@SuppressWarnings("unused")
	public String getAllDeadlines(Representation entity) {
		//do not treat Options requests
		if(this.getRequest().getMethod() == Method.OPTIONS)
			return "";
				
		Form request = new Form(this.getRequestEntity());
		JSONObject info = JSONObject.fromObject(request.getValues("info"));

		int schoolId = info.getInt("schoolId");
		int userId = info.getInt("userId");
		int semester = info.getInt("semester");

		String database = ConfigurationSettings
				.getSchoolDatabaseName(schoolId);
		DBConnection dbConnection = DBConnectionManager.getConnection(
				schoolId, database);

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
		Object[] array = (Object[])JSONArray.toArray(filteredDeadlines);
		MorphDynaBean[] mdb = new MorphDynaBean[array.length];
		for(int i = 0; i < array.length; i++) {
			mdb[i] = (MorphDynaBean)array[i];
		}

		Arrays.sort(mdb, new Comparator<MorphDynaBean>() {
			@Override
			public int compare(MorphDynaBean o1, MorphDynaBean o2) {
				String date1 = (String)o1.get("deadline");
				String date2 = (String)o2.get("deadline");

				return Date.timeDateBefore(date1, date2) ? -1 : 1;
			}
		});

		JSONObject allDeadlines = new JSONObject();
		JSONArray allNames = new JSONArray();
		JSONArray allDates = new JSONArray();
		JSONArray allCourses = new JSONArray();
		for (MorphDynaBean obj : mdb) {
			allNames.add(obj.get("name"));
			allDates.add(obj.get("deadline"));
			allCourses.add(obj.get("course"));
		}
		allDeadlines.put("dates", allDates);
		allDeadlines.put("tips", allNames);
		allDeadlines.put("courses", allCourses);
		
		return allDeadlines.toString();
	}
}
