package webservice.application;


import org.restlet.*;
import org.restlet.routing.Router;

import webserviceResources.AllGradesResource;
import webserviceResources.CheckPasswordResource;
import webserviceResources.LoginCheckResource;
import webserviceResources.getters.AllStudentsResource;
import webserviceResources.getters.AvailableClassesResource;
import webserviceResources.getters.ClassStudentsResource;
import webserviceResources.getters.CourseClassbookResource;
import webserviceResources.getters.CourseDetailsResource;
import webserviceResources.getters.CoursesListResource;
import webserviceResources.getters.FeedbackRequestResource;
import webserviceResources.getters.ForumSubjectResource;
import webserviceResources.getters.ForumSummaryResource;
import webserviceResources.getters.HomeworkResource;
import webserviceResources.getters.MessagesResource;
import webserviceResources.getters.PersonResource;
import webserviceResources.getters.PersonalInformationResource;
import webserviceResources.getters.SchoolNewsArticleResource;
import webserviceResources.getters.SchoolNewsResource;
import webserviceResources.getters.SchoolsListResource;
import webserviceResources.getters.SettingsInformationResource;
import webserviceResources.getters.TeachersResource;
import webserviceResources.getters.TimetableResource;
import webserviceResources.setters.UpdateColumnResource;
import webserviceResources.setters.UploadMessageResource;

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
		router.attach("/getForumSummary", ForumSummaryResource.class);
		router.attach("/getForumSubject", ForumSubjectResource.class);
		router.attach("/getMessages", MessagesResource.class);
		router.attach("/getTeachers", TeachersResource.class);
		router.attach("/getPerson", PersonResource.class);
		router.attach("/getAvailableClasses", AvailableClassesResource.class);
		router.attach("/getClassStudents", ClassStudentsResource.class);
		router.attach("/getAllStudents", AllStudentsResource.class);
		
		router.attach("/uploadMessage", UploadMessageResource.class);
		
		return router;
	}
}
