
package semlink.apps.gu;

import static java.lang.System.out;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import semlink.apps.VerbNetAnalyzer;
import semlink.grouping.Grouping;
import semlink.grouping.GroupingFile;
import semlink.grouping.GroupingMap;
import semlink.grouping.GroupingParser;
import semlink.util.XMLElementEditor;
import semlink.wordnet.WordNet;
import slflixer.cc.util.mm.MembershipMap;

public class GroupingAnalyzer extends VerbNetAnalyzer {

    private MembershipMap<String, String> vnMembersWithoutWN = new MembershipMap<String, String>();
    private MembershipMap<String, String> vnMembersWithGf = new MembershipMap<String, String>();
    private MembershipMap<String, String> vnMembersWithoutGf = new MembershipMap<String, String>();
    private int vnGrpAttrChanged = 0;
    private int vnGrpAttrBlank = 0;
    private int vnGrpAttrM1Blank = 0;
    private int vnGrpAttrM2Blank = 0;
    private int vnGrpAttrM1M2Blank = 0;
    private MembershipMap<String, String> totalMemberLines = new MembershipMap<String, String> ();

    private MembershipMap<String, String> wnskAll = new MembershipMap<String, String>();
    private MembershipMap<String, String> wnskUncertain = new MembershipMap<String, String>();
    private MembershipMap<String, String> grprAll = new MembershipMap<String, String>();
    private MembershipMap<String, String> memBroken = new MembershipMap<String, String>();

    private MembershipMap<String, String> classesUnknown = new MembershipMap<String, String>();
    private MembershipMap<String, String> wnsnUnknown = new MembershipMap<String, String>();
    private MembershipMap<String, String> lemmasUnknown = new MembershipMap<String, String>();

    private MembershipMap<String, String> differentMemberLines = new MembershipMap<String, String>();
    private MembershipMap<String, String> bothMethodsBlank = new MembershipMap<String, String>();
    private int grpSenseGroups = 0;

    private File grpDir;
    private GroupingMap gMap;
    private File wnKeys;
    private File[] grpXmlFiles;
    private boolean suppressEx;
    private String method;

    public GroupingAnalyzer(File iDir, File oDir, File[] grp, File wn, boolean suppr, String m) {
        super(iDir, oDir);
        wnKeys = wn;
        grpXmlFiles = grp;
        suppressEx = suppr;
        method = m;
    }

    @Override
    public void preAnalyze() {
        readWNInfo(inputFiles, wnKeys);
        grpDir = grpXmlFiles[0].getParentFile();
        readGrpInfo(grpDir);

        boolean doOutput = (outputDir != null);
        String action = (doOutput) ? "Update" : "Analyze";
        out.println("  --> Step 6: " + action + " VN grouping references...");
        out.print( "         ");
    }

    @Override
    public void postAnalyze() {
        setGrpStats();
        printReport();
    }

    private void readWNInfo(File[] vnXmlFiles, File wnKeys) {
        System.out.println("  --> Step 3: VerbNet pre-scan for just sense keys...");
        WordNet.preScan(vnXmlFiles);
        System.out.println("  --> Step 4: Scan index.sense for required sense numbers...");
        WordNet.loadSenseNumbers(wnKeys);
    }

    private void readGrpInfo(File grpDir) {
        System.out.println("  --> Step 5: Parse grouping files...");
        gMap = GroupingParser.parseFiles(grpDir);
    }

    private void setGrpStats() {
        for(GroupingFile gf : gMap.values()) {
            grpSenseGroups += gf.groupings.size();
        }

        for(String wn : gMap.uniqueWnNums.keySet()) {
            try {
                Integer.parseInt(wn);
            }
            catch(Exception e) {
                TreeMap<GroupingFile, Integer> tree = gMap.uniqueWnNums.get(wn);
                for(GroupingFile g : tree.keySet()) {
                    wnsnUnknown.addMembership(wn, g.file.getName());
                }
            }
        }

        for(GroupingFile value : gMap.values()) {
            if(value.isLemmaError()) {
                lemmasUnknown.addMembership(value.lemma, value.file.getName());
            }
        }

        for(String cls : gMap.uniqueVnClasses.keySet()) {
            if(!classNames.get("all").containsKey(cls)) {
                TreeMap<GroupingFile, Integer> tree = gMap.uniqueVnClasses.get(cls);
                for(GroupingFile g : tree.keySet()) {
                    classesUnknown.addMembership(cls, g.file.getName());
                }
            }
        }
    }


    @Override
    public String analyzeLine(File file, String line, int lineNum) {
        String fileName = file.getName();

        super.analyzeLine(file, line, lineNum);

        // Look for member element.
        int memStart = line.indexOf("<MEMBER ");

        // If the line is a member line...
        if(memStart != -1) {
            String verb = XMLElementEditor.getValueOfAttribute(line, "name");

            if(verb == null) {
                System.err.println("ERROR: MEMBER line without name= attribute (" + curClass + ")");
                return line;
            }

            totalMemberLines.addMembership(verb, fileName);
            int end = line.indexOf("/>");

            if(end == -1) {
                memBroken.addMembership(line.trim(), fileName);
                return line;
            }

            String wn = XMLElementEditor.getValueOfAttribute(line, "wn");

            String[] keys;
            if(wn == null || wn.trim().equals("")) {
                keys = new String[0];
                vnMembersWithoutWN.addMembership(verb, fileName);
            } else {
                keys = wn.trim().split( "  *" );
            }

            for(String k : keys) {
                if(k.startsWith("?")) {
                    wnskUncertain.addMembership(k, fileName);
                    wnskAll.addMembership(k.substring(1), fileName);
                } else {
                    wnskAll.addMembership(k, fileName);
                }
            }

            String grpOriginal = XMLElementEditor.getValueOfAttribute(line, "grouping");

            String grps[];
            if(grpOriginal == null || grpOriginal.trim().equals("")) {
                grps = new String[0];
            } else {
                grps = grpOriginal.trim().split( "  *" );
            }

            for(String g : grps) {
                grprAll.addMembership(g, fileName);
            }

            String newGroupingStr = "";

            GroupingFile gfForVerb = gMap.get(verb);

            if(gfForVerb != null) {

                //////////////
                // METHOD 1: If a sense grouping for a verb verb-v contains a link
                // to VerbNet, then the member in that VerbNet class gets a link
                // back to that sense grouping in that grouping file.
                //////////////

                String newGroupings1 = "";

                for(Grouping gg : gfForVerb.groupings) {
                    if(gg.vnClasses != null) {
                        for(String vnc : gg.vnClasses) {
                            if(vnc.equals(curClass)) {
                                newGroupings1 += verb + "." + gg.grpSenseNum + " ";
                                break;
                            }
                        }
                    }
                }

                newGroupings1 = newGroupings1.trim();

                if(newGroupings1.equals("")) {
                    vnGrpAttrM1Blank++;
                }

                //////////////
                // METHOD 2 //
                //////////////

                String newGroupings2 = "";

                List<String> wnsns = new ArrayList<String>();
                for(String k : keys) {
                    if(k.startsWith("?")) {
                        k = k.substring(1);
                    }

                    String wnsn = WordNet.getSenseNumber(k);

                    if(!wnsn.equals("?")) {
                        wnsns.add(wnsn);
                    }
                }

                for(Grouping gg : gfForVerb.groupings) {
                    if(gg.wnSenseNums != null) {
                        for(String ww : gg.wnSenseNums) {
                            if(wnsns.contains(ww)) {
                                newGroupings2 += verb + "." + gg.grpSenseNum + " ";
                                break;
                            }
                        }
                    }
                }

                newGroupings2 = newGroupings2.trim();

                if(newGroupings2.equals("")) {
                    vnGrpAttrM2Blank++;
                }

                if(newGroupings1.equals("") && newGroupings2.equals("")) {
                    vnGrpAttrM1M2Blank++;
                    bothMethodsBlank.addMembership(verb, fileName);
                }

                if(!newGroupings1.equals(newGroupings2)) {
                    differentMemberLines.addMembership(verb + ",M1{" + newGroupings1 + "},M2{" + newGroupings2 + "}", fileName);
                }

                if(method == null) {
                    method = "m2m1";
                }

                if(method.equals("m1")) {
                    newGroupingStr = newGroupings1;
                } else if(method.equals("m2")) {
                    newGroupingStr = newGroupings2;
                } else if(method.equals("m1m2")) {
                    newGroupingStr = newGroupings1;
                    if(newGroupingStr.equals("")) {
                        newGroupingStr = newGroupings2;
                    }
                } else if(method.equals("m2m1")) {
                    newGroupingStr = newGroupings2;
                    if(newGroupingStr.equals("")) {
                        newGroupingStr = newGroupings1;
                    }
                }

                vnMembersWithGf.addMembership(verb, fileName);
            } else {
                vnMembersWithoutGf.addMembership(verb, fileName);
            }

            if(!newGroupingStr.equals(grpOriginal)) {
                vnGrpAttrChanged++;
            }

            if(newGroupingStr.equals("")) {
                vnGrpAttrBlank++;
            }

            line = XMLElementEditor.replaceValueOfAttribute(line, "grouping", newGroupingStr);
        }

        return line;
    }

    @Override
    public void handleException(File file, Exception e) {
        e.printStackTrace();
    }

    public void printReport() {

        // These could happen if there is no ending tag for a MEMBER /> because
        // the code in changeLine will just skip the line, and WordNet.class
        // is using real XML parsing to get the members so the broken lines
        // don't affect it, and it still gets counted in its totals.
        if(wnskAll.getAllMemberships() != WordNet.totalVnWNSK) {
            //System.err.println("ERROR: allWNSK.getAllMemberships() != WordNet.totalWNSK");
        }
        if(wnskUncertain.getAllMemberships() != WordNet.numberUncertain) {
            //System.err.println("ERROR: uncertainKeys.getAllMemberships() != WordNet.numberUncertain");
        }

        // Ignore those verbs with blank 'wn' attributes that don't have
        // any corresponding WN sense keys in WN.
        List<String> verbsNotInWN = new ArrayList<String>();
        for(String verb : vnMembersWithoutWN.keySet()) {
            if(!WordNet.getAllVerbs().containsKey(verb)) {
                verbsNotInWN.add(verb);
            }
        }
        for(String verb : verbsNotInWN) {
            vnMembersWithoutWN.remove(verb);
        }

        out.println("");
        out.println("<<< Report Begin >>>");
        out.println("");
        out.println("(WNSK = WordNet Sense Keys (i.e. verb%2.XX.XX))");
        out.println("(WNSN = WordNet Sense Numbers)");
        out.println("(GRPR = Grouping References (i.e. verb.01))");
        out.println("(GRPA = Grouping Attributes (i.e. grouping=\"...\"))");
        out.println("");
        out.println("1. VerbNet Statistics Only");
        out.println("------------------------------------------------------------");
        out.println("| This section describes statistics about just VerbNet,    |");
        out.println("| its WordNet sense keys, and its grouping references.     |");
        out.println("------------------------------------------------------------");
        out.printf("1.A. Total files (.xml)                                %5d%n", inputFiles.length);
        out.printf("1.B. Total WNSK                                        %5d%n", wnskAll.getAllMemberships());
        out.printf("1.C. Unique WNSK                                       %5d%n", wnskAll.getGroupCount());
        out.printf("1.D. Total WNSK with uncertainty*                      %5d%n", wnskUncertain.getAllMemberships());
        out.printf("1.E. Unique WNSK with uncertainty*                     %5d%n", wnskUncertain.getGroupCount());
        out.printf("1.F. Unique invalid WNSK: Could not parse              %5d%n", WordNet.getWNSNParseErrors().getGroupCount());
        out.printf("1.G. Unique invalid WNSK: Not in WordNet index.sense   %5d%n", WordNet.getNonExistentWNSNErrors().getGroupCount());
        out.printf("1.H. Total MEMBERs with blank 'wn' attribute#          %5d%n", vnMembersWithoutWN.getAllMemberships());
        out.printf("1.I. Unique MEMBERs with blank 'wn' attribute#         %5d%n", vnMembersWithoutWN.getGroupCount());
        out.printf("1.J. Total GRPR                                        %5d%n", grprAll.getAllMemberships());
        out.printf("1.K. Unique GRPR                                       %5d%n", grprAll.getGroupCount());
        out.printf("1.L. Total MEMBER lines                                %5d%n", totalMemberLines.getAllMemberships());
        out.printf("1.M. Broken MEMBER lines^                              %5d%n", memBroken.getAllMemberships());
        out.printf("1.N. Unique verbs                                      %5d%n", totalMemberLines.getGroupCount());
        out.println("");
        out.println("* Uncertain WNSK are those preceded by a ? (i.e. ?run%2:30:00).");
        out.println("# Verbs which do not exist in WN are not included in these values.");
        out.println("^ Currently the GroupingUpdater cannot process <MEMBER .../> lines");
        out.println("  which are not contained on a single line.");
        out.println("");
        out.println("");

        out.println("2. WordNet Statistics Only");
        out.println("------------------------------------------------------------");
        out.println("| This section describes statistics about the WordNet      |");
        out.println("| index.sense file.                                        |");
        out.println("------------------------------------------------------------");
        out.printf("2.A. Total WNSK                                       %6d%n", WordNet.totalIndexSenseWNSK);
        out.printf("2.B. Total verb WNSK*                                  %5d%n", WordNet.getAllVerbs().getAllMemberships());
        out.printf("2.C. Unique verbs                                      %5d%n", WordNet.getAllVerbs().getGroupCount());
        out.println("");
        out.println("* Sense keys with a 2 after the % are for verbs (i.e. word%2.XX.XX).");
        out.println("");
        out.println("");

        out.println("3. Grouping Statistics Only");
        out.println("------------------------------------------------------------");
        out.println("| This section describes statistics about just the         |");
        out.println("| OntoNotes Sense Grouping files.                          |");
        out.println("------------------------------------------------------------");
        out.printf("3.A. Total files (-v.xml)                              %5d%n", gMap.size());
        out.printf("3.B. Total sense groups                                %5d%n", grpSenseGroups);
        out.printf("3.C. Total VN classes & subclasses                     %5d%n", gMap.uniqueVnClasses.getAllMemberships());
        out.printf("3.D. Unique VN classes & subclasses                    %5d%n", gMap.uniqueVnClasses.getGroupCount());
        out.printf("3.E. Unique invalid classes/subclasses: Not in VN      %5d%n", classesUnknown.getGroupCount());
        out.printf("3.F. Total WNSN                                        %5d%n", gMap.uniqueWnNums.getAllMemberships());
        out.printf("3.G. Unique WNSN                                       %5d%n", gMap.uniqueWnNums.getGroupCount());
        out.printf("3.H. Unique invalid WNSN                               %5d%n", wnsnUnknown.getGroupCount());
        out.printf("3.I. Unique invalid lemma attributes                   %5d%n", lemmasUnknown.getGroupCount());
        out.println("");
        out.println("");

        out.println("4. Update Statistics");
        out.println("------------------------------------------------------------");
        out.println("| This section describes the statistics about the updating |");
        out.println("| of the grouping references within VN.                    |");
        out.println("------------------------------------------------------------");
        out.printf("4.A. Total MEMBERs w/ corresponding grouping file      %5d%n", vnMembersWithGf.getAllMemberships());
        out.printf("4.B. Total MEMBERs w/o corresponding grouping file     %5d%n", vnMembersWithoutGf.getAllMemberships());
        out.printf("4.C. Unique MEMBERs w/ corresponding grouping file     %5d%n", vnMembersWithGf.getGroupCount());
        out.printf("4.D. Unique MEMBERs w/o corresponding grouping file    %5d%n", vnMembersWithoutGf.getGroupCount());
        out.printf("4.E. GRPA changed                                      %5d%n", vnGrpAttrChanged);
        out.printf("4.F. Total updated lines differing between methods*    %5d%n", differentMemberLines.getAllMemberships());
        out.printf("4.G. GRPA set to blank (includes those already blank)  %5d%n", vnGrpAttrBlank);
        out.printf("4.H. Method 1 resulted in blank GRPA#                  %5d%n", vnGrpAttrM1Blank);
        out.printf("4.I. Method 2 resulted in blank GRPA#                  %5d%n", vnGrpAttrM2Blank);
        out.printf("4.J. Times both methods returned blank GRPA#           %5d%n", vnGrpAttrM1M2Blank);
        out.println("");
        out.println("* Method 1: Use <vn>...</vn> links in grouping files to decide");
        out.println("  whether a grouping reference is added to a VerbNet MEMBER line.");
        out.println("  Method 2: Use <wn>...</wn> links in grouping files to decide");
        out.println("  whether a grouping reference is added to a VerbNet MEMBER line.");
        out.println("  This metric counts the number of times that the two methods would");
        out.println("  have chosen different values for a MEMBER's 'grouping' attribute.");
        out.println("* IMPT: Method 2 is the method used to update the grouping attributes");
        out.println("  but if Method 2 results in a blank GRPA, then Method 1 is used for");
        out.println("  those MEMBERs (which also could result in a blank GRPA).");
        out.println("* NOTE: If a method results in a blank GRPA it means that the method");
        out.println("  could not find a link between the MEMBER and any sense grouping.");
        out.println("# Only counts those instances when a corresponding grouping file");
        out.println("  actually existed for the MEMBER.  If no grouping file existed");
        out.println("  for the given MEMBER, then these metrics are not affected.");
        out.println("");
        out.println("");

        out.println("5. Exception Lists");
        out.println("------------------------------------------------------------");
        out.println("| This section contains lists of possibly erroneous input  |");
        out.println("| or results that require inspection.                      |");
        out.println("------------------------------------------------------------");
        printList("5.A. Invalid WNSK (1.F from above)", WordNet.getWNSNParseErrors());
        printList("5.B. Invalid WNSK (1.G from above)", WordNet.getNonExistentWNSNErrors());
        printList("5.C. MEMBERs with blank 'wn' attribute (1.I from above)", vnMembersWithoutWN);
        printList("5.D. Broken MEMBER lines (1.M from above)", memBroken);
        printList("5.E. Invalid VN classes/subclasses (3.E from above)", classesUnknown);
        printList("5.F. Invalid WNSN (3.H from above)", wnsnUnknown);
        printList("5.G. Invalid lemma attributes (3.I from above)", lemmasUnknown);
        printList("5.H. Methods differ on updated lines (4.F from above)", differentMemberLines);
        printList("5.I. Both methods returned blank GRPA (4.J from above)", bothMethodsBlank);

        out.println("");
        out.println("<<< Report End >>>");
    }

    /*
    private void printList(String title, Collection<?> list) {
        out.println(title + " [total items: " + list.size() + "]");

        if(suppressEx) {
            out.println("  [exception list output suppressed]");
            return;
        }

        if(list.size() == 0) {
            out.println("  [this list empty]");
        } else {
            for(Object o : list) {
                out.println(" - " + o);
            }
        }
    }
    */

    private void printList(String title, MembershipMap<String, String> map) {
        out.println(title + " [total unique items: " + map.getGroupCount() + ", total items: " + map.getAllMemberships() + "]");

        if(suppressEx) {
            out.println("  [exception list output suppressed]");
            return;
        }

        if(map.getGroupCount() == 0) {
            out.println("  [this list empty]");
        } else {

            String ret = "";
            for(String key : map.keySet()) {
                String s = map.toShortString(key);
                if(s.length() > 100) {
                    s = s.substring(0, 100) + "...";
                }
                ret += " - " + s + "\n";
            }
            out.print(ret);
        }
    }
}
