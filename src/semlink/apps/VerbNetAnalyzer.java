
package semlink.apps;

import java.io.File;
import java.io.FileFilter;

import semlink.util.Sorter;
import semlink.util.XMLElementEditor;
import semlink.util.analyzer.AnalyzerErrorHandler;
import semlink.util.analyzer.LineAnalyzer;
import semlink.util.analyzer.TextFilesAnalyzer;
import slflixer.cc.util.mm.MembershipMap;

public abstract class VerbNetAnalyzer implements LineAnalyzer, AnalyzerErrorHandler {
    protected File inputDir;
    protected File outputDir;
    protected File[] inputFiles;
    protected File[] outputFiles;

    public VerbNetAnalyzer(File iDir, File oDir) {
        inputDir = iDir;
        outputDir = oDir;
    }

    protected FileFilter getFilter() {
        FileFilter filter = new FileFilter() {
            public boolean accept(File file) {
                String nm = file.getName();
                if(nm.toLowerCase().endsWith(".xml")) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        return filter;
    }
    
    public void update() {
    	initializeFiles();
    	preAnalyze();
        TextFilesAnalyzer tfa = new TextFilesAnalyzer(inputFiles, this, outputFiles);
        tfa.setErrorHandler(this);
        tfa.process();
        postAnalyze();
    }
   
    protected void initializeFiles() {
        inputFiles = inputDir.listFiles(getFilter());
        Sorter.sortFileArray(inputFiles);
        outputFiles = null;
        if(outputDir != null) {
            outputFiles = new File[inputFiles.length];
            for(int f = 0; f < inputFiles.length; f++) {
                outputFiles[f] = new File(outputDir, inputFiles[f].getName());
            }
        }
    }
    
    public abstract void preAnalyze();
    public abstract void postAnalyze();

    // Every VerbNetUpdater keeps track of the current class
    // and all classes visited thus far.
    protected String curClass;
    protected MembershipMap<String, String> classNames = new MembershipMap<String, String>();

    public String analyzeLine(File file, String line, int lineNum) {

        // Look for class/subclass element.
        int clsStart = line.indexOf("<VNCLASS ");
        int subclsStart = line.indexOf("<VNSUBCLASS ");

        if(clsStart != -1 || subclsStart != -1) {
            curClass = XMLElementEditor.getValueOfAttribute(line, "ID");
            classNames.addMembership("all", curClass);
            if(clsStart != -1) {
                classNames.addMembership("toplevel", curClass);
            } else {
                classNames.addMembership("sub", curClass);
            }
        }

        return line;
    }
    public abstract void handleException(File file, Exception e);
}
