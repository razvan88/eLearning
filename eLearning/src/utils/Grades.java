package utils;

public class Grades {

	public static int round(double d){
	    double dAbs = Math.abs(d);
	    int i = (int) dAbs;
	    double result = dAbs - (double) i;
	    if(result < 0.5){
	        return d < 0 ? -i : i;            
	    } else {
	        return d < 0 ? - (i + 1) : i + 1;          
	    }
	}
	
	public static double round(double value, int places) {
	    if (places < 0) {
	    	throw new IllegalArgumentException();
	    }

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
}
