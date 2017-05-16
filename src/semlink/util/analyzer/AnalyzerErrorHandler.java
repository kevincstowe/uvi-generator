
package semlink.util.analyzer;

import java.io.File;

public interface AnalyzerErrorHandler {
    public void handleException(File file, Exception e);
}
