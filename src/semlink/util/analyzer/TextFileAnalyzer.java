
package semlink.util.analyzer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;

public class TextFileAnalyzer {
    protected File inputFile;
    protected LineAnalyzer analyzer;
    protected File outputFile;
    protected AnalyzerErrorHandler errorHandler;

    public TextFileAnalyzer(File iFile, LineAnalyzer alyz) {
        inputFile = iFile;
        analyzer = alyz;
    }

    public TextFileAnalyzer(File iFile, LineAnalyzer alyz, File oFile) {
        inputFile = iFile;
        analyzer = alyz;
        outputFile = oFile;
    }

    public boolean process() {
        try {
            BufferedReader in = new BufferedReader(new FileReader(inputFile));
            PrintWriter pw = null;
            if(outputFile != null) {
                pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            }
            String line;
            int lineNum = 1;
            while((line = in.readLine()) != null) {
                line = analyzer.analyzeLine(inputFile, line, lineNum++);
                if(pw != null) {
                    pw.println(line);
                }
            }
            if(pw != null) {
                pw.close();
            }
            in.close();
            return true;
        } catch(Exception e) {
            if(errorHandler == null) {
                e.printStackTrace();
            } else {
                errorHandler.handleException(inputFile, e);
            }
            return false;
        }
    }
    public void setErrorHandler(AnalyzerErrorHandler eHandler) {
        errorHandler = eHandler;
    }
}

