package main;

import webservice.application.WebserviceApp;


/**
 * The main class, the entry point of the Server application
 * 
 * @author Razvan Nedelcu
 */
public class MainClass {
	public static void main(String[] args) {
		WebserviceApp webservice = WebserviceApp.getInstance();
		
		try {
			webservice.startWebservice();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
	}
}