
package semlink.util.analyzer;

import java.io.File;

import semlink.util.ProgressManager;

public class TextFilesAnalyzer {
    protected File[] inputFiles;
    protected LineAnalyzer analyzer;
    protected File[] outputFiles;
    protected AnalyzerErrorHandler errorHandler;
    protected boolean quitAfterError;

    public TextFilesAnalyzer(File[] iFiles, LineAnalyzer alyz) {
        inputFiles = iFiles;
        analyzer = alyz;
    }

    public TextFilesAnalyzer(File[] iFiles, LineAnalyzer alyz, File[] oFiles) {
        inputFiles = iFiles;
        analyzer = alyz;
        outputFiles = oFiles;
    }

    public boolean process() {
        try {
            ProgressManager.reset();
            int f = 0;
            for(File inputFile : inputFiles) {
                ProgressManager.next(f, inputFiles.length);
                File outputFile = null;
                if(outputFiles != null) {
                    outputFile = outputFiles[f];
                }
                TextFileAnalyzer tfa = new TextFileAnalyzer(inputFile, analyzer, outputFile);
                tfa.setErrorHandler(new AnalyzerErrorHandler() {
                    public void handleException(File file, Exception e) {
                        if(errorHandler == null) {
                            e.printStackTrace();
                        } else {
                            errorHandler.handleException(file, e);
                        }
                    }
                });
                if(!tfa.process() && quitAfterError) {
                    return false;
                }
                f++;
            }
            return true;
        } catch(Exception e) {
            errorHandler.handleException(null, e);
            return false;
        } finally {
            ProgressManager.finish();
        }
    }
    public void setErrorHandler(AnalyzerErrorHandler eHandler) {
        errorHandler = eHandler;
    }
    public void setQuitAfterError(boolean quit) {
        quitAfterError = quit;
    }
}

