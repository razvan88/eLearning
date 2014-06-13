package utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Date {

	public static String getSQLDateNow() {
		Calendar cal = new GregorianCalendar();
		
		int yy = cal.get(Calendar.YEAR);
		int mm = cal.get(Calendar.MONTH) + 1;
		int dd = cal.get(Calendar.DAY_OF_MONTH);
		
		int h = cal.get(Calendar.HOUR_OF_DAY);
		int m = cal.get(Calendar.MINUTE);
		int s = cal.get(Calendar.SECOND);
		
		return String.format("%s-%s-%s %s:%s:%s", yy, mm, dd, h, m ,s);
	}
	/**
	 * @param date1 first date to be compared (format: YYYY-MM-DD)
	 * @param date2 second date to be compared (format: YYYY-MM-DD)
	 * @return true if date1 is before date2, false otherwise
	 */
	public static boolean dayDateBefore(String date1, String date2) {
		String[] dateTokens = date1.split("-");
		int year1 = Integer.parseInt(dateTokens[0]);
		int month1 = Integer.parseInt(dateTokens[1]);
		int day1 = Integer.parseInt(dateTokens[2]);
		
		dateTokens = date2.split("-");
		int year2 = Integer.parseInt(dateTokens[0]);
		int month2 = Integer.parseInt(dateTokens[1]);
		int day2 = Integer.parseInt(dateTokens[2]);
		
		if(year1 < year2 || 
				(year1 == year2 && month1 < month2) ||
				(year1 == year2 && month1 == month2 && day1 < day2)) {
			return true;
		}
		
		return false;
	}
	
	/**
	 * @param date1 first date to be compared (format: YYYY-MM-DD)
	 * @param date2 second date to be compared (format: YYYY-MM-DD)
	 * @return true if date1 equals date2, false otherwise
	 */
	public static boolean sameDayDate(String date1, String date2) {
		String[] dateTokens = date1.split("-");
		int year1 = Integer.parseInt(dateTokens[0]);
		int month1 = Integer.parseInt(dateTokens[1]);
		int day1 = Integer.parseInt(dateTokens[2]);
		
		dateTokens = date2.split("-");
		int year2 = Integer.parseInt(dateTokens[0]);
		int month2 = Integer.parseInt(dateTokens[1]);
		int day2 = Integer.parseInt(dateTokens[2]);
		
		return year1 == year2 && 
				month1 == month2 && 
				day1 == day2;
	}
	
	/**
	 * @param date1 first date to be compared (format: YYYY-MM-DD HH:MM:SS)
	 * @param date2 second date to be compared (format: YYYY-MM-DD HH:MM:SS)
	 * @return true if date1 is before date2, false otherwise
	 */
	public static boolean timeDateBefore(String date1, String date2) {
		String[] day1 = date1.split(" ");
		String[] day2 = date2.split(" ");
		
		if(!sameDayDate(day1[0], day2[0])) {
			return dayDateBefore(day1[0], day2[0]);
		}
		
		//check the time
		String[] time1 = day1[1].split(":");
		String[] time2 = day2[1].split(":");

		int hour1 = Integer.parseInt(time1[0]);
		int minute1 = Integer.parseInt(time1[1]);
		int second1 = Integer.parseInt(time1[2]);
				
		int hour2 = Integer.parseInt(time2[0]);
		int minute2 = Integer.parseInt(time2[1]);
		int second2 = Integer.parseInt(time2[2]);
		
		if(hour1 < hour2 ||
				(hour1 == hour2 && minute1 < minute2) ||
				(hour1 == hour2 && minute1 == minute2 && second1 < second2)) {
			return true;
		}
		
		return false;
	}
}
