// ////////////////////
// Grouping Updater //
// Derek Trumbo //
// ////////////////////

package semlink.apps.gu;

import static java.lang.System.err;
import static java.lang.System.out;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import semlink.util.Parser;
import slflixer.cc.util.CommandLineParser;
import slflixer.cc.util.mm.MembershipMap;

/**
 * This class facilitates the updating of the OntoNotes Sense Grouping references within the VerbNet
 * XML files. The default action of this program is to display statistics associated with the
 * desired translation (doesn't change anything or create new files). If you would like the program
 * to generate a new set of XML files with the grouping references updated, you must supply the -n
 * (--new-xml) flag and provide an XML output directory. <BR>
 * <BR>
 * This program accepts as its first three arguments, the VerbNet XML input directory, the groupings
 * XML input directory, and the WordNet 'index.sense' file. <BR>
 * <BR>
 * The statistics report is always sent to standard out.
 */
public class GroupingUpdater {

    private static class Options {

        // Parser
        private static CommandLineParser clParser;

        // Constants
        private static final int TOTAL_NON_FLAGS = 4;

        // Flags
        private static boolean flHelp;
        private static boolean flNewXML;
        private static boolean flOverwrite;
        private static boolean flNoLists;
        private static String method;

        // Paths
        private static File vnIn;
        private static File grpDir;
        private static File wnKeys;
        private static File vnOut;

        // Derived
        private static File[] vnXmlFiles;
        private static File[] grpXmlFiles;
    }

    /////////////////
    // Constructor //
    /////////////////

    private GroupingUpdater() {}

    //////////
    // Main //
    //////////

    public static void main(String args[]) {

        try {

            analyzeArguments(args);
            printStartInfo();
            verifyXMLSyntax();
            updateGroupings();

        } catch(UserWantsHelpMessage uwhm) {
            printUsage();

        } catch(InvalidCommandLineArgumentException iclae) {
            err.println("ERROR: " + iclae.getMessage());
            printUsage();

        } catch(CommandLineParser.OptionException oe) {
            err.println("ERROR: " + oe.getMessage());
            printUsage();

        } catch(InvalidPathException ide) {
            err.println("ERROR: " + ide.getMessage());

        } catch(BadXMLException bxe) {
            err.println("ERROR: " + bxe.getMessage());

        } catch(Exception e) {
            err.println("ERROR: [Generic/main] " + e.getMessage());
        }
    }

    //////////////////////////
    // Informational Output //
    //////////////////////////

    private static void printUsage() {
        out.println("GroupingUpdater for VerbNet");
        out.println("Usage: vn_gu [flags] <xml-input-dir> <grouping-dir> <index.sense-file> [<xml-output-dir>]");
        out.println("Flags:");
        out.print(Options.clParser.listOptionDescriptions(80, 20));
    }

    private static void printStartInfo() {

        String dt = new Date().toString();

        Map<CommandLineParser.Option, Object[]> m = Options.clParser.getOptionMap();
        String af = "";
        if(m.size() == 0) {
            af = "(none)";
        } else {
            for(CommandLineParser.Option opt : m.keySet()) {
                String value = Options.clParser.getOptionValue(opt).toString();
                af += opt;
                af += "=" + value;
                af += " ";
            }
        }

        String vOut;
        if(Options.vnOut == null) {
            vOut = "(none)";
        } else {
            vOut = filePath(Options.vnOut);
        }

        // Output the banner.
        out.println("GroupingUpdater for VerbNet");
        out.println("====================================================");
        out.println("VerbNet XML Input Directory:  " + filePath(Options.vnIn));
        out.println("Grouping XML Input Directory: " + filePath(Options.grpDir));
        out.println("WordNet index.sense file:     " + filePath(Options.wnKeys));
        out.println("VerbNet XML Output Directory: " + vOut);
        out.println("Flags:                        " + af);
        out.println("Executed On:                  " + dt);

        if(Options.vnOut == null) {
            out.println("");
            out.println("NOTE: New VerbNet XML files not requested.  Just a report will be printed.");
        }

        out.println("====================================================");
        out.println();
        out.println("Grouping Update Process");
        out.println("-----------------------");
    }

    ////////////////////////////
    // Command-Line Arguments //
    ////////////////////////////

    private static void analyzeArguments(String[] args) throws InvalidPathException,
                                                        UserWantsHelpMessage,
                                                        InvalidCommandLineArgumentException,
                                                        CommandLineParser.OptionException {

        CommandLineParser clParser = Options.clParser = new CommandLineParser();

        CommandLineParser.Option optHelp   = clParser.addBooleanOption('?', "help");
        CommandLineParser.Option optNew    = clParser.addBooleanOption('n', "new-xml");
        CommandLineParser.Option optOver   = clParser.addBooleanOption('o', "overwrite");
        CommandLineParser.Option optNoList = clParser.addBooleanOption('e', "suppress-exception-lists");
        CommandLineParser.Option optMethod = clParser.addStringOption('m', "method");

        String methodDesc =
            "Method 1: Use <vn>...</vn> links in grouping files to decide\n" +
            "whether a grouping reference is added to a VerbNet MEMBER line.\n" +
            "Method 2: Use <wn>...</wn> links in grouping files to decide\n" +
            "whether a grouping reference is added to a VerbNet MEMBER line.";

        optHelp.setHelpDescription("Usage");
        optNew.setHelpDescription("Generate new VerbNet XML files with the grouping references updated.  Requires VerbNet XML output directory.");
        optOver.setHelpDescription("Overwrite existing files");
        optNoList.setHelpDescription("Suppress the output of the exception lists in the report.");
        optMethod.setHelpDescription("Method to use when computing grouping links.  Valid values: 'm1', 'm2', 'm1m2', or 'm2m1'.  Default is 'm2m1' if not supplied.  Here are the descriptions of the methods:\n" + methodDesc);
        optMethod.setHelpParamName("METHOD");

        clParser.parse(args);

        Options.flHelp = (Boolean) clParser.getOptionValue(optHelp, false);
        Options.flNewXML = (Boolean) clParser.getOptionValue(optNew, false);
        Options.flOverwrite = (Boolean) clParser.getOptionValue(optOver, false);
        Options.flNoLists = (Boolean) clParser.getOptionValue(optNoList, false);
        Options.method = (String) clParser.getOptionValue(optMethod, null);

        if(Options.flHelp) {
            throw new UserWantsHelpMessage();
        }

        String[] remaining = clParser.getRemainingArgs();

        if(remaining.length < (Options.TOTAL_NON_FLAGS - 1)) {
            throw new InvalidCommandLineArgumentException("Invalid command line format.");
        }
        if(remaining.length != (Options.TOTAL_NON_FLAGS - 1) && !Options.flNewXML) {
            throw new InvalidCommandLineArgumentException(
                "Invalid command line format: Must specify -n when specifying output directory.");
        }
        if(remaining.length != Options.TOTAL_NON_FLAGS && Options.flNewXML) {
            throw new InvalidCommandLineArgumentException(
                "Invalid command line format: Cannot specify -n unless specifying output directory.");
        }

        Options.vnIn = new File(remaining[0]);
        Options.grpDir = new File(remaining[1]);
        Options.wnKeys = new File(remaining[2]);
        if(Options.flNewXML) {
            Options.vnOut = new File(remaining[3]);
        }

        if(Options.method != null) {
            String[] validMethods = new String[] {"m1", "m2", "m1m2", "m2m1"};
            if(!Arrays.asList(validMethods).contains(Options.method)) {
                throw new InvalidCommandLineArgumentException(
                    "Invalid command line format: Method must either be 'm1', 'm2', 'm1m2', or 'm2m1'.");
            }
        }

        checkPath(Options.vnIn, "VerbNet XML input directory", true);
        checkPath(Options.grpDir, "Grouping XML input directory", true);
        checkPath(Options.wnKeys, "WordNet index.sense", false);
        if(Options.flNewXML) {
            checkPath(Options.vnOut, "VerbNet XML output directory", true);
        }

        ensureUnique(Options.vnIn, Options.grpDir, "VerbNet XML input directory", "grouping XML input directory");
        if(Options.flNewXML) {
            ensureUnique(Options.vnIn, Options.vnOut, "VerbNet XML input directory", "VerbNet XML output directory");
            ensureUnique(Options.vnOut, Options.grpDir, "VerbNet XML output directory", "grouping XML input directory");
        }

        Options.vnXmlFiles = Options.vnIn.listFiles(new MyFilter(".xml"));
        if(Options.vnXmlFiles.length == 0) {
            throw new InvalidPathException(
                "There are no XML files in the VerbNet XML input directory.");
        }

        Options.grpXmlFiles = Options.grpDir.listFiles(new MyFilter("-v.xml"));
        if(Options.grpXmlFiles.length == 0) {
            throw new InvalidPathException(
                "There are no XML files in the grouping XML input directory.");
        }

        sortXMLFiles(Options.vnXmlFiles);
        sortXMLFiles(Options.grpXmlFiles);
    }

    private static void verifyXMLSyntax() throws BadXMLException {
        out.println("  --> Step 1: Verifying VerbNet XML...");

        if(Parser.parse(Options.vnXmlFiles)) {
            out.println("         ...XML OK.");
        } else {
            out.println("        ...XML INVALID.");
            MembershipMap<String, String> errorInfo = Parser.errorInfo;
            for(String err : errorInfo.keySet()) {
                System.err.println(err);
            }
            throw new BadXMLException(
                "There were syntax errors encountered in the VerbNet XML files.  Update aborted.");
        }

        out.println("  --> Step 2: Verifying Grouping XML...");

        if(Parser.parse(Options.grpXmlFiles)) {
            out.println("         ...XML OK.");
        } else {
            out.println("        ...XML INVALID.");
            MembershipMap<String, String> errorInfo = Parser.errorInfo;
            for(String err : errorInfo.keySet()) {
                System.err.println(err);
            }
            throw new BadXMLException(
                "There were syntax errors encountered in the Grouping XML files.  Update aborted.");
        }
    }

    ///////////////////////
    // Grouping Updating //
    ///////////////////////

    private static void updateGroupings() throws InvalidPathException {

        GroupingAnalyzer analyzer =
            new GroupingAnalyzer(Options.vnIn, Options.vnOut, Options.grpXmlFiles,
                Options.wnKeys, Options.flNoLists, Options.method);

        analyzer.update();

        if(Options.flNewXML) {
            File dtdSource = new File(Options.vnIn, "vn_class-3.dtd");
            File xsdSource = new File(Options.vnIn, "vn_schema-3.xsd");

            checkPath(dtdSource, "DTD file", false);
            checkPath(xsdSource, "XSD file", false);

            File dtdTarget = new File(Options.vnOut, "vn_class-3.dtd");
            File xsdTarget = new File(Options.vnOut, "vn_schema-3.xsd");

            copyFile(dtdSource, dtdTarget);
            copyFile(xsdSource, xsdTarget);
        }
    }

    ////////////////////
    // Helper Methods //
    ////////////////////

    private static String filePath(File f) {
        try {
            return f.getCanonicalPath();
        } catch(Exception e) {
            return f.getPath();
        }
    }

    private static void sortXMLFiles(File[] xmlFiles) {
        for(int a = 0; a < xmlFiles.length - 1; a++) {
            for(int b = a + 1; b < xmlFiles.length; b++) {
                File af = xmlFiles[a];
                File bf = xmlFiles[b];

                String an = xmlFiles[a].getName();
                String bn = xmlFiles[b].getName();

                if(an.compareTo(bn) > 0) {
                    xmlFiles[a] = bf;
                    xmlFiles[b] = af;
                }
            }
        }
    }

    private static void checkPath(File path, String desc, boolean isDir)
                throws InvalidPathException {
        if(!path.exists()) {
            throw new InvalidPathException(desc + " path does not exist.");
        }
        if(isDir && !path.isDirectory() || !isDir && !path.isFile()) {
            throw new InvalidPathException(desc + " path is not a " +
                                           (isDir ? "directory" : "regular file") + ".");
        }
        if(!path.canRead()) {
            throw new InvalidPathException(desc + " not readable.");
        }
    }

    private static void ensureUnique(File f1, File f2, String n1, String n2)
                throws InvalidPathException {
        if(f1.getAbsolutePath().equals(f2.getAbsolutePath())) {
            throw new InvalidPathException(
                "The " + n1 + " and the " + n2 + " must be unique.");
        }
    }

    private static void copyFile(File src, File dest) {
        try {
            if(dest.exists() && !Options.flOverwrite) {
                err.println("ERROR: Output file \"" + dest.getName() +
                            "\" already exists and overwrite not specified, skipping.");
                return;
            }

            FileChannel sourceChannel = new FileInputStream(src).getChannel();
            FileChannel destinationChannel = new FileOutputStream(dest).getChannel();

            sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);

            sourceChannel.close();
            destinationChannel.close();
        }
        catch(Exception e) {
            err.println("ERROR: Problem copying image file  \"" + src.getName() +
                        "\" to output directory.  " + e.getMessage());
        }
    }

    //////////////////////////
    // Supplemental Classes //
    //////////////////////////

    private static class InvalidCommandLineArgumentException extends IllegalArgumentException {
        public InvalidCommandLineArgumentException(String message) {
            super(message);
        }
    }

    private static class InvalidPathException extends IOException {
        public InvalidPathException(String message) {
            super(message);
        }
    }

    private static class UserWantsHelpMessage extends Exception {
        public UserWantsHelpMessage() {
            super();
        }
    }

    private static class BadXMLException extends Exception {
        public BadXMLException(String message) {
            super(message);
        }
    }

    private static class MyFilter implements FileFilter {
        private String ext;
        public MyFilter(String newExt) {
            ext = newExt;
        }
        public boolean accept(File pathName) {
            return pathName.getName().endsWith(ext);
        }
    }
}
