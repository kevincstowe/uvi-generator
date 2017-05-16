package semlink.grouping;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;

import semlink.util.ProgressManager;
import slflixer.cc.util.StringUtil;

public class GroupingParser {

	   public static GroupingMap parseFiles(File groupingsDir) {
		   
		   GroupingMap groupingMap = new GroupingMap();
	      
	      File[] files = groupingsDir.listFiles(new FileFilter() {
	         public boolean accept(File file) {
	            return file.getAbsolutePath().endsWith("-v.xml");
	         }
	      });
	      
          System.out.print( "         ");
          ProgressManager.reset();
	     
          int f = 0;
	      for(File file : files) {
	    	  
	    	  ProgressManager.next(f++, files.length);
	      
	         GroupingFile groupingFile = new GroupingFile();
	         groupingFile.file = file;

	         String line = null;
	         try {
	         
	            BufferedReader reader = new BufferedReader(new FileReader(file));
	            
	            Grouping currentGrouping = null;
	            while((line = reader.readLine()) != null) { 
	            	if(isSenseLemma(line)) {
	            		groupingFile.lemma = parseSenseLemma(line);
	            	} else if(isStartGrouping(line)) {
	            		currentGrouping = new Grouping();
	            		currentGrouping.grpSenseNum = parseGroupingLine(line);
	            		if(currentGrouping.grpSenseNum.length() == 1) {
	            		    currentGrouping.grpSenseNum = "0" + currentGrouping.grpSenseNum;
	            		}
	            		groupingFile.groupings.add(currentGrouping);
	            	}  else if(isVnClassLine(line)) {
	            		String vn = parseVnClassLine(line);
	            		if(!vn.equals("")) {
	            			currentGrouping.vnClasses = vn.split(" *, *");
	            		}
	            	}  else if(isWnSenseNums(line)) {
	            		String wn = parseWnSenseNums(line);
	            		if(!wn.equals("")) {
		            		currentGrouping.wnSenseNums  = wn.split(" *, *");
	            		}
	            	}
	            }
	            
	            reader.close();
	            
	         } catch(Exception e) {
	        	 System.err.println(line);
	        	 e.printStackTrace();
	         }
	         
	         groupingMap.put(StringUtil.removeEnd(file.getName(), "-v.xml"), groupingFile);

	      }
	         
	         ProgressManager.finish();
	      
	      return groupingMap;
	   }
	   

	   public static boolean isSenseLemma(String line) {
		   return line.contains("<inventory lemma=");
	   }
	   public static String parseSenseLemma(String line) {
		   int x = line.indexOf("\"");
		   int y = line.indexOf("\"", x + 3);
		   return line.substring(x + 1, y).trim();
	   }
	   
	   public static boolean isStartGrouping(String line) {
		   return line.contains("<sense ");
	   }
	   public static boolean isVnClassLine(String line) {
		   return line.contains("<vn>") && line.contains("</vn>");
	   }
	   public static boolean isWnSenseNums(String line) {
		   return line.contains("<wn version=") && line.contains("</wn>") && !line.contains("lemma");
	   }
	   
	   public static String parseGroupingLine(String line) {
		   int x = line.indexOf("n=\"");
		   int y = line.indexOf("\"", x + 3);
		   return line.substring(x + 3, y).trim();
	   }
	   public static String parseVnClassLine(String line) {
		   int vn= line.indexOf("<vn>");
		   int svn= line.indexOf("</vn>");
		   return line.substring(vn + 4, svn).trim();
	   }
	   public static String parseWnSenseNums(String line) {
		   int wn= line.indexOf(">");
		   int swn= line.indexOf("</wn>");
		   return line.substring(wn + 1, swn).trim();
	   }
}
