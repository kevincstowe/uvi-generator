
package semlink.util;

public class XMLElementEditor {

    public static String leftTrim(String str) {
        int start = 0;
        while(start < str.length() && str.charAt(start) == ' ') {
            start++;
        }
        return (start > 0) ? str.substring(start) : str;
    }

    public static String rightTrim(String str) {
        int len = str.length();
        while(len > 0 && str.charAt(len - 1) == ' ') {
            len--;
        }
        return (len > 0) ? str.substring(0, len) : str;
    }

    public static boolean existsAttribute(String line, String attr) {
        int aStart = line.indexOf(" " + attr + "=");
        return aStart != -1;
    }

    public static String getValueOfAttribute(String line, String attr) {
        int aStart = line.indexOf(" " + attr + "=");

        if(aStart == -1) {
            return null;
        }

        aStart += attr.length() + 3;

        int aEnd = line.indexOf("\"", aStart);

        return line.substring(aStart, aEnd);
    }

    public static String replaceValueOfAttribute(String line, String attr, String value) {
        int aStart = line.indexOf(" " + attr + "=");

        if(aStart == -1) {
            int end = line.indexOf("/>");
            if(end == -1) {
                end = line.indexOf(">");
                if( end == -1) {
                    System.err.print("CANNOT FIND > !!!");
                } else {
                    line = rightTrim(line.substring(0, end)) + " " + attr + "=\"" + value + "\">" + line.substring(end + 1, line.length());
                }
            } else {
                line = rightTrim(line.substring(0, end)) + " " + attr + "=\"" + value + "\"/>" + line.substring(end + 2, line.length());
            }
        } else {
            aStart += attr.length() + 3;
            int aEnd = line.indexOf("\"", aStart);
             line = line.substring(0, aStart) + value + line.substring(aEnd);
        }

        return line;
    }

    public static void main(String[] args) {
        System.out.println("["+replaceValueOfAttribute("  <MEMBER id=\"derek\" name=\"trumbo\"/>", "name", "xxxxx")+"]");

        System.out.println("["+leftTrim("")+"]");
        System.out.println("["+leftTrim("  ")+"]");
        System.out.println("["+leftTrim("aaa ")+"]");
        System.out.println("["+leftTrim("aaa  ")+"]");
        System.out.println("["+leftTrim(" aaa")+"]");
        System.out.println("["+leftTrim("  aaa")+"]");
        System.out.println("["+leftTrim(" aaa ")+"]");
        System.out.println("["+leftTrim("  aaa  ")+"]");

        System.out.println("["+rightTrim("")+"]");
        System.out.println("["+rightTrim("  ")+"]");
        System.out.println("["+rightTrim("aaa ")+"]");
        System.out.println("["+rightTrim("aaa  ")+"]");
        System.out.println("["+rightTrim(" aaa")+"]");
        System.out.println("["+rightTrim("  aaa")+"]");
        System.out.println("["+rightTrim(" aaa ")+"]");
        System.out.println("["+rightTrim("  aaa  ")+"]");
    }
}
