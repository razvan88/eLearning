package webservice.application;


import org.restlet.*;
import org.restlet.routing.Router;

import webserviceResources.AllGradesResource;
import webserviceResources.CheckPasswordResource;
import webserviceResources.CourseClassbookResource;
import webserviceResources.CourseDetailsResource;
import webserviceResources.CoursesListResource;
import webserviceResources.FeedbackRequestResource;
import webserviceResources.HomeworkResource;
import webserviceResources.LoginCheckResource;
import webserviceResources.PersonalInformationResource;
import webserviceResources.SchoolNewsArticleResource;
import webserviceResources.SchoolNewsResource;
import webserviceResources.SchoolsListResource;
import webserviceResources.SettingsInformationResource;
import webserviceResources.TimetableResource;
import webserviceResources.UpdateColumnResource;

/**
 * Used to create a root restlet that will receive all the
 * incoming requests
 * 
 * @author Razvan Nedelcu
 */
public class WebserviceDispatcher extends Application {
	
	@Override
	public synchronized Restlet createInboundRoot() {
		Router router = new Router(getContext());
		
		router.attach("/getSchoolsList", SchoolsListResource.class);
		router.attach("/checkLogin", LoginCheckResource.class);
		router.attach("/updateColumn", UpdateColumnResource.class);
		router.attach("/getPersonalInformation", PersonalInformationResource.class);
		router.attach("/getEmailAndDescription", SettingsInformationResource.class);
		router.attach("/getAllGrades", AllGradesResource.class);
		router.attach("/checkPassword", CheckPasswordResource.class);
		router.attach("/getSchoolNews", SchoolNewsResource.class);
		router.attach("/getSchoolNewsArticle", SchoolNewsArticleResource.class);
		router.attach("/getTimetable", TimetableResource.class);
		router.attach("/getCoursesList", CoursesListResource.class);
		router.attach("/getCourseDetails", CourseDetailsResource.class);
		router.attach("/getHomework", HomeworkResource.class);
		router.attach("/getCourseClassbook", CourseClassbookResource.class);
		router.attach("/getFeedbackRequest", FeedbackRequestResource.class);
		
		return router;
	}
}
