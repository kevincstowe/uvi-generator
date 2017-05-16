
package semlink.util.analyzer;

import java.io.File;

public interface LineAnalyzer {
    public String analyzeLine(File file, String line, int lineNum);
}
