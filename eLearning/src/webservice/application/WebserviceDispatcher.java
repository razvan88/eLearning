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
		router.attach("/checkPassword", CheckPasswordResource.class);
		router.attach("/getSchoolNews", SchoolNewsResource.class);
		router.attach("/getSchoolNewsArticle", SchoolNewsArticleResource.class);
		router.attach("/getTimetable", TimetableResource.class);
		router.attach("/getCoursesList", CoursesListResource.class);
		router.attach("/getCourseDetails", CourseDetailsResource.class);
		router.attach("/getHomework", HomeworkResource.class);
		router.attach("/getFeedbackRequest", FeedbackRequestResource.class);
		router.attach("/getTeacherFeedbackRequest", TeacherFeedbackRequestResource.class);
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
		router.attach("/getTeacherCoursesAndClassses", TeacherCoursesAndClassesResource.class);
		router.attach("/getCourseName", CourseNameResource.class);
		router.attach("/getClassName", ClassNameResource.class);
		router.attach("/getTeacherHomeworkList", TeacherHomeworkListResource.class);
		router.attach("/getTeacherHomework", TeacherHomeworkResource.class);
		router.attach("/getHomeworkNameAndMaxGrade", HomeworkNameAndMaxGradeResource.class);
		router.attach("/getSubmittedHomework", SubmittedHomeworkResource.class);
		router.attach("/getWeekResources", WeekResourcesResource.class);
		router.attach("/getAuxiliaryTimetable", AuxiliaryTimetableResource.class);
		router.attach("/getTeacherList", TeacherListResource.class);
		router.attach("/getAllCoursesList", AllCoursesListResource.class);
		router.attach("/getClassList", AllClassListResource.class);
		router.attach("/checkDuplicateUsername", DuplicateUsernameResource.class);
		router.attach("/getTeacherTitles", TeacherTitlesResource.class);
		router.attach("/getOtherJobs", OtherJobsResource.class);
		router.attach("/getTeacherByCnp", TeacherByCnpResource.class);
		router.attach("/getAuxiliaryByCnp", AuxiliaryByCnpResource.class);
		router.attach("/checkDuplicateUsernameWithId", DuplicateUsernameWithIdResource.class);
		router.attach("/getStudentByCnp", StudentByCnpResource.class);
		router.attach("/getSemesterStructure", GetSemesterStructureResource.class);
		router.attach("/getSemesterNo", GetSemesterNumberResource.class);
		router.attach("/getAllTccAssocs", AllTccAssocsResource.class);
		router.attach("/getTccAssoc", TccAssocResource.class);
		router.attach("/getClassForStudent", ClassForStudentResource.class);
		router.attach("/getOldTccId", OldTccIdResource.class);
		router.attach("/getStudentActivities", StudentActivitiesResource.class);
		router.attach("/getFeedbackResponses", FeedbackResponsesResource.class);
		router.attach("/getGradesArchive", GetGradesArchiveResource.class);
		router.attach("/getGradesArchiveByCnp", GetGradesArchiveByCnpResource.class);
		
		router.attach("/updateColumn", UpdateColumnResource.class);
		router.attach("/startMessage", StartMessageResource.class);
		router.attach("/uploadMessage", UploadMessageResource.class);
		router.attach("/uploadCourseForumTopic", UploadCourseForumTopicResource.class);
		router.attach("/uploadCourseForumSubject", UploadCourseForumSubjectResource.class);
		router.attach("/uploadHomework", UploadHomeworkResource.class);
		router.attach("/uploadFeedback", UploadFeedbackResource.class);
		router.attach("/uploadTeacherFeedback", UploadTeacherFeedbackResource.class);
		router.attach("/uploadTeacherHomework", UploadTeacherHomeworkResource.class);
		router.attach("/updateTeacherHomework", UpdateTeacherHomeworkResource.class);
		router.attach("/uploadTeacherHomeworkResources", UploadTeacherHomeworkResourcesResource.class);
		router.attach("/rateHomework", RateHomeworkResource.class);
		router.attach("/uploadAuxiliaryTimetable", UploadAuxiliaryTimetableResource.class);
		router.attach("/uploadClassTimetable", UploadClassTimetableResource.class);
		router.attach("/uploadNewsArticle", UploadNewsArticleResource.class);
		router.attach("/uploadCourseWeekInfo", CourseWeekInfoResource.class);
		router.attach("/uploadCourseResources", CourseResourcesResource.class);
		router.attach("/uploadNewGroup", UploadNewGroupResource.class);
		router.attach("/uploadNewTeachers", UploadNewTeachersResource.class);
		router.attach("/uploadNewAuxiliary", UploadNewAuxiliaryResource.class);
		router.attach("/modifyTeacher", ModifyTeacherResource.class);
		router.attach("/removeUser", RemoveUserResource.class);
		router.attach("/modifyAuxiliary", ModifyAuxiliaryResource.class);
		router.attach("/modifyStudent", ModifyStudentResource.class);
		router.attach("/uploadNewCourse", NewCourseResource.class);
		router.attach("/uploadClassTransitions", ClassTransitionsResource.class);
		router.attach("/removeNews", RemoveNewsResource.class);
		router.attach("/modifyNews", ModifyNewsResource.class);
		router.attach("/uploadSemesterStructure", SemesterStructureResource.class);
		router.attach("/setSemesterNo", SetSemesterNumberResource.class);
		router.attach("/createTccAssoc", CreateTccAssocResource.class);
		router.attach("/updateSemestrialPaper", UpdateSemestrialPaperResource.class);
		router.attach("/updateAbsence", UpdateAbsenceResource.class);
		router.attach("/addNewActivity", AddNewActivityResource.class);
		router.attach("/removeActivity", RemoveActivityResource.class);
		router.attach("/updateActivity", UpdateActivityResource.class);
		router.attach("/finalizeSemester", FinalizeSemesterResource.class);
		
		return router;
	}
}
