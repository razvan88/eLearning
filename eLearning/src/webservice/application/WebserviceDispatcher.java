package webservice.application;


import org.restlet.*;
import org.restlet.routing.Router;

import webserviceResources.*;
import webserviceResources.getters.*;
import webserviceResources.setters.*;

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
		router.attach("/getAllDeadlines", AllDeadlinesResource.class);
		router.attach("/getFeedbackGiven", FeedbackStatusResource.class);
		
		router.attach("/updateColumn", UpdateColumnResource.class);
		router.attach("/startMessage", StartMessageResource.class);
		router.attach("/uploadMessage", UploadMessageResource.class);
		router.attach("/uploadCourseForumTopic", UploadCourseForumTopicResource.class);
		router.attach("/uploadCourseForumSubject", UploadCourseForumSubjectResource.class);
		router.attach("/uploadHomework", UploadHomeworkResource.class);
		router.attach("/uploadFeedback", UploadFeedbackResource.class);
		
		return router;
	}
}
