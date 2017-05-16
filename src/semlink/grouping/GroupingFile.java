package semlink.grouping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import slflixer.cc.util.StringUtil;


public class GroupingFile implements Comparable<GroupingFile> {
    public File file;
    public List<Grouping> groupings = new ArrayList<Grouping>();
    public String lemma;

    public boolean isLemmaError() {
        return !(lemma + ".xml").equals(file.getName());
    }

    @Override
    public String toString() {
        String s =
            StringUtil.removeEnd(StringUtil.removeEnd(file.getName(), "-v.html"), "-v.xml") + " [" +
                            groupings.size() + " groups]";
        return s;
    }

    public String toLongString() {
        String s = file.getAbsolutePath() + " (" + lemma + ")\n";
        for(Grouping g : groupings) {
            s += g + "\n";
        }
        return s;
    }

    @Override
    public boolean equals(Object o) {
        if(o == null) {
            return false;
        }
        GroupingFile gf = (GroupingFile) o;
        if(!StringUtil.removeEnd(StringUtil.removeEnd(file.getName(), "html"), "xml").equals(
            StringUtil.removeEnd(StringUtil.removeEnd(gf.file.getName(), "html"), "xml"))) {
            return false;
        }
        if(!lemma.equals(gf.lemma)) {
            return false;
        }
        if(groupings.size() != gf.groupings.size()) {
            return false;
        }
        for(int g = 0; g < groupings.size(); g++) {
            if(!groupings.get(g).equals(gf.groupings.get(g))) {
                return false;
            }
        }

        return true;
    }

    public int compareTo(GroupingFile o) {
        return file.getAbsolutePath().compareTo(o.file.getAbsolutePath());
    }
}
