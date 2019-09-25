package com.trustcommerce.ipa.dal.commport.ports;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Locale;

public class PortReader {

	static class StreamReader extends Thread {
		
        private InputStream is;
        private StringWriter sw= new StringWriter();

        private StreamReader(InputStream is) {
            this.is = is;
        }

	    public void run() {
	        	
	    	try {
	    		int c;
	            while ((c = is.read()) != -1) {
	            	sw.write(c);
	            }
	        }
	        catch (IOException e) { 
	        }
        }

        private String GetResult() {
            return sw.toString();
        }
        
        public static String GetDevicePID() {

        	String devicePID = "";
        	
    		try {
    			
    			Process process = Runtime.getRuntime().exec("reg  QUERY HKLM\\HARDWARE\\DEVICEMAP\\SERIALCOMM /s");
    			PortReader.StreamReader reader = new PortReader.StreamReader(process.getInputStream());
                reader.start();
                process.waitFor();
                reader.join();
                String output = reader.GetResult();
                String [] buffer = output.split("\r\n");
                for(String item : buffer) {
                	if(item.contains("\\Device")) {
                		String worker = item.toLowerCase(Locale.US);
                        int offset = worker.indexOf("vid_0b00");
                        if (offset != -1) {
                        	offset = worker.indexOf("pid_00");
                        	if (offset != -1) {
                        		int index = offset + "PID_".length();
                        		devicePID = item.substring(index, index + 4);
                        	}
                        }
                	}
                }
    		} catch (Exception e) {
    			return "Exception Occurred {} " + e.getMessage();
    		}
        	
        	return devicePID;
        }
    }
}
