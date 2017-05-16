
///////////////////
// UVI Generator //
// Derek Trumbo  //
///////////////////

package semlink.apps.uvig;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import enums.RoleValue;
import slflixer.cc.util.mm.GroupComparator;
import slflixer.cc.util.mm.MembershipMap;
import syntax.Role;
import verbnet.Predicate;
import verbnet.SemanticFrame;
import verbnet.VerbNet;
import verbnet.VerbNetClass;

import java.nio.file.Files;

/**
 * This class is the driver for the UVI web page creation.  It validates all the command-line
 * arguments, reads in the VerbNet XML files one-by-one, performs validation, and generates the
 * corresponding HTML files.  Additional data sources are read from and all index pages are then
 * created (with PB and FN links).  This class contains all static members as the problem of parsing
 * at this high a level does not map well onto an OOP paradigm.  This javadoc documentation
 * is targeted at future developers, not at people attempting to use an interface herein.
 * All but one method in this program have 'private' scope (only {@link uvi.Generator#main(String[])} is public).
 * This class could have been forced into smaller modules, but for simplicity it is very long.
 * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
 * When this documentation was created, only *.html files were used.  Later, they were
 * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
 * <BR><BR><I>NOTE: All the "See Also" links in this Javadoc documentation refer to other
 * methods, variables, or classes which reference or are referenced *in the code* by the class
 * member in question. It's not the normal usage of the Javadoc "See Also" links because this
 * documentation is geared more towards future developers of this tool.</I>
 *
 * @author Derek Trumbo
 */
public class Generator
{

    ////////////
    // Fields //
    ////////////

    /**
     * The beginning portion of the URL which, when appended with a frame name,
     * refers to the web page on FrameNet's web site that will display
     * the proper frame.  This is used when constructing the index and when
     * adding links for the VN-FN mapping on each individual verb class page.
     *
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Sweeper#printMembers()
     */
    //static final String fnURL = "http://framenet.icsi.berkeley.edu/index.php?option=com_wrapper&amp;Itemid=118&amp;frame=";

    /**
     * The beginning portion of the URL which, when appended with a grouping page
     * refers to the web page for the given Ontonotes Sense Grouping verb.
     * This is used when adding links for the groupings on each individual verb
     * class page.
     *
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Sweeper#printMembers()
     */
    static final String grpURL = "http://verbs.colorado.edu/html_groupings/";

    /**
     * Used to identify VerbNet as a data source.
     *
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Sweeper#printMembers()
     */
    static final int DS_VERBNET = 0;

    /**
     * Used to identify PropBank as a data source.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#addOthers(int)
     */
    static final int DS_PROPBANK = 1;

    /**
     * Used to identify FrameNet as a data source.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#addOthers(int)
     */
    static final int DS_FRAMENET = 2;

    /**
     * Used to identify WordNet as a data source.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#addOthers(int)
     */
    static final int DS_WORDNET = 3;

    /**
     * Used to identify the VerbNet-FrameNet mapping as a data source.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#addOthers(int)
     */
    static final int DS_VN_FN = 4;

    /**
     * Used to identify the VerbNet-Cyc mapping as a data source.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#addOthers(int)
     */
    static final int DS_VN_CYC = 5;

    /**
     * Used to identify the OntoNotes Sense Groupings as a data source.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#addOthers(int)
     */
    static final int DS_GROUPING = 6;

    /**
     * Extras added to search themroles and predicates
     */
    static final int DS_THEMROLE = 7;
    static final int DS_PREDICATE = 8;
    
    /**
     * All of the supplemental files (supporting files) required by the program.  These
     * files must be present in the directory 'supplemental' at the time of the program's
     * execution.  See the README file for the purpose and format of each file.
     *
     * @see uvi.Generator#printSupplemental()
     * @see uvi.Generator#checkSupplementalFiles()
     * @see uvi.Generator#copySupplementalFiles()
     */
    private static String[] sNames = { "propbank.s",                        // CSV (verb,url)
                                       "framenet.s",                        // XML
                                       "grouping.s",                        // CSV (verb,url)
                                       "vn-fn.s",                           // XML
                                       "vn-cyc.s",                          // XML (optional to have in directory)
                                       "wordnet.s",                         // WordNet format (index.sense)
                                       "header.s",                          // HTML+PHP
                                       "footer.s",                          // HTML+PHP
                                       "index.s",                           // HTML+PHP
                                       "styles.s",                          // CSS
                                       "wn/wordnet.cgi.s",                  // Perl script (WN)
                                       "wn/styles-wn.s",                    // CSS (WN)
                                       "scripts.s",                         // JS
                                       "contact.s",                         // HTML+PHP
                                       "search.s",                          // PHP
                                       "postcomment.s",                     // PHP
                                       "comments.s",                        // PHP
                                       "include.s",                         // PHP
                                       "login.s",                           // PHP
                                       "vn/preps.s",                        // Text
                                       "vn/synrestr-desc.s",                // Text
                                       "properties.s",                      // Text
                                       "images/A.jpg", "images/B.jpg",
                                       "images/C.jpg", "images/D.jpg",
                                       "images/E.jpg", "images/F.jpg",
                                       "images/G.jpg", "images/H.jpg",
                                       "images/I.jpg", "images/J.jpg",
                                       "images/K.jpg", "images/L.jpg",
                                       "images/M.jpg", "images/N.jpg",
                                       "images/O.jpg", "images/P.jpg",
                                       "images/Q.jpg", "images/R.jpg",
                                       "images/S.jpg", "images/T.jpg",
                                       "images/U.jpg", "images/V.jpg",
                                       "images/W.jpg", "images/X.jpg",
                                       "images/Y.jpg", "images/Z.jpg",
                                       "images/favicon.ico",
                                       "images/key.gif",
                                       "images/ref.gif",
                                       "images/si.gif",
                                       "images/delete.gif", 
                                       "documents/selrestr_hierarchy.gif",
                                       "documents/themrole_hierarchy.pdf"
 };

    /**
     * Holds {@link java.io.File} objects in an associative array, keyed on the
     * file names stored in {@link uvi.Generator#sNames}.  This is loaded up as
     * the supplemental files are being checked and used primarily for convenience
     * in referencing the above files.
     *
     * @see uvi.Generator#checkSupplementalFiles()
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#createHTMLOutStream(String, String, String)
     * @see uvi.Generator#closeHTMLOutStream(PrintWriter, String)
     */
    private static Map sFiles;

    /**
     * Holds the user-definable properties that will be read in from the
     * properties.s supplemental file.
     */
    private static Map properties;

    /**
     * An array of strings representing the flags passed to the program
     * These are all of the command-line arguments that begin with a hypen ('-').
     * This array is loaded in {@link uvi.Generator#analyzeArguments(String[])} referenced
     * in {@link uvi.Generator#printStartInfo()}.  They are printed in the starting banner
     * of the program.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#printStartInfo()
     * @see uvi.Generator#printUsage()
     * @see uvi.Generator#existsFlag(String)
     */
    private static ArrayList flags;

    /**
     * An array of strings representing the non-flag command-line parameters.
     * This array should hold only the XML input directory and the HTML
     * output directory, and will be validated as such.  The first element
     * of the array will be considered the XML input directory and the
     * second element the HTML output directory.  This variable does
     * not need to be class-level; it could be local.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static ArrayList nonFlags;

    /**
     * An array of all possible flags that this program accepts.  Each flag
     * has both a short form and a long form.  All supplied flags are validated
     * against this array and the existence of any unknown flags triggers
     * an exception.  This variable does not need to be class-level; it could
     * be local.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static String[] allFlags = { "-?", "--help",
                                         "-v", "--verbose",
                                         "-q", "--quiet",
                                         "-o", "--overwrite",
                                         "-g", "--nogen",
                                         "-i", "--noinsp",
                                         "-x", "--novxc",
                                         "-s", "--sort",
                                         "-c", "--copyonly",
                                         "-y", "--withcyc",
                                         "-p", "--noindexpb",
                                         "-f", "--noindexfn",
                                         "-n", "--noindexsg",
                                         "-r", "--noindexvn",
                                         "-m", "--novnfnmap",
                                         "-w", "--novnwnlink"
                                       };

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used once only inside
     * of said method.  It is class-level for documentation purposes only.  This
     * flag is set if the user specifies they would like to see the help for the
     * tool.  If the user places a '-?' or '--help' anywhere on the command line
     * then this flag is set to <CODE>true</CODE>, the usage message is displayed
     * and the program exits.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#printUsage()
     */
    static boolean flHelp;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used throughout the rest
     * of the program.  If this variable is set to <CODE>true</CODE>, more output
     * is generated than otherwise.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#generateIndexFiles()
     */
    static boolean flVerbose;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used throughout the rest
     * of the program.  If this variable is set to <CODE>true</CODE>, only error
     * and warning messages will be sent to standard out.  This flag overrides
     * {@link uvi.Generator#flVerbose}.
     *
     * @see uvi.Generator#main(String[])
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#generateIndexFiles()
     */
    static boolean flQuiet;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used throughout the rest
     * of the program.  If this variable is set to <CODE>true</CODE>, the program
     * will write over existing files that it needs to create.  If this flag
     * is not specified then the program will not destroy existing files.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#createHTMLOutStream(String, String, String)
     * @see uvi.Generator#copySupplementalFiles()
     */
    static boolean flOverwrite;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used only inside
     * {@link uvi.Generator#replaceSpecialSequences(String, String)} to decide whether or not a link to
     * the Generator's disclaimer page should be added to each index page, including
     * the main index page.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     * @see uvi.Generator#generateIndexFiles()
     */
    static boolean flNoGen;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used only inside
     * {@link uvi.Generator#replaceSpecialSequences(String, String)} to decide whether or not a link to
     * the Inspector's disclaimer page should be added to each index page, including
     * the main index page.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     * @see uvi.Generator#generateIndexFiles()
     */
    static boolean flNoInsp;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used only inside
     * {@link uvi.Generator#replaceSpecialSequences(String, String)} to decide whether or not a link to
     * the VxC application's disclaimer page should be added to each index page, including
     * the main index page.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     * @see uvi.Generator#generateIndexFiles()
     */
    static boolean flNoVxC;

    // Suppress sources from the index.
    static boolean flNoIndexPb;
    static boolean flNoIndexFn;
    static boolean flNoIndexSg;
    static boolean flNoIndexVn;
    static boolean flNoVnFnMap;
    static boolean flNoVnWnLinks;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used only inside
     * {@link uvi.Sweeper#startMEMBER(Node)} to indicate whether or not to sort all
     * the members for each class/subclass.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Sweeper#startMEMBER(Node)
     */
    static boolean flSort;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used once only inside
     * of {@link uvi.Generator#generateHTMLFiles()} to suppress the generation of the
     * VerbNet class pages and the index pages.  All the supplemental files, however,
     * are copied and/or constructed.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#generateHTMLFiles()
     */
    static boolean flCopyOnly;

    /**
     * A flag set in {@link uvi.Generator#analyzeArguments(String[])} and used to decide if
     * the VerbNet-Cyc data source should be used and its contents displayed.
     * COMMENT
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#generateHTMLFiles()
     */
    static boolean flWithCyc;

    /**
     * An array of {@link java.io.File} objects that represent all the XML files in the
     * specified XML input directory.  This is loaded up in {@link uvi.Generator#analyzeArguments(String[])}
     * and referenced in {@link uvi.Generator#generateHTMLFiles()}.  If there are no XML files in
     * the XML input directory an exception is thrown and the program exits.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#generateHTMLFiles()
     */
    private static File[] xmlFiles;

    /**
     * A string that holds in a list format all those XML files that contain subclasses.  This
     * is used only to show which classes contain subclasses at the bottom of each letter index.
     *
     * @see uvi.Sweeper#startVNSUBCLASS(Node)
     * @see uvi.Generator#generateIndexFiles()
     */
    static String classesWithSubclasses = "";

    /**
     * A count of all subclasses in VerbNet, at any level (does not include main classes).
     *
     * @see uvi.Sweeper#startVNSUBCLASS(Node)
     * @see uvi.Generator#generateIndexFiles()
     */
    static int totalSubclasses = 0;

    /**
     * A list of every class and subclass name in Verbnet.  This is used to implement
     * the class hierarchy page.
     *
     * @see uvi.Sweeper#curIndentLevel
     * @see uvi.Sweeper#startVNCLASS(Node)
     * @see uvi.Sweeper#startVNSUBCLASS(Node)
     */
    static ArrayList classHierarchy = new ArrayList();

    /**
     * The {@link java.io.File} object that represents the user's desired XML input directory.
     * This is used to gather the XML files, check that a DTD file exists, and show in
     * the starting banner of the program.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#printStartInfo()
     * @see uvi.Generator#checkDTDFile()
     */
    private static File inDir;

    /**
     * The {@link java.io.File} object that represents the user's desired HTML output directory.
     * This is referenced essentially whenever a method calls
     * {@link uvi.Generator#createHTMLOutStream(String, String, String)},
     * as this method always uses the desired HTML output directory since the program
     * does not output to any other directory for any reason.  This is first set in
     * {@link uvi.Generator#analyzeArguments(String[])}, and referenced to create HTML files and copy
     * supplemental files.  It is also shown in the starting banner of the program.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     * @see uvi.Generator#printStartInfo()
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Generator#copySupplementalFiles()
     * @see uvi.Generator#createHTMLOutStream(String, String, String)
     */
    private static File outDir;

    private static File supDir;

    /**
     * The current date and time of the execution of this program.  It is set in
     * {@link uvi.Generator#main(String[])} and used in {@link uvi.Generator#replaceSpecialSequences(String, String)}
     * to replace @@date@@ and @@time@@ sequences stored in the header and footer
     * files with actual values.  See the README file for full list of possible
     * special sequences.
     *
     * @see uvi.Generator#main(String[])
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     */
    private static Calendar runTime;

    /**
     * A set of word lists constructed in the Sweeper class and accessed in this class.
     * This set of lists holds these lists:
     *   - General Thematic Roles
     *   - NP Thematic Roles
     *   - Selectional Restrictions
     *   - Syntax Restrictions
     *   - Predicates
     *
     * @see uvi.Sweeper#startNP(Node)
     * @see uvi.Sweeper#startSELRESTR(Node)
     * @see uvi.Sweeper#startSYNRESTR(Node)
     * @see uvi.Sweeper#startPRED(Node)
     * @see uvi.Sweeper#startTHEMROLE(Node)
     * @see uvi.Generator#printReferenceColumn(String, String)
     */
    static MembershipMap<String, String> refMap = new MembershipMap<String, String>();

    /**
     * A special word list for keeping track of the primary and
     * secondary frame types individually.
     */
    static MembershipMap<String, String> refFrameIndMap = new MembershipMap<String, String>();

    /**
     * A special word list for the syntactic frame descriptions.
     * This set of lists holds these lists:
     *   - Primary Frame Types
     *      - Secondary Frame Types
     *
     * @see uvi.Sweeper#startDESCRIPTION(Node)
     * @see uvi.Generator#printReferenceColumnSpecial(String)
     */
    static MembershipMap<String, String> refFrameBothMap = new MembershipMap<String, String>();

    ////////////////////
    // Helper Methods //
    ////////////////////

    /**
     * Used as shorthand for <CODE>System.out.println</CODE>.
     *
     * @param s the string to print
     * @see     java.io.PrintStream#println(String)
     */
    private static void println( String s ) { System.out.println( s ); }

    /**
     * Used as shorthand for <CODE>System.out.print</CODE>.
     *
     * @param s the string to print
     * @see     java.io.PrintStream#print(String)
     */
    private static void print( String s ) { System.out.print( s ); }

    /**
     * Used as shorthand for <CODE>System.err.println</CODE>.
     *
     * @param s the string to print
     * @see     java.io.PrintStream#println(String)
     */
    private static void eprintln( String s ) { System.err.println( s ); }

    /**
     * Used as shorthand for <CODE>System.err.print</CODE>.
     *
     * @param s the string to print
     * @see     java.io.PrintStream#print(String)
     */
    private static void eprint( String s ) { System.err.print( s ); }

    /**
     * Ensures that a path only contains the separator character for the given
     * OS on which the program is run.  This is only a concern because various
     * paths could be stored in the program in the {@link uvi.Generator#sNames} array.
     * If the system were ported to an OS that contained a different
     * separator character, things could break.
     *
     * @param p the path to check.  This will replace all occurrences of both
     *          types of separator characters with the current OS's separator
     *          character.
     * @return  the corrected string
     * @see     uvi.Generator#checkSupplementalFiles()
     */
    private static String sameSlash( String p )
    {
        char sep = File.separatorChar;

        return p.replace( '/', sep ).replace( '\\', sep );
    }

    /**
     * Returns a descriptive path for a {@link java.io.File} object.  This is
     * only an issue due to the possibility of the more desired method,
     * {@link java.io.File#getCanonicalPath()} to throw an exception.  If an
     * exception is thrown, the basic {@link java.io.File#getPath()} is returned.
     *
     * @param f the file whose path to retrieve
     * @return  a descriptive path
     */
    private static String filePath( File f )
    {
        try
        {
            return f.getCanonicalPath();
        }
        catch( Exception e )
        {
            return f.getPath();
        }
    }

    /**
     * Sorts the already loaded XML files array {@link uvi.Generator#xmlFiles}.
     * This is because different operating systems will return a directory's
     * contents in different orders but we want to always process the files
     * in a consistent fashion.
     */
    private static void sortXMLFiles()
    {
        for( int a = 0; a < xmlFiles.length - 1; a++ ) {
            for( int b = a + 1; b < xmlFiles.length; b++ )
            {
                File af = xmlFiles[ a ];
                File bf = xmlFiles[ b ];

                String an = xmlFiles[ a ].getName();
                String bn = xmlFiles[ b ].getName();

                if( an.compareTo( bn ) > 0 )
                {
                    xmlFiles[ a ] = bf;
                    xmlFiles[ b ] = af;
                }
            }
        }
    }

    /////////////////
    // Constructor //
    /////////////////

    /**
     * This constructor is private because the class is not intended to ever
     * be instantiated.  The UVI generation is a very procedural process and
     * thus all the members are static.
     */
    private Generator() {}

    //////////
    // Main //
    //////////

    /**
     * Drives the entire generation process.  This is the only public method.
     * All program-ending exceptions are caught here and displayed.
     *
     * @param args Contains the command-line arguments for the program.
     */
    
public static void main( String args[] )
    {
    	//Emptying current directory...
    	//Take this out tho
    	try {
			FileUtils.cleanDirectory(new File("/var/www/TempUVI/"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    	args = new String[]{"-s","/home/kevin/Lexical_Resources/verbnet/", "/var/www/TempUVI/", "web/uvig/supplemental/"};
        try
        {

            // Set the date and time of this program's execution.
            runTime = Calendar.getInstance();

            // Check command-line arguments for accuracy and set appropriate variables.
            analyzeArguments( args );

            // Perform checks on required files.
            checkDTDFile();
            checkSupplementalFiles();

            // Read in user-definable properties.
            readProperties();

            // Print an initial message with basic information.
            if( !flQuiet ) {
                printStartInfo();
            }

            // Create index files, files for each VerbNet class, and any other supporting
            // PHP files.
            generateHTMLFiles();

            // Print a message to show the process is over.
            if( !flQuiet ) {
                printEndInfo();
            }
        }

        // The command-line arguments were not valid.
        catch( InvalidCommandLineArgumentException iclae )
        {
            eprintln( "ERROR: " + iclae.getMessage() );
            printUsage();
        }

        // A help flag has been supplied.
        catch( UserWantsHelpMessage uwhm )
        {
            printUsage();
        }

        // The input or output directory was not valid.
        catch( InvalidDirectoryException ide )
        {
            eprintln( "ERROR: " + ide.getMessage() );
        }

        // The supplemental files are not all accessible.
        catch( InvalidSupplementalFilesException isfe )
        {
            eprintln( "ERROR: " + isfe.getMessage() );
            printSupplemental();
        }

        // Show any other errors that might occur.
        catch( Exception e )
        {
            eprintln( "ERROR: [Generic/main] " + e.getMessage() );
            e.printStackTrace();
        }
    }

    //////////////////////////
    // Informational Output //
    //////////////////////////

    /**
     * Prints the usage message.  This message includes all available flags.  This
     * method can be invoked from main either because the user has specified invalid
     * command-line arguments or because the user has requested to see help for
     * the program.  This message is also the 'help' message.
     *
     * @see uvi.Generator#main(String[])
     */
    private static void printUsage()
    {
        println( "Usage: run [flags] <xml-input-dir> <html-output-dir> <supplemental-dir>" );
        println( "Flags:" );
        println( "  -?, --help         Usage" );
        println( "  -v, --verbose      Verbose output" );
        println( "  -q, --quiet        No output except for errors and warnings, overrides -v" );
        println( "  -o, --overwrite    Overwrite existing files in HTML output directory" );
        println( "  -g, --nogen        Surpress the \"Generator\" links on the index pages" );
        println( "  -i, --noinsp       Surpress the \"Inspector\" links on the index pages" );
        println( "  -x, --novxc        Suppress the \"VxC\" links on the index pages" );
        println( "  -s, --sort         Sort the members in each \"VerbNet\" class or subclass" );
        println( "  -c, --copyonly     Surpress VerbNet/Index page creation, only re-copy suppl. files" );
        println( "  -y, --withcyc      Include VN-Cyc mapping in output, suppl. file \"vn-cyc.s\" is required" );
        println( "  -p, --noindexpb    Surpress the \"PropBank\" verbs in the index" );
        println( "  -f, --noindexfn    Surpress the \"FrameNet\" verbs in the index" );
        println( "  -n, --noindexsg    Surpress the \"OntoNotes Sense Groupings\" verbs in the index" );
        println( "  -r, --noindexvn    Surpress the \"VerbNet\" verbs in the index" );
        println( "  -m, --novnfnmap    Surpress the \"VerbNet-FrameNet mappings\" in the VerbNet class pages" );
        println( "  -w, --novnwnlinks  Surpress the \"WordNet\" links in the VerbNet class pages" );
        println( "Read more about this tool in the README file." );
        println( "**NOTE: Any reference to \"HTML File(s)\" or \"HTML Directory\" is a synonym" );
        println( "for \"PHP file(s)\" or \"PHP Directory\"." );
    }

    /**
     * Prints the information about the contents of the required supplemental directory.  This
     * method is invoked from main if one or more of the required supplemental files were not
     * found.  Read more about the significance of each file in the README file.
     *
     * @see uvi.Generator#main(String[])
     */
    private static void printSupplemental()
    {
        println( "The supplemental directory contains the additional files that the generator" );
        println( "reads in and uses in order to generate the HTML output.  The supplemental" );
        println( "directory must contain:" );
        println( "     ./supplemental/" );

        // Print all the supplemental file names.
        for( int s = 0; s < sNames.length; s++ ) {
            // Show an extra note next to the VerbNet-Cyc mapping file.
            if( sNames[ s ].equals( "vn-cyc.s" ) ) {
                println( "         " + sNames[ s ] + "* (if -y or --withcyc supplied on command line)" );
            } else {
                println( "         " + sNames[ s ] );
            }
        }
    }

    /**
     * Prints information before the HTML generation process has completed.  This method
     * is called from {@link uvi.Generator#main(String[])}.  The information printed includes: the desired
     * XML input directory, the desired HTML output directory, supplied flags, and
     * date and time when the program was executed.
     *
     * @see uvi.Generator#main(String[])
     */
    private static void printStartInfo()
    {
        String af = "",
        dt = new Date().toString(),        // Get today's date and time.
        ip,
        dp,
        sp;

        // Create a string list of all the flags, or 'none' if there aren't any.
        if( flags.size() == 0 ) {
            af = "(none)";
        } else {
            for( int f = 0; f < flags.size(); f++ ) {
                af += flags.get( f ) + " ";
            }
        }

        // Get string representations of the files' paths.
        ip = filePath( inDir );
        dp = filePath( outDir );
        sp = filePath( supDir );

        // Output the banner.
        println( "Unified Verb Index Generator" );
        println( "====================================================" );
        println( "XML Input Directory:    " + ip );
        println( "HTML Output Directory:  " + dp );
        println( "Supplemental Directory: " + sp );
        println( "Flags:                  " + af );
        println( "Executed On:            " + dt );
        println( "====================================================" );
        println( "Generation Begun" );
    }

    /**
     * Prints information after the HTML generation process has completed.  As of
     * now this is a single message.
     *
     * @see uvi.Generator#main(String[])
     */
    private static void printEndInfo()
    {
        println( "Generation Complete" );
        println( "Notes: Be sure to enable or request the enabling of the output directory" );
        println( "for perl CGI scripts.  This is to enable the \"wordnet.cgi\" script." );
        println( "It is recommended that you protect the \"users\" directory with a" );
        println( "\".htaccess\" and a \".htpasswd\" file.");
    }

    ////////////////////////////
    // Command-Line Arguments //
    ////////////////////////////

    /**
     * Validates all of the command-line arguments and sets appropriate class-level variables.
     *
     * @param args the command-line arguments from {@link uvi.Generator#main(String[])}
     * @throws InvalidCommandLineArgumentException if the command-line contains invalid flags
     *         or does not contain the two requisite paths.
     * @throws InvalidDirectoryException if the paths requested do not exist or do not have
     *         the appropriate permissions.
     * @throws UserWantsHelpMessage if the user has supplied the help flag.
     * @see uvi.Generator#existsFlag(String)
     * @see uvi.Generator.MyFilter
     */
    private static void analyzeArguments( String args[] )
    throws InvalidCommandLineArgumentException,
    InvalidDirectoryException,
    UserWantsHelpMessage
    {

        // Allocate the command-line arrays.
        flags    = new ArrayList();
        nonFlags = new ArrayList();

        // Divide the command-line arguments into flags and non-flags.
        for( int a = 0; a < args.length; a++ )
        {
            if( args[ a ].startsWith( "-" ) ) {
                flags.add( args[ a ] );
            } else {
                nonFlags.add( args[ a ] );
            }
        }

        // Expand each combined flag (i.e. -ov into -o -v).
        for( int f = 0; f < flags.size(); f++ )
        {
            String flag = ( String ) flags.get( f );

            if( flag.length() > 2 && flag.charAt( 0 ) == '-' && flag.charAt( 1 ) != '-' )
            {
                for( int g = 2; g < flag.length(); g++ ) {
                    flags.add( "-" + flag.charAt( g ) );
                }

                flags.set( f, flag.substring( 0, 2 ) );
            }
        }

        // Set the appropriate class-level booleans.
        flHelp      = existsFlag( "-?" ) || existsFlag( "--help"      );
        flVerbose   = existsFlag( "-v" ) || existsFlag( "--verbose"   );
        flQuiet     = existsFlag( "-q" ) || existsFlag( "--quiet"     );
        flOverwrite = existsFlag( "-o" ) || existsFlag( "--overwrite" );
        flNoGen     = existsFlag( "-g" ) || existsFlag( "--nogen"     );
        flNoInsp    = existsFlag( "-i" ) || existsFlag( "--noinsp"    );
        flNoVxC     = existsFlag( "-x" ) || existsFlag( "--novxc"     );
        flSort      = existsFlag( "-s" ) || existsFlag( "--sort"      );
        flCopyOnly  = existsFlag( "-c" ) || existsFlag( "--copyonly"  );
        flWithCyc   = existsFlag( "-y" ) || existsFlag( "--withcyc"   );

        flNoIndexPb   = existsFlag( "-p" ) || existsFlag( "--noindexpb" );
        flNoIndexFn   = existsFlag( "-f" ) || existsFlag( "--noindexfn" );
        flNoIndexSg   = existsFlag( "-n" ) || existsFlag( "--noindexsg" );
        flNoIndexVn   = existsFlag( "-r" ) || existsFlag( "--noindexvn" );
        flNoVnFnMap   = existsFlag( "-m" ) || existsFlag( "--novnfnmap" );
        flNoVnWnLinks = existsFlag( "-w" ) || existsFlag( "--novnwnlinks" );

        if(flNoIndexVn) {
            flNoVnFnMap = true;
            flNoVnWnLinks = true;
        }

        // If a help flag was present, stop this method and return to main to
        // show the help/usage message.
        if( flHelp ) {
            throw new UserWantsHelpMessage();
        }

        // If there are not exactly two non-flag arguments, then show an error message.
        // The two non-flag arguments are the XML input directory path and HTML output
        // directory path.
        if( nonFlags.size() != 3 ) {
            throw new InvalidCommandLineArgumentException( "Invalid command line format." );
        }

        // Validate the supplied flags.
        for( int f = 0; f < flags.size(); f++ )
        {
            boolean found = false;

            for( int g = 0; g < allFlags.length; g++ ) {
                if( flags.get( f ).equals( allFlags[ g ] ) )
                {
                    found = true;
                    break;
                }
            }

            if( !found ) {
                throw new InvalidCommandLineArgumentException( "Unknown command line flag." );
            }
        }

        // Create the File objects for the supplied paths.
        inDir  = new File( ( String ) nonFlags.get( 0 ) );
        outDir = new File( ( String ) nonFlags.get( 1 ) );
        supDir = new File( ( String ) nonFlags.get( 2 ) );

        // Validate the supplied paths.  Each path must exist, be a directory, and
        // the user must have the appropriate rights to it.
        if( !inDir.exists() ) {
            throw new InvalidDirectoryException( "XML input directory path does not exist."       );
        }
        if( !outDir.exists() ) {
            if( !outDir.mkdir() ) {
                throw new InvalidDirectoryException( "Could not create HTML output directory."    );
            }
        }
        if( !supDir.exists() ) {
            throw new InvalidDirectoryException( "Supplemental directory path does not exist."    );
        }

        if( !inDir.isDirectory() ) {
            throw new InvalidDirectoryException( "XML input directory path is not a directory."   );
        }
        if( !outDir.isDirectory() ) {
            throw new InvalidDirectoryException( "HTML output directory path is not a directory." );
        }
        if( !supDir.isDirectory() ) {
            throw new InvalidDirectoryException( "Supplemental directory path is not a directory.");
        }

        if( !inDir.canRead() ) {
            throw new InvalidDirectoryException( "XML input directory not readable."              );
        }
        if( !outDir.canWrite() ) {
            throw new InvalidDirectoryException( "HTML output directory not modifiable."          );
        }
        if( !supDir.canRead() ) {
            throw new InvalidDirectoryException( "Supplemental directory not readable."           );
        }

        // Get the list of all XML files in the input directory.
        xmlFiles = inDir.listFiles( new MyFilter( "xml" ) );

        // Make sure there is at least one XML file in the input directory.
        if( xmlFiles.length == 0 ) {
            throw new InvalidDirectoryException( "There are no XML files in the XML input directory." );
        }

        sortXMLFiles();
    }

    /**
     * Checks whether the given flag was supplied by the user.
     *
     * @param flag the flag to look for
     * @return <CODE>true</CODE> if the flag exists in the user's supplied
     *         command-line arguments.
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static boolean existsFlag( String flag )
    {
        for( int f = 0; f < flags.size(); f++ ) {
            if( flags.get( f ).equals( flag ) ) {
                return true;
            }
        }

        return false;
    }

    ////////////////////////////
    // Additional File Checks //
    ////////////////////////////

    /**
     * Makes sure there is at least one DTD file in the XML input directory.
     * This does not check whether the DTD file found is the one referenced
     * in the XML files, as that check is performed later by the XML parser.
     * This is a fairly weak check in that sense, but here for added safety.
     *
     * @see uvi.Generator#main(String[])
     */
    private static void checkDTDFile()
    {
        String[] inFiles = inDir.list();     // Get every file in the input directory.
        boolean found = false;

        // Search for the DTD file.
        for( int f = 0; f < inFiles.length; f++ ) {
            if( inFiles[ f ].toLowerCase().endsWith( ".dtd" ) )
            {
                found = true;
                break;
            }
        }

        // Show a warning if there is no DTD file.
        if( !found ) {
            eprintln( "WARNING: No DTD file found in XML input directory.  This could cause errors." );
        }
    }

    /**
     * Makes sure all required supplemental files exist.  The supplemental directory must
     * exist in the same directory as src/ and uvi/ and must contain the files listed
     * in the help/usage message.
     *
     * @throws InvalidSupplementalFilesException if the supplemental directory or any
     *         required files therein do not exist or are not readable.
     * @see uvi.Generator#main(String[])
     */
    private static void checkSupplementalFiles() throws InvalidSupplementalFilesException
    {

        // Instantiate the map that will hold the File objects for each
        // required supplemental file.  The key will be the file name.
        sFiles = new HashMap();

        for( int s = 0; s < sNames.length; s++ )
        {

            // If the use-vn-cyc-mapping flag was not supplied, do not require it among
            // the supplemental files (skip it).
            if( sNames[ s ].equals( "vn-cyc.s" ) && !flWithCyc ) {
                continue;
            }

            // Make sure any slashes used here in the code are the correct
            // ones for this operating system.
            sNames[ s ] = sameSlash( sNames[ s ] );

            // Create the File object for this file.
            File sFile = new File( supDir, sNames[ s ] );

            // Make sure the file is good.
            if( !sFile.exists() ) {
                throw new InvalidSupplementalFilesException( "Supplemental file \"" + sNames[ s ] + "\" does not exist." );
            }

            if( !sFile.isFile() ) {
                throw new InvalidSupplementalFilesException( "Supplemental file \"" + sNames[ s ] + "\" is not a file." );
            }

            if( !sFile.canRead() ) {
                throw new InvalidSupplementalFilesException( "Supplemental file \"" + sNames[ s ] + "\" not readable." );
            }

            // Place the object into the map under its file name.
            sFiles.put( sNames[ s ], sFile );
        }
    }

    /**
     * Reads key-value pairs out of the properties.s supplemental file.
     */
    private static void readProperties() {
        properties = new LinkedHashMap();
        try {
            BufferedReader reader = new BufferedReader( new FileReader( ( File ) sFiles.get( "properties.s" ) ) );

            String line;
            String curKey = null;
            String curValue = "";

            while( ( line = reader.readLine() ) != null) {

                // Trim and ignore blank and comment lines.
                line = line.trim();
                if( line.startsWith( "#" ) ) {
                    continue;
                }

                // Find location of equals character.
                int equal = line.indexOf( '=' );

                // Assume line is not a key= line initially.
                boolean isKeyLine = false;

                // If the equals character is at least the second character on
                // the line...
                if( equal > 0 ) {

                    // Grab the character before the equals sign.
                    char before = line.charAt( equal - 1 );

                    // If the character is not a backslash, then this line is
                    // a key= line, else it is a regular line, and remove
                    // the slash.
                    if( before != '\\' ) {
                        isKeyLine = true;
                    } else {
                        line = line.substring( 0, equal - 1 ) + line.substring( equal );
                    }
                }

                if( isKeyLine ) {
                    if( curKey != null ) {
                        while( curValue.length() > 0 && curValue.charAt( curValue.length() - 1 ) == '\n' ) {
                            curValue = curValue.substring( 0, curValue.length() - 1 );
                        }
                        properties.put( curKey, curValue );
                    }
                    curKey = line.substring( 0, equal ).trim();
                    curValue = line.substring( equal + 1 ) + "\n";
                } else {
                    curValue += line + "\n";
                }
            }

            if( curKey != null ) {
                while( curValue.length() > 0 && curValue.charAt( curValue.length() - 1 ) == '\n' ) {
                    curValue = curValue.substring( 0, curValue.length() - 1 );
                }
                properties.put( curKey, curValue );
            }

            reader.close();

        } catch(Exception e) {
            eprintln( "ERROR: Problem reading properties file.  Some properties may not have been read.  " + e.getMessage() );
        }
    }

    /**
     *
     */
    public static String getProperty( String propertyName, String defaultValue ) {
        String value = ( String ) properties.get( propertyName );
        if( value == null ) {
            value = defaultValue;
        }
        return value;
    }
    //////////////////////////////

    ////////////////////////////////
    // Copying Supplemental Files //
    ////////////////////////////////

    /**
     * Copies the supplemental files that belong in the HTML output directory to
     * that directory.  These files include the CSS stylesheet, the JavaScript
     * code, and all images.  It's important to note that the current
     * implementation of this method does not maintain directory structure of
     * the supplemental files.  Each file in the supplemental directory,
     * in a sub-directory or not, will be copied to the same HTML output
     * directory.  So if you begin to add additional supplemental files, and have
     * an <CODE>supplemental/abc.jpg</CODE> and an <CODE>supplemental/images/abc.jpg</CODE>,
     * only one file will appear in the output directory, <CODE>&lt;html&gt;/abc.jpg</CODE>,
     * unless this method is changed create subdirectories, etc. (but you can just not
     * give your files identical names).
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#copyFile(File, File)
     * @see uvi.Generator#createHTMLOutStream(String, String, String)
     */
    private static void copySupplementalFiles()
    {
        PrintWriter pw;

        // Copy the CSS, JS, Text, and include.php files to the output directory without
        // any additional files added before or after the body of the supplemental file.

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"styles.s\" to HTML output directory as \"styles.css\"..." );
        }

        pw = createHTMLOutStream( "styles.css", "styles.s" );
        if( pw != null ) {
            pw.close();
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"wn/styles-wn.s\" to HTML output directory as \"wn/styles-wn.css\"..." );
        }

        pw = createHTMLOutStream( "wn/styles-wn.css", "wn/styles-wn.s" );
        if( pw != null ) {
            pw.close();
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"scripts.s\" to HTML output directory as \"scripts.js\"..." );
        }

        pw = createHTMLOutStream( "scripts.js", "scripts.s" );
        if( pw != null ) {
            pw.close();
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"vn/preps.s\" to HTML output directory as \"vn/preps.txt\"..." );
        }

        pw = createHTMLOutStream( "vn/preps.txt", "vn/preps.s" );
        if( pw != null ) {
            pw.close();
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"vn/synrestr-desc.s\" to HTML output directory as \"vn/synrestr-desc.txt\"..." );
        }

        pw = createHTMLOutStream( "vn/synrestr-desc.txt", "vn/synrestr-desc.s" );
        if( pw != null ) {
            pw.close();
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"wn/wordnet.cgi.s\" to HTML output directory as \"wn/wordnet.cgi\"..." );
        }

        pw = createHTMLOutStream( "wn/wordnet.cgi", "wn/wordnet.cgi.s" );
        if( pw != null ) {
            pw.close();
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"include.s\" to HTML output directory as \"include.php\"..." );
        }

        pw = createHTMLOutStream( "include.php", "include.s" );
        if( pw != null ) {
            pw.close();
        }

        // Copy the PHP files to the output directory with the header file before
        // the body and the footer file after the body (addded with closeHTMLOutStream).

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"contact.s\" to HTML output directory as \"contact.php\"..." );
        }

        pw = createHTMLOutStream( "contact.php", "header.s", "contact.s" );
        if( pw != null ) {
            closeHTMLOutStream( pw, "contact.php" );
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"search.s\" to HTML output directory as \"search.php\"..." );
        }

        pw = createHTMLOutStream( "search.php", "header.s", "search.s" );
        if( pw != null ) {
            closeHTMLOutStream( pw, "search.php" );
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"postcomment.s\" to HTML output directory as \"postcomment.php\"..." );
        }

        pw = createHTMLOutStream( "postcomment.php", "header.s", "postcomment.s" );
        if( pw != null ) {
            closeHTMLOutStream( pw, "postcomment.php" );
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"comments.s\" to HTML output directory as \"comments.php\"..." );
        }

        pw = createHTMLOutStream( "comments.php", "header.s", "comments.s" );
        if( pw != null ) {
            closeHTMLOutStream( pw, "comments.php" );
        }

        if( flVerbose && !flQuiet ) {
            println( "Supplemental Files: Copying \"login.s\" to HTML output directory as \"login.php\"..." );
        }

        pw = createHTMLOutStream( "login.php", "header.s", "login.s" );
        if( pw != null ) {
            closeHTMLOutStream( pw, "login.php" );
        }

        // Copy the image files to the output directory.
        for( int s = 0; s < sNames.length; s++ ) {
            if( sNames[ s ].startsWith( "images" ) || sNames[s].startsWith("documents"))
            {

                // Prepare the input File object.
                File src = new File( supDir, sNames[ s ] );

                // Extract just the file name from the path stored here in the code.
                String fileName = sNames[ s ];

                // Prepare the output File object.
                File dest = new File(outDir, fileName);

                if( flVerbose && !flQuiet ) {
                    println( "Supplemental Files: Copying \"" + sNames[ s ] + "\" to HTML output directory as \"" + fileName + "\"..." );
                }

                // Skip this file if it exists and the overwrite flag has not been specified.
                if( dest.exists() && !flOverwrite )
                {
                    eprintln( "ERROR: Output file \"" + fileName + "\" already exists and overwrite not specified, skipping." );
                    continue;
                }

                // Create a blank file for it in case it doesn't already exist.
                try
                {
                    dest.createNewFile();
                }
                catch( Exception e )
                {
                    eprintln( "ERROR: Problem opening output file \"" + fileName + "\".  " + e.getMessage() );
                    continue;
                }

                // Perform the copy.
                copyFile( src, dest );
            }
        }
    }

    /**
     * Performs a low-level copy of one file into another.
     *
     * @param src  the source file
     * @param dest the destination file
     * @see        uvi.Generator#copySupplementalFiles()
     */
    private static void copyFile( File src, File dest )
    {
        try
        {
            FileChannel sourceChannel      = new FileInputStream( src ).getChannel();
            FileChannel destinationChannel = new FileOutputStream( dest ).getChannel();

            sourceChannel.transferTo( 0, sourceChannel.size(), destinationChannel );

            sourceChannel.close();
            destinationChannel.close();
        }
        catch( Exception e )
        {
            eprintln( "ERROR: Problem copying image file  \"" + src.getName() +
                "\" to output directory.  " + e.getMessage() );
        }
    }

    /**
     * Creates the two subdirectories that the UVI utilizes for comments and users.
     *
     * @see uvi.Generator#generateHTMLFiles()
     */
    private static void createSubdirectories()
    {
    	
        createSubdirectory("comments");
        createSubdirectory("images");
        createSubdirectory("documents");
        createSubdirectory("index");
        createSubdirectory("search");
        File u = createSubdirectory("users");
        createSubdirectory("vn");
        createSubdirectory("wn");
        
        //Added by K. Stowe
        createSubdirectory("themroles");
        createSubdirectory("selrestrs");
        createSubdirectory("predicates");
        createSubdirectory("synrestrs");
        createSubdirectory("verbfeatures");
        
        // Write a single administrator user to the user file initially, if it
        // doesn't already exist.
        try
        {
            File ulist = new File( u, "user.list" );
            if( !ulist.exists() )
            {
                PrintWriter pw = new PrintWriter( new BufferedWriter( new FileWriter( ulist ) ) );
                pw.println( "admin,password,1" );
                pw.close();
            }
        }
        catch( Exception e )
        {
            eprintln( "ERROR: Problem opening output file \"user.list\".  " + e.getMessage() );
        }
    }

    protected static File createSubdirectory(String dirName) {
        File subdir = new File(outDir, dirName);
        subdir.mkdir();
        if(!subdir.exists()) {
            throw new RuntimeException("Could not create subdirectory: " + dirName);
        }
        return subdir;
    }

    ///////////////////////////
    // HTML Files Generation //
    ///////////////////////////

    /**
     * Completes the filling of the HTML output directory of all files, pre-made ones
     * and generated ones.  This method and supporting {@link uvi.Generator#generateOneFile(DocumentBuilder, File)}
     * method make use of JDOM to parse the incoming XML files.  Essentially this just means
     * that {@link javax.xml.parsers.DocumentBuilderFactory}, {@link javax.xml.parsers.DocumentBuilder},
     * and {@link org.w3c.dom.Document} are utilized.  After the HTML files for
     * each class are generated, this method reads in PropBank and FrameNet information,
     * adds it to the growing index, and finally generates all the HTML files needed
     * for the index/indices.
     *
     * @see uvi.Generator#main(String[])
     * @see uvi.Generator#generateOneFile(DocumentBuilder, File)
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Generator#addOthers(int)
     */
    private static void generateHTMLFiles()
    {
        // Create the subdirectories that the UVI uses
        createSubdirectories();
                
        //Additional reference pages by K. Stowe
        ReferencePages.generateThemRolesReference(outDir);
        ReferencePages.generateSelectionalReference(outDir);
        ReferencePages.generatePredicateReference(outDir);
        ReferencePages.generateSyntacticReference(outDir);
        ReferencePages.generateVerbFeatureReference(outDir);

        loadThemRoleLinks();
        loadPredicateLinks();
        
        // List all the supplemental files.
        if( flVerbose && !flQuiet )
        {
            println( "Supplemental files valid" );
            for( int s = 0; s < sNames.length; s++ ) {
                if( !sNames[ s ].equals( "vn-cyc.s" ) || flWithCyc ) {
                    println( "   " + filePath( ( File ) sFiles.get( sNames[ s ] ) ) );
                }
            }
        }

        // Place all required supporting files in the HTML output directory.
        copySupplementalFiles();

        if( flVerbose && !flQuiet ) {
            System.out.println("Properties defined:");
            for(Object o : properties.keySet()) {
                String key = (String) o;
                String val = (String) properties.get(o);
                System.out.println("  " + key + " = " + val);
            }
        }

        // If the user only wanted to copy/construct the supplemental files, stop here.
        if( flCopyOnly ) {
            return;
        }

        if(!flNoVnFnMap) {
            if( flVerbose && !flQuiet ) {
                println( "Additional Source: Extracting VerbNet-FrameNet mapping info for members from \"vn-fn.s\"..." );
            }

            // Load VerbNet-FrameNet mapping links for the VerbNet classes.  This one
            // happens before the loop since the verb classes depend on these being
            // loaded (see Sweeper.printMembers).
            addOthers( DS_VN_FN );
        }

        // COMMENT
        if( flWithCyc )
        {
            if( flVerbose && !flQuiet ) {
                println( "Additional Source: Extracting VerbNet-Cyc mapping info for members from \"vn-cyc.s\"..." );
            }

            addOthers( DS_VN_CYC );
        }

        if(!flNoVnWnLinks) {
            if( flVerbose && !flQuiet ) {
                println( "Additional Source: Extracting WordNet sense numbers for members from \"wordnet.s\"..." );
            }

            // Load WordNet sense number info for the VerbNet classes.  This one
            // happens before the loop since the verb classes depend on these being
            // loaded (see Sweeper.printMembers).
            addOthers( DS_WORDNET );
        }

        if(!flNoIndexVn) {
            try
            {

                // Create essential JDOM objects.
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                // So XML files are checked against their DTD.
                dbf.setValidating( true );

                DocumentBuilder db = dbf.newDocumentBuilder();

                XMLErrorHandler xmlErrH = new XMLErrorHandler();

                // So custom error messages can be printed (see XMLErrorHandler).
                db.setErrorHandler( xmlErrH );

                for( int x = 0; x < xmlFiles.length; x++ )
                {
                    String xmlFileName = xmlFiles[ x ].getName();
                    String htmlFileName = "vn/" + xmlFileName.substring( 0, xmlFileName.lastIndexOf( "." ) ) + ".php";

                    // Print the name of the current file being processed.
                    if( flVerbose && !flQuiet ) {
                        println( "Processing file \"" + xmlFileName + "\" into \"" + htmlFileName + "\"..." );
                    }

                    // Create the stream for the HTML file and give it to the 'Q class'.
                    Q.setWriter( createHTMLOutStream( htmlFileName ) );

                    // Tell the 'Sweeper' class what file is currently being processed.  It
                    // will use this information to add the appropriate links to the index
                    // as verbs are discovered in the XML files and to place in error messages
                    // for location-of-error info.
                    Sweeper.setCurFile( htmlFileName );

                    // If everything with the stream was good, generated the HTML file.
                    if( Q.getWriter() != null )
                    {

                        // Let the JDOM XML error handler know what input file is currently
                        // being processed so it can report which file contained the error.
                        xmlErrH.setActiveFile( xmlFileName );

                        // Perform translation from XML->HTML.
                        generateOneFile( db, xmlFiles[ x ] );

                        // Close the stream to the HTML file.
                        closeHTMLOutStream( Q.getWriter(), htmlFileName );
                    }
                }
            }
            catch( ParserConfigurationException e )
            {
                eprintln( "ERROR: Parser configuration error." );
            }
        }

        if(!flNoVnFnMap) {
            // Print warnings for all matches not utilized in the VN-FN mapping.
            VN_FN_Map.printUnused();
        }

        if(!flNoIndexPb) {
            if( flVerbose && !flQuiet ) {
                println( "Additional Source: Extracting PropBank info for index from \"propbank.s\"..." );
            }

            // Add PropBank links to index.
            addOthers( DS_PROPBANK );
        }

        if(!flNoIndexFn) {
            if( flVerbose && !flQuiet ) {
                println( "Additional Source: Extracting FrameNet info for index from \"framenet.s\"..." );
            }

            // Add FrameNet links to index.
            addOthers( DS_FRAMENET );
        }

        if(!flNoIndexSg) {
            if( flVerbose && !flQuiet ) {
                println( "Additional Source: Extracting Grouping info for index from \"grouping.s\"..." );
            }

            // Add Grouping links to index.
            addOthers( DS_GROUPING );
        }

        // Sort the verbs for each index page (for each letter).
        Index.sort();

        // Generate index.php and A.php through Z.php.
        generateIndexFiles();

        if(!flNoIndexVn) {

            // Generate the reference page, reference.php.
            generateReferencePage();

            // Generate the class hierarchy, class-h.php.
            generateClassHierarchy();

        }
    }

    /**
     * Creates a page that displays the field information collected during the
     * generation phase. Each list is basically just a unique list of all values provided
     * for the given element over all the XML files.
     *
     * @see uvi.Generator#generateHTMLFiles()
     */
    private static void generateReferencePage()
    {
        if( flVerbose && !flQuiet ) {
            println( "Constructing reference page \"vn/reference.php\"..." );
        }

        // Open the stream to the page (this inserts the header).
        PrintWriter pw = createHTMLOutStream( "vn/reference.php" );

        Q.setWriter( pw );

        // Print the top table with the message.
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<TABLE align='center' class='InfoTable' cellspacing=0 cellpadding=4>" );
        Q.oh( 3, "<TR><TD>" );
        Q.oh( 4, "The following lists were extracted automatically from the VerbNet XML files.<BR>" );
        Q.oh( 4, "These files might also be of use: <A href='preps.txt'>Prepositions</A>," );
        Q.oh( 4, "<A href='synrestr-desc.txt'>Syntax Restrictions</A>," );
        Q.oh( 4, "<A href='../documents/selrestr_hierarchy.gif'>Selectional Restriction Hierarchy</A>,");
        Q.oh( 4, "<A href='../documents/themrole_hierarchy.pdf'>Thematic Role Hierarchy</A>");
        Q.oh( 3, "</TD></TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "" );

        // Print a table with all the lists.
        Q.oh( 2, "<TABLE align='center' class='RefTable' cellspacing=0 width='100%'>" );
        Q.oh( 3, "<TR valign='top'>" );

        printReferenceColumn( "General Thematic Roles", "GenThemRole", 1 );
        printReferenceColumn( "NP Thematic Roles", "NPThemRole", 2 );
        printReferenceColumn( "Selectional Restrictions", "SelRestr", 1 );
        printReferenceColumn( "Verb-Specific Features", "VerbFeatures", 2 );

        Q.oh( 3, "</TR>" );
        Q.oh( 3, "<TR valign='top'>" );

        printReferenceColumn( "Predicates", "Predicate", 1 );
        printReferenceColumnSpecial( "Frame Types" );

        // Print ref page info from the properties file into the last table cell.
        Q.oh( 4, "<TD width='25%' class='RefTable'>");
        Q.oh( 5, "<TABLE width='100%' cellspacing=0>" );
        Q.oh( 6, "<TR><TD class='RefGroup' style='border-bottom: 1px black dashed;'>Reference Page Info</TD></TR>" );
        Q.oh( 6, "<TR><TD style='text-align:justify'><P style='margin-left:10px; margin-right: 10px'>" );
        Q.oh( 7, getProperty("refPageInfo", "") );
        Q.oh( 6, "</P></TD></TR>" );
        Q.oh( 5, "</TABLE>" );
        Q.oh( 4, "</TD>" );

        Q.oh( 3, "</TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<BR>" );

        // Close the stream to the page (this inserts the footer).
        closeHTMLOutStream( pw, "vn/reference.php" );
    }

    /**
     * Prints an HTML TD element containing a selected word list.  The word lists
     * are constructed during the HTML generation phase (see {@link uvi.Sweeper} class).
     *
     * @param title the title for the column
     * @param list the word list to display
     * @see uvi.Generator#generateReferencePage()
     */
    private static void printReferenceColumn( String title, String list, int whichColor )
    {
    	String referencePage = "";
    	if (list.equals("GenThemRole") || list.equals("NPThemRole"))
    		referencePage = "../themroles/";
    	else if (list.equals("SynRestr"))
    		referencePage = "../synrestrs/";
    	else if (list.equals("SelRestr"))
    		referencePage = "../selrestrs/";
    	else if (list.equals("Predicate"))
    		referencePage = "../predicates/";
    	else if (list.equals("VerbFeatures"))
    		referencePage = "../verbfeatures/";
    	
        String color = (whichColor == 1) ? "RefTable" : "RefTable2";
        Q.oh( 4, "<TD width='25%' class='" + color + "'>" );
        Q.oh( 5, "<TABLE width='100%' cellspacing=0>" );
        Q.oh( 6, "<TR><TD class='RefGroup'>" + title + "</TD></TR>" );
        Q.oh( 6, "<TR><TD class='RefCounts' style='border-bottom: 1px black dashed;'><NOBR>" + refMap.getMemberCount(list) +
            " unique values / " + refMap.getMemberships(list) + " total uses</NOBR></TD></TR>" );
        Q.oh( 6, "<TR><TD>" );
        Q.oh( 7, "<BR>" );
        Q.oh( 7, "<UL>" );

        TreeMap<String, Integer> members = refMap.getMembers(list);

        if(members == null) {
            Q.oh( 8, "<LI><I>no occurrences</I></LI>" );
        } else {

            int mem = 0;
            for(String member : members.keySet()) {
                Integer count = members.get(member);
                String extra = "";
                if(mem == 0) {
                    extra = " uses";
                }
                String countStr = " <B>(" + count + extra + ")</B>";
                Q.oh( 8, "<LI><a href=\"" + referencePage + member.replace("-", "_") + ".php\" class=VerbLinks>" + member + countStr + "</a></LI>" );
                mem++;
            }

        }

        Q.oh( 7, "</UL>" );
        Q.oh( 7, "<BR>" );
        Q.oh( 6, "</TD></TR>" );
        Q.oh( 5, "</TABLE>" );
        Q.oh( 4, "</TD>" );
    }

    /**
     * Prints an HTML TD element containing the special word list for the frame
     * descriptions.  This special list is constructed during the HTML generation
     * phase (see {@link uvi.Sweeper} class).
     *
     * @param title the title for the column
     * @see uvi.Generator#generateReferencePage()
     */
    private static void printReferenceColumnSpecial( String title )
    {
        Q.oh( 4, "<TD width='50%' colspan='2' class='RefTable2'>" );
        Q.oh( 5, "<TABLE width='100%' cellspacing=0>" );
        Q.oh( 6, "<TR><TD class='RefGroup'>" + title + "</TD></TR>" );
        Q.oh( 6, "<TR><TD class='RefCounts'>Primary: " + refFrameIndMap.getMemberCount( "primary" ) +
            " unique values / " + refFrameIndMap.getMemberships( "primary" ) + " total uses (= # frames in VN)</TD></TR>" );
        Q.oh( 6, "<TR><TD class='RefCounts' style='border-bottom: 1px black dashed;'>Secondary: " + refFrameIndMap.getMemberCount( "secondary" ) +
            " unique values / " + refFrameIndMap.getMemberships( "secondary" ) + " total uses</TD></TR>" );
        Q.oh( 6, "<TR><TD>" );
        Q.oh( 7, "<BR>" );
        Q.oh( 7, "<UL>" );

        GroupComparator<String, String> comparator = new GroupComparator<String, String>() {
            public int compareTo(MembershipMap<String, String> map, String group1, String group2) {
                int mems1 = map.getMemberships(group1);
                int mems2 = map.getMemberships(group2);
                return mems2 - mems1;
            }
        };
        for(String group : refFrameBothMap.groups(comparator)) {
            int gcnt = refFrameBothMap.getMemberships(group);
            String ff = ( gcnt == 1 ) ? "frame" : "frames";
            Q.oh( 8, "<LI>" + group + " <B>(" + gcnt + " " + ff + ")</B>" );
            Q.oh( 9, "<UL>" );
            TreeMap<String, Integer> smems = refFrameBothMap.getMembers(group);
            for(String member : smems.keySet()) {
                int scnt = smems.get(member);
                String fc = " <B>(" + scnt + ")</B>";
                Q.oh( 10, "<LI>" + member + fc + "</LI>" );
            }
            Q.oh( 9, "</UL>" );
            Q.oh( 8, "</LI>" );
        }

        Q.oh( 7, "</UL>" );
        Q.oh( 7, "<BR>" );
        Q.oh( 6, "</TD></TR>" );
        Q.oh( 5, "</TABLE>" );
        Q.oh( 4, "</TD>" );
    }

    /**
     * Creates a page that displays the complete class hierarchy for VerbNet.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Sweeper#startVNCLASS(Node)
     * @see uvi.Sweeper#startVNSUBCLASS(Node)
     */
    private static void generateClassHierarchy()
    {
        if( flVerbose && !flQuiet ) {
            println( "Constructing class hierarchy \"vn/class-h.php\"..." );
        }

        // Open the stream to the page (this inserts the header).
        PrintWriter pw = createHTMLOutStream( "vn/class-h.php" );

        Q.setWriter( pw );

        Q.oh( 2, "" );
        Q.oh( 2, "<TABLE align='center' cellspacing=0 cellpadding=0 width='100%'>" );
        Q.oh( 3, "<TR><TD width='50%'>&nbsp;</TD><TD width='50%'>&nbsp;</TD></TR>" );
        Q.oh( 3, "<TR valign='top'><TD align='center'>" );
        Q.oh( 4, "<TABLE align='center'>" );
        Q.oh( 5, "<TR><TD class='ClasHCol'>Alphabetical</TD></TR>" );
        Q.oh( 5, "<TR valign='top'><TD>" );

        // Print the alphabetized column.
        for( int c = 0; c < classHierarchy.size(); c++ )
        {
            String cls = ( String ) classHierarchy.get( c );

            // Indent as appropriate.
            String indent = "";
            while( cls.charAt( 0 ) == '@' )
            {
                indent += "&nbsp;&nbsp;&nbsp;&nbsp;";
                cls = cls.substring( 1 );
            }

            String f = cls.substring( cls.indexOf( "$$" ) + 2 );
            String d = cls.substring( 0, cls.indexOf( "$$" ) );

            // Strip "vn/" off of the front of the file path.
            f = f.substring(3);

            Q.oh( 6, indent + "<A href='" + f + "#" + d + "'>" + d + "</A><BR>" );
        }

        Q.oh( 5, "</TD></TR>" );
        Q.oh( 4, "</TABLE>" );
        Q.oh( 3, "</TD><TD align='center'>" );
        Q.oh( 4, "<TABLE align='center'>" );
        Q.oh( 5, "<TR><TD class='ClasHCol'>By Class Number</TD></TR>" );
        Q.oh( 5, "<TR valign='top'><TD>" );

        // Modify the class name strings to what will be displayed for
        // the second column, and ignore subclasses.
        for( int c = classHierarchy.size() - 1; c >= 0; c-- )
        {
            String cls = ( String ) classHierarchy.get( c );
            String nu = "";

            // Remove subclasses from the list.
            if( cls.charAt( 0 ) == '@' )
            {
                classHierarchy.remove( c );
                continue;
            }

            // Grab all the number digits.
            for( int x = 0; x < cls.length(); x++ ) {
                if( "0123456789.".indexOf( cls.charAt( x ) ) != -1 ) {
                    nu += cls.charAt( x );
                } else if( !nu.equals( "" ) ) {
                    break;
                }
            }

            // Change the string in the array.
            classHierarchy.set( c, nu + ": " + cls );
        }

        // Sort the array by the beginning number.
        for( int a = 0; a < classHierarchy.size() - 1; a++ ) {
            for( int b = a + 1; b < classHierarchy.size(); b++ )
            {

                String sx = ( String ) classHierarchy.get( a );
                String sy = ( String ) classHierarchy.get( b );
                String nx = sx.substring( 0, sx.indexOf( ":" ) );  // Just the number, e.g. 9.10.3, 19
                String ny = sy.substring( 0, sy.indexOf( ":" ) );
                int compare = compareFirstNumber( nx, ny );

                if( compare > 0 )
                {
                    classHierarchy.set( b, sx );
                    classHierarchy.set( a, sy );
                }
                else if( compare == 0 && nx.indexOf( "." ) != -1 && ny.indexOf( "." ) != -1 )
                {
                    nx = nx.substring( nx.indexOf( "." ) + 1 );
                    ny = ny.substring( ny.indexOf( "." ) + 1 );
                    compare = compareFirstNumber( nx, ny );

                    if( compare > 0 )
                    {
                        classHierarchy.set( b, sx );
                        classHierarchy.set( a, sy );
                    }
                    else if( compare == 0 && nx.indexOf( "." ) != -1 && ny.indexOf( "." ) != -1 )
                    {
                        nx = nx.substring( nx.indexOf( "." ) + 1 );
                        ny = ny.substring( ny.indexOf( "." ) + 1 );
                        compare = compareFirstNumber( nx, ny );

                        if( compare > 0 )
                        {
                            classHierarchy.set( b, sx );
                            classHierarchy.set( a, sy );
                        }
                    }
                }
            }
        }

        // Print the sorted by-number column.
        for( int c = 0; c < classHierarchy.size(); c++ )
        {
            String cls = ( String ) classHierarchy.get( c );

            String f = cls.substring( cls.indexOf( "$$" ) + 2 );
            String d = cls.substring( 0, cls.indexOf( "$$" ) );

            // Strip "vn/" off of the front of the file path.
            f = f.substring(3);

            Q.oh( 6, "<A href='" + f + "'>" + d + "</A><BR>" );
        }

        Q.oh( 5, "</TD></TR>" );
        Q.oh( 4, "</TABLE>" );
        Q.oh( 3, "</TD></TR>" );
        Q.oh( 2, "</TABLE>" );

        // Close the stream to the page (this inserts the footer).
        closeHTMLOutStream( pw, "vn/class-h.php" );
    }

    /**
     * Helps with sorting the class hierarchy.
     */
    private static int compareFirstNumber( String nx, String ny )
    {
        String tx = ( nx.indexOf( "." ) == -1 ) ? nx : nx.substring( 0, nx.indexOf( "." ) );
        String ty = ( ny.indexOf( "." ) == -1 ) ? ny : ny.substring( 0, ny.indexOf( "." ) );
        while( tx.length() != 5 ) {
            tx = "0" + tx;
        }
        while( ty.length() != 5 ) {
            ty = "0" + ty;
        }
        return tx.compareTo( ty );
    }

    /**
     * Creates the content of the HTML output file using the source XML file and
     * the Java Document Object Model.  The XML file is parsed, and then HTML
     * is sent to the {@link uvi.Q} class, which was already initialized with the current
     * HTML output stream in {@link uvi.Generator#generateHTMLFiles()}.  The XML
     * tags are processed recursively using {@link uvi.Generator#processNode(Node)}.
     * All DTD validation errors are printed upon executing the
     * <CODE>db.parse( src );</CODE> statement (via XMLErrorHandler class).
     *
     * @param db the {@link javax.xml.parsers.DocumentBuilder} which will parse
     *        the source XML file into a {@link org.w3c.dom.Document}.
     * @param src the source XML file
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#processNode(Node)
     */
    private static void generateOneFile( DocumentBuilder db, File src )
    {
        String xmlFileName = src.getName();

        try
        {
            // Parse the XML source file.  This will print all the errors with validating
            // the XML file against the DTD specified inside.
            Document doc = db.parse( src );

            NodeList members = doc.getElementsByTagName( "VNCLASS" );

            if( members.getLength() == 0 ) {
                eprintln( "ERROR: No VNCLASS node exists in file \"" + xmlFileName + "\"." );
            } else
            {
                Element mainClass = ( Element ) members.item( 0 );

                if( !mainClass.getAttribute( "ID" ).equalsIgnoreCase( xmlFileName.substring( 0, xmlFileName.lastIndexOf( "." ) ) ) ) {
                    eprintln( "WARNING: VNCLASS node ID value does not correspond to XML file name in file \"" + xmlFileName + "\"." );
                }

                // Print the HTML for the document root node (aka, generate the HTML
                // for the entre XML document).
                processNode( xmlFileName, mainClass );
            }
        }

        // This exception could occur if the DTD file is not present in the
        // same directory as the XML file being parsed.
        catch( FileNotFoundException fnfe )
        {
            eprintln( "ERROR: " + fnfe.getMessage() + "." );
            eprintln( "   Most likely the file \"" + xmlFileName + "\" references the missing file above." );
        }
        catch( Exception e )
        {
            eprintln( "ERROR: [" + xmlFileName + "] " + e.getMessage() + "." );
        }
    }

    /**
     * This method starts by outputting the HTML code you would want to output
     * upon encountering the start of the given node, then recursively prints all the
     * HTML associated with this node's children nodes, and finally outputs the
     * HTML needed for the close of the given node.  The process by which this
     * method outputs the 'start' or 'end' HTML for a given node is subtle.
     * Reflection is used.  For a node with tag name NODE, methods called
     * 'startNODE' and 'endNODE' are located in the class called 'Sweeper'.
     * If the method is found, it is executed.  If it is not found, nothing happens.
     *
     * @param n the xml node for which to generate HTML code
     * @see uvi.Generator#executeHTMLMethod(String, Node)
     * @see uvi.Sweeper
     */
    private static void processNode( String fileName, Node n )
    {
        executeHTMLMethod( fileName, "start", n );

        NodeList kids = n.getChildNodes();

        for( int k = 0; k < kids.getLength(); k++ ) {
            processNode( fileName, kids.item( k ) );             // Recursively call each child.
        }

        executeHTMLMethod( fileName, "end", n );
    }

    /**
     * Executes a method in the {@link uvi.Sweeper} class based on a node tag name
     * (i.e.&nbsp;'VNCLASS') and a 'start' or 'end' flag.  Each of the methods in Sweeper,
     * startVNCLASS, endVNCLASS, startMEMBERS, endMEMBERS, etc., produce HTML code.
     * This method just calls the right method based on the current node being
     * processed by {@link uvi.Generator#processNode(Node)}.
     *
     * @param which either the value 'start' or 'end', the string to prepend to the
     *        tag name when locating the method in {@link uvi.Sweeper}
     * @param n the node whose tag name should be located in the Sweeper methods
     * @see uvi.Sweeper
     */
    private static void executeHTMLMethod( String fileName, String which, Node n )
    {

        // Verify flag is a valid token.
        if( !which.equals( "start" ) && !which.equals( "end" ) )
        {
            eprintln( "ERROR: Invalid \"which\" parameter.  Possible values are \"start\" and \"end\"." );
            return;
        }

        // Use the appropriate reflection methods to execute the desired method.
        try
        {
            Class<?>[] classes = { Node.class };

            Method m = Sweeper.class.getDeclaredMethod( which + n.getNodeName(), classes );

            Object[] args = { n };

            m.invoke( null, args );
        }
        catch( NoSuchMethodException nsme )
        {
            // Do nothing - this is not an error condition.  It merely means the developer has not
            // specified any action for this node at this point (i.e. 'start' or 'end').
        }
        catch( SecurityException se )
        {
            eprintln( "ERROR: ["+ fileName + "; Security/refl] " + se.getMessage() + "." );
        }
        catch( Exception e )
        {
            e.printStackTrace();
            eprintln( "ERROR: ["+ fileName + "; Generic/refl] " + e.getMessage() + "." );
        }
        catch( Error err )
        {
            eprintln( "ERROR: ["+ fileName + "; Generic/refl-err] " + err.getMessage() + "." );
        }
    }

    private static void loadThemRoleLinks(){
    	for (VerbNetClass vnc : VerbNet.getClasses()){
    		for (Role r : vnc.getRoles()){
    			Index.addLink(vnc.getName() + "-" + vnc.getId(), DS_THEMROLE, r.getRoleValue().toString(), "themroles/" + r.getRoleValue().toString() + ".php" );
    		}
    	}
    	for (RoleValue r : RoleValue.values()){
    		Index.addLink(r.toString(), DS_THEMROLE, r.toString(), "themroles/" + r.toString() + ".php");
    	}
    }
    
    private static void loadPredicateLinks(){
    	Set<String> added = new HashSet<String>();
    	for (VerbNetClass vnc : VerbNet.getClasses()){
    		for (SemanticFrame sf : vnc.getSemanticFrames()){
    			for (Predicate p : sf.predicates){
    				if (!added.contains(vnc.getName() + "-" + vnc.getId() + "-" + p.value)){
    					Index.addLink(vnc.getName() + "-" + vnc.getId(), DS_PREDICATE, p.value, "predicates/" + p.value + ".php");
    					added.add(vnc.getName() + "-" + vnc.getId() + "-" + p.value);
    				}
    				if (!added.contains(p.value)){
    					Index.addLink(p.value, DS_PREDICATE, p.value, "predicates/" + p.value + ".php");
    					added.add(p.value);
    				}
    			}
    		}
    	}
    }
    
    /**
     * Reads in data from an alternate data source (one other than VerbNet).
     * Based on the source requested via the argument, a file name is chosen
     * and a its data is extracted into the appropriate internal data structure.
     * Each file has its own format and is accounted for herein.
     *
     * @param type which file to read in and add to the index.  This should be
     *        one of the DS_* constants.
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Index#addLink(String, int, String, String)
     * @see uvi.VN_FN_Map#addMapPair(String, String, String)
     * @see uvi.WordNet
     */
    private static void addOthers( int type )
    {
        String fileName = "";

        // Decide which file name should be used, based on the requested source.
        switch( type )
        {
            case DS_PROPBANK: fileName = "propbank.s"; break;
            case DS_FRAMENET: fileName = "framenet.s"; break;
            case DS_GROUPING: fileName = "grouping.s"; break;
            case DS_WORDNET:  fileName = "wordnet.s";  break;
            case DS_VN_FN:    fileName = "vn-fn.s";    break;
            case DS_VN_CYC:   fileName = "vn-cyc.s";   break;

            default:
                eprintln( "ERROR: Invalid Generator.DS_* constant." );
                return;
        }

        try
        {

            // If the data source is an XML file...
            if( type == DS_FRAMENET || type == DS_VN_FN || type == DS_VN_CYC )
            {

                // Create essential JDOM objects.
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

                // So XML files are checked against their DTD.  This assumes that
                // there is an inline DTD inside the file, or that the XML file
                // references an external DTD file.
                dbf.setValidating( true );

                DocumentBuilder db = dbf.newDocumentBuilder();

                XMLErrorHandler xmlErrH = new XMLErrorHandler();

                // So custom error messages can be printed (see XMLErrorHandler).
                db.setErrorHandler( xmlErrH );

                // Let the JDOM XML error handler know what input file is currently
                // being processed so it can report which file contained the error.
                xmlErrH.setActiveFile( fileName );

                Document doc = db.parse( ( File ) sFiles.get( fileName ) );

                // Scan the FrameNet XML tree.
                if( type == DS_FRAMENET )
                {
                    NodeList members = doc.getElementsByTagName( "lexical-entry" );

                    for( int m = 0; m < members.getLength(); m++ )
                    {
                        Element e = ( Element ) members.item( m );

                        String vb = e.getAttribute( "name" ).replaceAll( " ", "_" );      // Disallow spaces for searchable index reasons.
                        String fm = e.getAttribute( "frame" );

                        vb = vb.substring( 0, vb.length() - 2 );     // Strip '.v'

                        String fnUrlPattern = getProperty("fnUrlPattern", "ErrorNoFnUrlPatternDefined-{1}");
                        fnUrlPattern = fnUrlPattern.replaceAll("\\{1\\}", fm);

                        // Add the link to the index.
                        Index.addLink( vb, DS_FRAMENET, "(fn " + fm + ")", fnUrlPattern );
                    }
                }

                // Scan the VN-FN XML tree.
                else if( type == DS_VN_FN )
                {
                    NodeList members = doc.getElementsByTagName( "vncls" );

                    for( int m = 0; m < members.getLength(); m++ )
                    {
                        Element e = ( Element ) members.item( m );

                        String cl = e.getAttribute( "class" );
                        String vn = e.getAttribute( "vnmember" );
                        String fn = e.getAttribute( "fnframe" );

                        // Add the class-verb-frame tuple to a special data structure
                        // designed to hold the VN-FN information.  Only add those
                        // entries that aren't 'not available' or 'different sense'.
                        if( !fn.equals( "NA" ) && !fn.equals( "DS" ) && !fn.equals( "" ) ) {
                            VN_FN_Map.addMapPair( cl, vn, fn );
                        }

                        if( fn.equals( "" ) ) {
                            eprintln( "WARNING: VN-FN mapping should not have a blank fnframe attribute (" + cl + "/" + vn + ")." );
                        }
                    }
                }

                // Scan the VN-CYC XML tree.  (never finished)
                else if( type == DS_VN_CYC )
                {
                    NodeList members = doc.getElementsByTagName( "tuple" );

                    for( int m = 0; m < members.getLength(); m++ )
                    {
                        Element e = ( Element ) members.item( m );

                        String vnc = e.getAttribute( "vnc" );
                        String vnm = e.getAttribute( "vnm" );
                        String vnf = e.getAttribute( "vnf" );
                        String crl = e.getAttribute( "crule" );

                        // finish...
                    }
                }
            }

            // Scan the PropBank file.
            else if( type == DS_PROPBANK )
            {
                BufferedReader in = new BufferedReader( new FileReader( ( File ) sFiles.get( fileName ) ) );
                String line;

                // Read the desired file line-by-line.
                while( ( line = in.readLine() ) != null )
                {

                    // Split the 'verb,url' pair into its constituent parts.
                    String[] parts = line.split( "," );

                    // Add a PropBank entry to the index.
                    Index.addLink( parts[ 0 ], DS_PROPBANK, "(PropBank)", parts[ 1 ] );
                }

                in.close();
            }

            // Scan the Grouping file.
            else if( type == DS_GROUPING )
            {
                BufferedReader in = new BufferedReader( new FileReader( ( File ) sFiles.get( fileName ) ) );
                String line;

                // Read the desired file line-by-line.
                while( ( line = in.readLine() ) != null )
                {

                    // Split the 'verb,url' pair into its constituent parts.
                    String[] parts = line.split( "," );

                    // Add a Grouping entry to the index.
                    Index.addLink( parts[ 0 ], DS_GROUPING, "(Grouping)", parts[ 1 ] );
                }

                in.close();
            }

            // Scan the WordNet file (wordnet.s should be the index.sense file).
            else if( type == DS_WORDNET )
            {
                if( flVerbose && !flQuiet ) {
                    println( "   --> Step 1: VerbNet pre-scan for just members..." );
                }

                // Look for all the sense keys that will need sense numbers looked up.
                WordNet.preScan( xmlFiles );

                if( flVerbose && !flQuiet ) {
                    println( "   --> Step 2: Scan index.sense (wordnet.s) for required sense numbers..." );
                }

                // Look up the sense numbers for the sense keys that exist in VerbNet.
                WordNet.loadSenseNumbers( ( File ) sFiles.get( "wordnet.s" ) );
            }
        }
        catch( Exception e )
        {
            eprintln( "ERROR: Cannot read from file \"" + fileName + "\". " + e.getMessage() );
        }
    }

    /**
     * Creates the main index page and index pages for each letter of the alphabet.
     * The HTML generated is based on the {@link uvi.Index} class which should
     * contain all links to be displayed.  The VerbNet links are added to index in
     * {@link uvi.Generator#generateOneFile(DocumentBuilder, File)} and the PropBank and FrameNet
     * links are added in {@link uvi.Generator#addOthers(int)}.  This method also
     * generates the <I>searchable index files</I> used by the search mechanism
     * to provide fast search results.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Sweeper#startMEMBER(Node)
     */
    private static void generateIndexFiles()
    {
        String iName = "index.php";

        // Print the current file being generated.
        if( flVerbose && !flQuiet ) {
            println( "Processing index file \"" + iName + "\"..." );
        }

        // Begin with the HTML for the white box that the index letters
        // will go in.
        String indexHTML = "<TABLE class='IndexLetterBox' align='center' cellspacing=0 cellpadding=7>" +
        "<TR><TD align='center'>";
        String indexHTML2 = "<TABLE class='IndexLetterBox' align='center' cellspacing=0 cellpadding=7>" +
        "<TR><TD align='center'>";

        // Add a hyperlink letter for each letter for which there
        // exist verbs.
        for( int a = 0; a < Index.index.length; a++ )
        {
            if( Index.index[ a ] != null )
            {
                char L = ( char )( a + 65 );

                indexHTML += "<A href='index/" + L + ".php'>" + L + "</A>";
                indexHTML2 += "<A href='" + L + ".php'>" + L + "</A>";

                if( a != Index.index.length - 1 ) {
                    indexHTML += "&nbsp;&nbsp;";           // Add spaces between letters.
                    indexHTML2 += "&nbsp;&nbsp;";           // Add spaces between letters.
                }
            }
        }

        indexHTML += "</TD></TR></TABLE>";
        indexHTML2 += "</TD></TR></TABLE>";

        // Open the stream to the main index page.
        Q.setWriter( createHTMLOutStream( iName, "header.s", "index.s" ) );

        // Write the index table to the main index page and close the stream.
        if( Q.getWriter() != null )
        {

            Q.oh( 2, indexHTML );
            Q.oh( 2, "" );
            Q.oh( 2, "<BR><BR>" );
            Q.oh( 2, "" );

            String banner = getProperty( "indexBanner", "" );
            if( !banner.equals( "" ) ) {
                Q.oh( 2, "<TABLE class='IndexBannerBox' align='center' cellspacing=0 cellpadding=7>" );
                Q.oh( 3, "<TR><TD align='center'>" );
                Q.oh( 4, banner );
                Q.oh( 3, "</TD></TR>" );
                Q.oh( 2, "</TABLE>" );
                Q.oh( 2, "<BR><BR>" );
                Q.oh( 2, "" );
            }

            Q.oh( 2, "<TABLE class='TotalsTable' align='center' cellspacing=0 cellpadding=13>" );
            Q.oh( 3, "<TR>" );
            Q.oh( 4, "<TD>" );
            Q.oh( 5, "<FONT class='TotalsTableTitle'>The index has...</FONT>" );
            if(!flNoIndexVn) {
                Q.oh( 5, "<BR><FONT class='TotalsTableNum'>" + Index.getNumVerbs( -1 ) + "</FONT> total verbs represented<BR>" );
                Q.oh( 5, "<FONT class='TotalsTableNum'>" + Index.getNumVerbs( DS_VERBNET ) + "</FONT> total VerbNet links<BR>" );
                Q.oh( 5, "<FONT class='TotalsTableNum'>" + xmlFiles.length + "</FONT> total VerbNet main classes<BR>" );
                Q.oh( 5, "<FONT class='TotalsTableNum'>" + totalSubclasses + "</FONT> total VerbNet subclasses" );
            }
            if(!flNoIndexPb) {
                Q.oh( 5, "<BR><FONT class='TotalsTableNum'>" + Index.getNumVerbs( DS_PROPBANK ) + "</FONT> total PropBank links" );
            }
            if(!flNoIndexFn) {
                Q.oh( 5, "<BR><FONT class='TotalsTableNum'>" + Index.getNumVerbs( DS_FRAMENET ) + "</FONT> total FrameNet links" );
            }
            if(!flNoIndexSg) {
                Q.oh( 5, "<BR><FONT class='TotalsTableNum'>" + Index.getNumVerbs( DS_GROUPING ) + "</FONT> total Grouping links" );
            }
            Q.oh( 4, "</TD>" );
            Q.oh( 3, "</TR>" );
            Q.oh( 2, "</TABLE>" );
            Q.oh( 2, "" );
            Q.oh( 2, "<BR>" );

            closeHTMLOutStream( Q.getWriter(), iName );
        }

        // Create the HTML index page for each letter.
        for( int a = 0; a < Index.index.length; a++ )
        {
            ArrayList thisList = Index.index[ a ];

            // If this letter has a corresponding index array, then at least
            // one verb must have been added to the index during the course
            // of the program.  The 'index' array in the 'Index' class holds
            // ArrayList objects.  But the ArrayList is only instaniated when
            // the first verb of that letter is being added to the index.
            if( thisList != null )
            {
                char L = ( char )( a + 65 );       // The capital letter.

                String lName = "index/" + L + ".php";         // The file name for the web page.
                String sName = "search/search-index-" + L;   // The file name for the searchable index file.

                // Print the current file being generated.
                if( flVerbose && !flQuiet ) {
                    println( "Processing index file \"" + lName + "\"..." );
                }

                // Open the stream to the index file for this letter.
                Q.setWriter( createHTMLOutStream( lName ) );

                // Open the stream to the searchable index file.
                PrintWriter search = createHTMLOutStream( sName, null );

                if( Q.getWriter() != null )
                {

                    // Write the image and the index table generated earlier.
                    Q.oh( 2, "<BR>" );
                    Q.oh( 2, "" );
                    Q.oh( 2, "<DIV class='IndexImageCenter'>" );
                    Q.oh( 3, "<IMG src='../images/" + L + ".jpg' width=89 height=72 border=1 alt='" + L + "'>" );
                    Q.oh( 2, "</DIV>" );
                    Q.oh( 2, "" );
                    Q.oh( 2, "<BR>" );
                    Q.oh( 2, "" );
                    Q.oh( 2, indexHTML2 );
                    Q.oh( 2, "" );
                    Q.oh( 2, "<TABLE width='80%' cellspacing=0 cellpadding=2 align='center'>" );

                    // Add a row for a post comments link and a jump to classes link.
                    Q.oh( 3, "<TR>" );
                    Q.oh( 4, "<TD width='14%'>&nbsp;</TD>" );
                    Q.oh( 4, "<TD width='86%' align='right'>" );
                    Q.oh( 5, "<A href='javascript:postComment( \"" + lName + "\", \"generic-comments\" );'>Post Generic Comment</A> | " );
                    Q.oh( 5, "<A href='#classes'>Go To Classes</A>" );
                    Q.oh( 4, "</TD>" );
                    Q.oh( 3, "</TR>" );

                    // Print the entry for each verb for this letter.
                    for( int e = 0; e < thisList.size(); e++ )
                    {

                        // Grab the verb entry.
                        Index.Entry ie = ( Index.Entry ) thisList.get( e );

                        // Alternate colors for the rows.
                        String color = ( e % 2 == 0 ) ? "EntryColor1" : "EntryColor2";

                        // Initialize the CSS for the side borders.
                        String leftBorder = "border-left: 1px #000000 solid;";
                        String rightBorder = "border-right: 1px #000000 solid;";

                        // If this is the first entry, add the CSS for the top border.
                        if( e == 0 )
                        {
                            leftBorder += "border-top: 1px #000000 solid;";
                            rightBorder += "border-top: 1px #000000 solid;";
                        }

                        // If this is the last entry, add the CSS for the bottom border.
                        if( e == thisList.size() - 1 )
                        {
                            leftBorder += "border-bottom: 1px #000000 solid;";
                            rightBorder += "border-bottom: 1px #000000 solid;";
                        }

                        // Print the empty cell, the cell with the verb, and begin the details cell.
                        Q.oh( 3, "<TR valign='top' class='" + color + "'>" );
                        Q.oh( 4, "<TD width='14%' style='" + leftBorder + "'>" + ie.verb.replaceAll( "_", " " ) + "</TD>" );
                        Q.oh( 4, "<TD width='86%' style='" + rightBorder + "'>" );

                        // Print each Link for this verb.  A verb will have various
                        // links for it over the course of the program.
                        for( int l = 0; l < ie.links.size(); l++ )
                        {
                            Index.Link il = ( Index.Link ) ie.links.get( l );

                            // Check to see if this link has already been printed, and show
                            // a warning if need be.  This happens if a VerbNet verb is listed
                            // twice in the same class for example.
                            for( int l2 = l - 1; l2 >= 0; l2-- )
                            {
                                Index.Link itest = ( Index.Link ) ie.links.get( l2 );

                                if( itest.text.equals( il.text ) )
                                {
                                    if( itest.type == DS_VERBNET ) {
                                        eprintln( "WARNING: [" + il.text + "] Verb '" + ie.verb + "' appears in this subclass more than once." );
                                    } else {
                                        eprintln( "WARNING: [" + il.text + "] Verb '" + ie.verb + "' has one or more identical links." );
                                    }
                                }
                            }

                            String extra;

                            // Allow a VerbNet subclass link to jump right to the right
                            // spot on the target page.
                            if( il.type == DS_VERBNET ) {
                                extra = "#" + il.text;
                            } else {
                                extra = "";
                            }

                            // Print the link with a comma.
                            String cm = ( l == ie.links.size() - 1 ) ? "" : ", ";
                            String relDir = (il.type == DS_VERBNET) ? "../" : "";
                            Q.oh( 5, "<A href='"  + relDir + il.link + extra + "'><NOBR>" + il.text + "</NOBR></A>" + cm );

                            // Write this link to the searchable index file.
                            if( search != null )
                            {
                                String src   = "";
                                String label = "";

                                // Choose type token and label.
                                switch( il.type )
                                {
                                    case DS_VERBNET:  src = "V"; label = il.text;                        break;
                                    case DS_PROPBANK: src = "P"; label = ie.verb + ".v";                 break;
                                    case DS_FRAMENET: src = "F"; label = computeFrameNetLabel(il.link);  break;
                                    case DS_GROUPING: src = "G"; label = ie.verb + ".v";                 break;
                                    case DS_THEMROLE: src = "R"; label = il.text;						 break;
                                    case DS_PREDICATE: src = "PR"; label = il.text;						 break;
                                }

                                // Write verb, source, label, and link to file.
                                search.println( ie.verb + " " + src + " " + label + " " + il.link );
                            }
                        }

                        // End the row for the entry
                        Q.oh( 4, "</TD>" );
                        Q.oh( 3, "</TR>" );
                    }

                    int classCount = 0;

                    // Count the number of classes that start with this letter.
                    for( int x = 0; x < xmlFiles.length; x++ ) {
                        if( xmlFiles[ x ].getName().toUpperCase().charAt( 0 ) == L ) {
                            classCount++;
                        }
                    }

                    // Add total row.
                    Q.oh( 3, "<TR>" );
                    Q.oh( 4, "<TD width='100%' colspan=2 class='TotalRow'>" +
                        "Total verbs represented for this letter: " + thisList.size() + "</TD>" );
                    Q.oh( 3, "</TR>" );

                    // Add blank row.
                    Q.oh( 3, "<TR>" );
                    Q.oh( 4, "<TD width='100%' colspan=2>&nbsp;</TD>" );
                    Q.oh( 3, "</TR>" );

                    if(!flNoIndexVn) {

                        // Show title for next section.
                        Q.oh( 3, "<TR>" );
                        Q.oh( 4, "<TD width='100%' colspan=2 class='YesCaps'>" +
                        "<A name='classes'></A>VerbNet class names for this letter</TD>" );
                        Q.oh( 3, "</TR>" );

                        if( classCount == 0 )
                        {
                            Q.oh( 3, "<TR valign='middle'>" );
                            Q.oh( 4, "<TD width='100%' colspan=2 class='NoClassNames'>" +
                            "&nbsp;<FONT class='AbsenceOfItems'>No class names for this letter</FONT></TD>" );
                            Q.oh( 3, "</TR>" );
                        }
                        else
                        {
                            int thisLetterCount = 0;

                            // Add rows for the class names that start with this letter.
                            for( int x = 0; x < xmlFiles.length; x++ )
                            {
                                String fName = xmlFiles[ x ].getName();

                                if( fName.toUpperCase().charAt( 0 ) == L )
                                {
                                    String hasSubC = "";

                                    thisLetterCount++;

                                    String cName = fName.substring( 0, fName.lastIndexOf( "." ) );

                                    // Alternate colors for the rows.
                                    String color = ( thisLetterCount % 2 == 1 ) ? "EntryColor1" : "EntryColor2";

                                    // Initialize the CSS for the side borders.
                                    String border = "border-left: 1px #000000 solid;border-right: 1px #000000 solid;";

                                    // If this is the first entry, add the CSS for the top border.
                                    if( thisLetterCount == 1 ) {
                                        border += "border-top: 1px #000000 solid;";
                                    }

                                    // If this is the last entry, add the CSS for the bottom border.
                                    if( thisLetterCount == classCount ) {
                                        border += "border-bottom: 1px #000000 solid;";
                                    }

                                    // If the main class in this XML file has subclasses, add a message.
                                    if( classesWithSubclasses.indexOf( "@@" + fName.replaceAll( "\\.xml", ".php" ) + "@@" ) != -1 ) {
                                        hasSubC = "<FONT class='HasSubclasses'>- has subclass(es)</FONT>";
                                    }

                                    Q.oh( 3, "<TR class='" + color + "'>" );
                                    Q.oh( 4, "<TD width='100%' colspan=2 style='" + border + "'>");
                                    Q.oh( 6, "&nbsp;<A href='../vn/" + fName.replaceAll( "\\.xml", ".php" ) +
                                        "'>" + cName + "</A> " + hasSubC + "</TD>" );
                                    Q.oh( 3, "</TR>" );

                                    // Write the classes to the searchable index file.
                                    if( search != null ) {
                                        search.println( fName.replaceAll("\\.xml", "") + " C " + cName + " vn/" + fName.replaceAll( "\\.xml", ".php" ) );
                                    }
                                }
                            }

                            // Add total row.
                            Q.oh( 3, "<TR>" );
                            Q.oh( 4, "<TD width='100%' colspan=2 class='TotalRow'>" +
                                "Total classes represented for this letter: " + thisLetterCount + "</TD>" );
                            Q.oh( 3, "</TR>" );
                        }
                    }

                    // Close the table for this letter index.
                    Q.oh( 2, "</TABLE>" );
                    Q.oh( 2, "" );
                    Q.oh( 2, "<BR>" );
                    Q.oh( 2, "" );
                    Q.oh( 2, "<FORM name='frmPostComment' method='post' action='../postcomment.php'>" );
                    Q.oh( 3, "<INPUT type='hidden' name='txtFileName'>" );
                    Q.oh( 3, "<INPUT type='hidden' name='txtClassName'>" );
                    Q.oh( 2, "</FORM>" );

                    // Close the stream to the index page.
                    closeHTMLOutStream( Q.getWriter(), lName );

                    // Close the stream to the searchable index file.
                    if( search != null ) {
                        search.close();
                    }
                }       // if( Q.getWriter() != null )
            }          // if( thisList != null )
        }             // for( int a = 0; a < Index.index.length; a++ )
    }

    private static String computeFrameNetLabel(String url) {
        String fnUrlPattern = getProperty("fnUrlPattern", "ErrorNoFnUrlPatternDefined-{1}");
        int tokenIndex = fnUrlPattern.indexOf("{1}");
        int numEndChars = fnUrlPattern.length() - tokenIndex - 3;
        return url.substring(tokenIndex, url.length() - numEndChars);
    }

    ///////////////////////////
    // Opening/Closing Files //
    ///////////////////////////

    /**
     * Creates a new output file and returns an output stream to it.  The
     * new file will be initialized with the lines of text from the file
     * <CODE>header.s</CODE>.  If the overwrite flag is
     * not specified then the file cannot already exist, otherwise the
     * method will fail and return <CODE>null</CODE>.  This method
     * also replacees all special sequences in the initial file to the
     * appropriate values.
     *
     * @param fileName the name of the file to create and open
     * @return an output stream associated with the requested file, or
     *         <CODE>null</CODE> if any error is encountered
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     */
    static PrintWriter createHTMLOutStream( String fileName )
    {
        return createHTMLOutStream( fileName, "header.s" );
    }

    /**
     * Creates a new output file and returns an output stream to it.  The
     * new file will be initialized with the lines of text from the file
     * with name <CODE>startWithFile</CODE>.  If the overwrite flag is
     * not specified then the file cannot already exist, otherwise the
     * method will fail and return <CODE>null</CODE>.  This method
     * also replaces all special sequences in the initial file to the
     * appropriate values.
     *
     * @param fileName the name of the file to create and open
     * @param startWithFile thhe name of the file which should be
     *        loaded into the first file after it is created.
     * @return an output stream associated with the requested file, or
     *         <CODE>null</CODE> if any error is encountered
     * @see uvi.Generator#copySupplementalFiles()
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     */
    private static PrintWriter createHTMLOutStream( String fileName, String startWithFile )
    {
        return createHTMLOutStream( fileName, startWithFile, null );
    }

    /**
     * Creates a new output file and returns an output stream to it.  The
     * new file will be initialized with the lines of text from the files
     * with names <CODE>startWithFile</CODE> and <CODE>startWithFile2</CODE>.
     * If the overwrite flag is
     * not specified then the files cannot already exist, otherwise the
     * method will fail and return <CODE>null</CODE>.  This method
     * also replacees all special sequences in the initial file to the
     * appropriate values.
     *
     * @param fileName the name of the file to create and open
     * @param startWithFile the name of the file which should be
     *        loaded into the first file after it is created
     * @param startWithFile2 the name of the second file whose text
     *        is to be appended to the newly opened file
     * @return an output stream associated with the requested file, or
     *         <CODE>null</CODE> if any error is encountered
     * @see uvi.Generator#copySupplementalFiles()
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     */
    private static PrintWriter createHTMLOutStream( String fileName, String startWithFile, String startWithFile2 )
    {
        File outFile = new File(outDir, fileName);

        // Skip this file if it exists and the overwrite flag has not been specified.
        if( outFile.exists() && !flOverwrite )
        {
            eprintln( "ERROR: Output file \"" + fileName + "\" already exists and overwrite not specified, skipping." );
            return null;
        }

        PrintWriter pw;

        // Create a blank file (superfluous) and attempt to open the stream to the new file.
        try
        {
            pw = new PrintWriter( new BufferedWriter( new FileWriter( outFile ) ) );
        }
        catch( Exception e )
        {
            eprintln( "ERROR: Problem opening output file \"" + fileName + "\".  " + e.getMessage() );

            pw = null;
        }

        // Write all the lines from the initialization file(s) to the new file.
        try
        {
            BufferedReader header;
            String line;

            // Append the first file to the new file (this will almost always be provided).
            if( startWithFile != null )
            {
                header = new BufferedReader( new FileReader( ( File ) sFiles.get( startWithFile ) ) );

                while( ( line = header.readLine() ) != null )
                {
                    line = replaceSpecialSequences( line, fileName );

                    pw.println( line );
                }

                header.close();
            }

            // Append the text in the second file, if provided, to the new file.
            if( startWithFile2 != null )
            {
                header = new BufferedReader( new FileReader( ( File ) sFiles.get( startWithFile2 ) ) );

                while( ( line = header.readLine() ) != null )
                {
                    line = replaceSpecialSequences( line, fileName );

                    pw.println( line );
                }

                header.close();
            }
        }
        catch( Exception e )
        {
            String f2 = "";

            if( startWithFile2 != null ) {
                f2 = ",FromFile2=" + startWithFile2;
            }

            eprintln( "ERROR: Problem contructing file \"" + fileName + "\".  [FromFile1=" + startWithFile + f2 + "]");
            pw = null;
        }

        return pw;
    }

    /**
     * Closes the output stream to a file.  The files whose streams are closed with
     * this method are the VerbNet class files, main index file, and letter index files.
     * This method writes the file <CODE>footer.s</CODE> to each file before closing it.
     *
     * @param outWriter the output stream to be closed
     * @param fileName the original name of the file to which this output
     *        stream was opened.  This is required only so
     *        {@link uvi.Generator#replaceSpecialSequences(String, String)} will know how to
     *        replace the special sequences in <CODE>footer.s</CODE>
     * @see uvi.Generator#generateHTMLFiles()
     * @see uvi.Generator#generateIndexFiles()
     * @see uvi.Generator#replaceSpecialSequences(String, String)
     */
    static void closeHTMLOutStream( PrintWriter outWriter, String fileName )
    {
        try
        {
            BufferedReader footer = new BufferedReader( new FileReader( ( File ) sFiles.get( "footer.s" ) ) );
            String line;

            while( ( line = footer.readLine() ) != null )
            {
                line = replaceSpecialSequences( line, fileName );

                outWriter.println( line );
            }

            footer.close();
        }
        catch( Exception e )
        {
            eprintln( "ERROR: Problem opening supplemental file \"footer.s\".  " + e.getMessage() );
        }

        outWriter.close();
    }

    /**
     * Replaces all special sequences in a given line of text with corresponding
     * real-world values.  The available special sequences are: <CODE>@@browser-title@@</CODE>,
     * <CODE>@@page-title@@</CODE>, <CODE>@@license@@</CODE>, <CODE>@@date@@</CODE>, and
     * <CODE>@@time@@</CODE>.  More details about these are supplied in the README file.
     *
     * @param line the line which requires replacement of any existing special sequences
     * @param fileName the name of the file that the line came from.  This will be
     *        used to determine exactly how to replace certain sequences, since
     *        different files will need to display different information around their
     *        borders.
     * @see uvi.Generator#createHTMLOutStream(String, String, String)
     * @see uvi.Generator#closeHTMLOutStream(PrintWriter, String)
     */
    private static String replaceSpecialSequences( String line, String fileName )
    {
        String n;
        String flavor = getProperty("vnFlavorName", "VerbNet");

        // Just examine the file name portion (there could be
        // a directory on here).
        fileName = fileName.substring(fileName.lastIndexOf('/') + 1);

        // Remove the extension of a file name to get an OK title for the file.
        if( fileName.lastIndexOf( "." ) == -1 ) {
            n = fileName;
        } else {
            n = fileName.substring( 0, fileName.lastIndexOf( "." ) );
        }

        // Replace the special tokens based on whether the file in question
        // is an index page or a VerbNet page.
        if( fileName.matches( "index\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", "." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index" );
            line = line.replaceAll( "@@page-title@@",    "&nbsp;" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='vn/reference.php'>Reference</A> | <A href='vn/class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }

            String contact = "<A href='contact.php'>Contact</A>";
            if( !flNoInsp ) {
                contact += " | <A href='inspector/'>Inspector</A>";
            }
            if( !flNoVxC  ) {
                contact += " | <A href='vxc/'>VxC</A>";
            }
            if( !flNoGen  ) {
                contact += " | <A href='generator/'>Generator</A>";
            }

            line = line.replaceAll( "@@license@@", contact );
        }
        else if( fileName.matches( "[A-Z]\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", ".." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index: " + n );
            line = line.replaceAll( "@@page-title@@",    "Unified Verb Index" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='../vn/reference.php'>Reference</A> | <A href='../vn/class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }

            String contact = "<A href='contact.php'>Contact</A>";
            if( !flNoInsp ) {
                contact += " | <A href='../inspector/'>Inspector</A>";
            }
            if( !flNoVxC  ) {
                contact += " | <A href='../vxc/'>VxC</A>";
            }
            if( !flNoGen  ) {
                contact += " | <A href='../generator/'>Generator</A>";
            }

            line = line.replaceAll( "@@license@@", contact );
        }
        else if( fileName.matches( "contact\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", "." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index: Contact" );
            line = line.replaceAll( "@@page-title@@",    "Contact Information" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='vn/reference.php'>Reference</A> | <A href='vn/class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }

            String contact = "<A href='contact.php'>Contact</A>";
            if( !flNoInsp ) {
                contact += " | <A href='inspector/'>Inspector</A>";
            }
            if( !flNoVxC  ) {
                contact += " | <A href='vxc/'>VxC</A>";
            }
            if( !flNoGen  ) {
                contact += " | <A href='generator/'>Generator</A>";
            }

            line = line.replaceAll( "@@license@@", contact );
        }
        else if( fileName.matches( "search\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", "." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index: Search" );
            line = line.replaceAll( "@@page-title@@",    "Search" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='vn/reference.php'>Reference</A> | <A href='vn/class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }

            String contact = "<A href='contact.php'>Contact</A>";
            if( !flNoInsp ) {
                contact += " | <A href='inspector/'>Inspector</A>";
            }
            if( !flNoVxC  ) {
                contact += " | <A href='vxc/'>VxC</A>";
            }
            if( !flNoGen  ) {
                contact += " | <A href='generator/'>Generator</A>";
            }

            line = line.replaceAll( "@@license@@", contact );
        }
        else if( fileName.matches( "postcomment\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", "." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index: Post Comment" );
            line = line.replaceAll( "@@page-title@@",    "Post A Comment" );
            line = line.replaceAll( "@@license@@",       "&nbsp;" );
            line = line.replaceAll( "@@footer-links@@",  "&nbsp;" );
        }
        else if( fileName.matches( "comments\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", "." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index: Comments" );
            line = line.replaceAll( "@@page-title@@",    "View and Delete Comments" );
            line = line.replaceAll( "@@license@@",       "&nbsp;" );
            line = line.replaceAll( "@@footer-links@@",  "&nbsp;" );
        }
        else if( fileName.matches( "login\\.php" ) )
        {
            line = line.replaceAll( "@@rel-dir@@", "." );
            line = line.replaceAll( "@@browser-title@@", "Unified Verb Index: Login" );
            line = line.replaceAll( "@@page-title@@",    "Login" );
            line = line.replaceAll( "@@license@@",       "&nbsp;" );
            line = line.replaceAll( "@@footer-links@@",  "&nbsp;" );
        }
        else if( fileName.matches( "reference\\.php" ) )
        {
            String contact = "<A href='../contact.php'>Contact</A>";
            line = line.replaceAll( "@@rel-dir@@", ".." );
            line = line.replaceAll( "@@browser-title@@", flavor + " Reference Page" );
            line = line.replaceAll( "@@page-title@@",    flavor + " Reference Page" );
            line = line.replaceAll( "@@license@@",       contact + " | <A href='http://verbs.colorado.edu/~mpalmer/" +
            "projects/verbnet/downloads.html'>VerbNet Download &amp; License</A>" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='reference.php'>Reference</A> | <A href='class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }
        }
        else if( fileName.matches( "class-h\\.php" ) )
        {
            String contact = "<A href='../contact.php'>Contact</A>";
            line = line.replaceAll( "@@rel-dir@@", ".." );
            line = line.replaceAll( "@@browser-title@@", flavor + " Class Hierarchy" );
            line = line.replaceAll( "@@page-title@@",    flavor + " Class Hierarchy" );
            line = line.replaceAll( "@@license@@",       contact + " | <A href='http://verbs.colorado.edu/~mpalmer/" +
            "projects/verbnet/downloads.html'>VerbNet Download &amp; License</A>" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='reference.php'>Reference</A> | <A href='class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }
        }
        else        // All VerbNet class files will fall into here, and a few other files as well (i.e. those that don't use @@ tokens).
        {
            String contact = "<A href='../contact.php'>Contact</A>";
            line = line.replaceAll( "@@rel-dir@@", ".." );
            line = line.replaceAll( "@@browser-title@@", flavor + ": " + n );
            line = line.replaceAll( "@@page-title@@",    flavor + " v" + getProperty("vnVersion", "?") );
            line = line.replaceAll( "@@license@@",       contact + " | <A href='http://verbs.colorado.edu/~mpalmer/" +
            "projects/verbnet/downloads.html'>VerbNet Download &amp; License</A>" );
            if(!flNoIndexVn) {
                line = line.replaceAll( "@@footer-links@@",  "<A href='../vn/reference.php'>Reference</A> | <A href='../vn/class-h.php'>Class Hierarchy</A>" );
            } else {
                line = line.replaceAll( "@@footer-links@@", "&nbsp;" );
            }
        }

        // Replace the special token with the current date.
        if( line.indexOf( "@@date@@" ) != -1 )
        {
            String d = runTime.get( Calendar.YEAR ) + "." +
            ( runTime.get( Calendar.MONTH ) + 1 ) + "." +
            runTime.get( Calendar.DAY_OF_MONTH );

            line = line.replaceAll( "@@date@@", d );
        }

        // Replace the special token with the current time.
        if( line.indexOf( "@@time@@" ) != -1 )
        {
            String maintNotesLink;
            if( fileName.matches( "index\\.php" ) ) {
                maintNotesLink="<A href='maint-notes.html' style='font-weight:normal;color:white;'>M</A>";
            } else {
                maintNotesLink = "M";
            }
            String ap = ( ( runTime.get( Calendar.AM_PM  ) == 0 ) ? "A" : "P" ) + maintNotesLink;
            String h  = ( runTime.get( Calendar.HOUR   ) == 0 ) ? "12" : "" + runTime.get( Calendar.HOUR );
            String m  = ( runTime.get( Calendar.MINUTE ) < 10 ) ? "0" + runTime.get( Calendar.MINUTE )
                                                                : ""  + runTime.get( Calendar.MINUTE );

            String t = h + ":" + m + " " + ap;

            line = line.replaceAll( "@@time@@", t );
        }

        return line;
    }

    //////////////////////////
    // Supplemental Classes //
    //////////////////////////

    /**
     * Exception class for identifying when the user did not supply a
     * command-line of the proper format.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static class InvalidCommandLineArgumentException extends IllegalArgumentException
    {

        /**
         * Constructs the exception object with the given message.
         *
         * @param message the text of the exception
         */
        public InvalidCommandLineArgumentException( String message )
        {
            super( message );
        }
    }

    /**
     * Exception class for identifying when the user did not supply both
     * a valid XML input directory and a valid HTML output directory.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static class InvalidDirectoryException extends IOException
    {

        /**
         * Constructs the exception object with the given message.
         *
         * @param message the text of the exception
         */
        public InvalidDirectoryException( String message )
        {
            super( message );
        }
    }

    /**
     * Exception class for identifying when the user requests to
     * view the help/usage message for the program.
     *
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static class UserWantsHelpMessage extends Exception
    {

        /**
         * Constructs the exception object.
         */
        public UserWantsHelpMessage()
        {
            super();
        }
    }

    /**
     * Exception class for identifying when the required supplemental directory
     * or the files within cannot be found or read.
     *
     * @see uvi.Generator#checkSupplementalFiles()
     */
    private static class InvalidSupplementalFilesException extends IOException
    {

        /**
         * Constructs the exception object with the given message.
         *
         * @param message the text of the exception
         */
        public InvalidSupplementalFilesException( String message )
        {
            super( message );
        }
    }

    /**
     * Decides which files to select for the {@link java.io.File#listFiles()}
     * method of the {@link java.io.File} class.
     * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
     * When this documentation was created, only *.html files were used.  Later, they were
     * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
     *
     * @see uvi.Generator#analyzeArguments(String[])
     */
    private static class MyFilter implements FileFilter
    {
        /**
         * The extension of the files to select.
         */
        private String ext;

        /**
         * Constructs this filter with the given extension on which to filter.
         *
         * @param newExt the extension of the files to select (i.e. "xml")
         */
        public MyFilter( String newExt )
        {
            ext = newExt;
        }

        /**
         * Returns whether or not to accept the given file based on this filter.
         *
         * @param pathName the path which should be accepted or rejected
         *        based on whether it contains the extension for this
         *        <CODE>MyFilter</CODE> object
         * @return <CODE>true</CODE> if the given file should be selected
         */
        public boolean accept( File pathName )
        {
            return pathName.getName().endsWith( "." + ext );
        }
    }

    /**
     * Contains the methods called by the {@link javax.xml.parsers.DocumentBuilder} object
     * in {@link uvi.Generator#generateHTMLFiles()} when an error is encountered with an XML
     * file during the parsing thereof.  The DB object is given an instance of this class
     * and it calls the appropriate method if it encounters a problem.  This object is
     * also constantly being given the name of the current XML file being processed so it
     * can provide that information along with the error text.
     * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
     * When this documentation was created, only *.html files were used.  Later, they were
     * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
     *
     * @see uvi.Generator#generateHTMLFiles()
     */
    private static class XMLErrorHandler implements ErrorHandler
    {
        /**
         * The current XML file being processed in the {@link uvi.Generator#generateHTMLFiles()}
         * method.
         */
        private static String activeFile = "";

        /**
         * Constructs a new error handler.
         */
        XMLErrorHandler() {}

        /**
         * Sets the active file to the new file being processed.
         *
         * @param newFile the file currently being processed
         * @see uvi.Generator#generateHTMLFiles()
         */
        public void setActiveFile( String newFile )
        {
            activeFile = newFile;
        }

        /**
         * Shows any error, along with file name and line number, found by the parser.
         *
         * @param spe the exception generated by the parser
         */
        public void error( SAXParseException spe )
        {
            eprintln( "ERROR: [" + activeFile + "/Line " + spe.getLineNumber() + "] " + spe.getMessage() );
        }

        /**
         * Shows any fatal error, along with file name and line number, found by the parser.
         *
         * @param spe the exception generated by the parser
         */
        public void fatalError( SAXParseException spe )
        {
            eprintln( "FATAL ERROR: [" + activeFile + "/Line " + spe.getLineNumber() + "] " + spe.getMessage() );
        }

        /**
         * Shows any warning, along with file name and line number, found by the parser.
         *
         * @param spe the exception generated by the parser
         */
        public void warning( SAXParseException spe )
        {
            eprintln( "WARNING: [" + activeFile + "/Line " + spe.getLineNumber() + "] " + spe.getMessage() );
        }
    }
}

