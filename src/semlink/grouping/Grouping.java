package semlink.grouping;

import java.util.Arrays;

public class Grouping {
    public String grpSenseNum;
    public String[] vnClasses;
    public String[] wnSenseNums;
    
    public String toString() {
  	  return "Grouping: " + grpSenseNum  + ", vn=" + Arrays.toString(vnClasses) + ", wn=" + Arrays.toString(wnSenseNums);
    }
    
	@Override
    public boolean equals(Object o) {
		if(o == null ) {
			return false;
		}
    	Grouping g = (Grouping) o;
    	if(!equals2(grpSenseNum, g.grpSenseNum)) { 
    		return false;
    	}
    	if(!equalsArray(vnClasses, g.vnClasses)) { 
    		return false;
    	}
    	if(!equalsArray(wnSenseNums, g.wnSenseNums)) { 
    		return false;
    	}
    	
    	return true; 
    }
    
    public static boolean equals2(Object o1, Object o2) {
    	if(o1 == null && o2 == null) {
    		return true;
    	}
    	if(o1 == null || o2 == null) {
    		return false;
    	}
    	return o1.equals(o2);
    }
    public static boolean equalsArray(String[] o1, String[] o2) {
    	if(o1 == null && o2 == null) {
    		return true;
    	}
    	if(o1 == null || o2 == null) {
    		return false;
    	}
    	return Arrays.equals(o1, o2);
    }
}
