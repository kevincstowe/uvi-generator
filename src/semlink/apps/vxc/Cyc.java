
////////////////////////////
// VxC: A VN-Cyc Mapper   //
// Built on the Inspector //
// University of Colorado //
// Fall 2006              //
////////////////////////////

package semlink.apps.vxc;

import java.util.*;
import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

/**
 * This class implements the mapping of VerbNet verb-frame pairs to Cyc
 * <CODE>verbSemTrans</CODE> rules.  This is the bulk of the VxC application.  A better name for this class
 * might have been <I>Mapper</I> or <I>CycMapper</I> since this class is not merely the embodiment
 * of the Cyc data, but <I>Cyc</I> was chosen for simplicity.
 * VxC uses the Inspector extension method
 * of writing as little code as possible in the {@link vn.EventManager} class so that
 * this class, along with {@link vn.Matcher} and {@link vn.PrepositionManager},
 * are the embodiment of the extension.
 * <BR><BR>
 * The VxC application accepts 5 additional command-line options (view usage note for more information):
 * <UL>
 *    <LI>Cyc input file (-C operator)</LI>
 *    <LI>Manual mapping file (-M operator)</LI>
 *    <LI>Match output file (-F operator)</LI>
 *    <LI>Match Constraints (-A operator)</LI>
 *    <LI>VxC Application Flags (-S operator)</LI>
 * </UL>
 * The -C, -M, and -F operators are required. If the -A operator is omitted, -An is assumed.<BR><BR>
 * The Cyc input file is a text file enumerating the Cyc <CODE>verbSemTrans</CODE> rules.
 * A Cyc input file <CODE>supplemental/cyc-rules.txt</CODE> is provided in the <CODE>vxc.tar.gz</CODE> download file.
 * More information on this file can be found here: {@link vn.Cyc#cycFile}.<BR><BR>
 * The manual mapping file contains the hand-made matches between all verb-frame pairs in
 * a handful of VerbNet classes and the Cyc rules.  These are assumed to be "correct" matches
 * against which the automatic matching algorithm can be measured.  A manual mapping
 * file <CODE>supplemental/manual-mapping.xml</CODE> is provided in the <CODE>vxc.tar.gz</CODE> download file.
 * <BR><BR>The <CODE>vxc.tar.gz</CODE> download file can be found
 * <A href="http://verbs.colorado.edu/verb-index/vxc" target="_blank">here</A>.<BR><BR>
 * The Cyc class works by receiving requests to look for Cyc matches from the {@link vn.EventManager} class.
 * The Inspector system (running in verb-frame pair mode) scans the VerbNet XML files and fire events
 * when it encounters various elements.  When it encounters a verb-frame pair it calls
 * {@link vn.Cyc#findCycMatches(String, String, String, int, String)} to search for matches with the Cyc rules.
 * A match is determined by applying all the constraints supplied (via the -A operator) and if the possible
 * match has not been discarded, it is assumed to be a good match.  At the end of the program
 * all the results of the automatic matching are printed to stdout.
 *
 * <BR><BR>Here are all of the constraints implemented by the VxC system:<BR><BR>
 * <TABLE style='border:1px #000000 solid' cellpadding=4 align='center' width='80%'>
 *    <TR valign='top'><TD align='center'><U>Symbol</U></TD><TD><U>Name</U></TD>
 *        <TD><U>Description</U></TD></TR>
 *    <TR valign='top'><TD align='center'><B>0</B></TD><TD><I>null match</I></TD>
 *        <TD>excludes all possible matches</TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#nullMatch()}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>n</B></TD><TD><I>naive match</I></TD>
 *        <TD>excludes only those possible matches whose lemmas are not equal</TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#naive(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>p</B></TD><TD><I>preposition</I></TD>
 *        <TD>excludes those possible matches where VerbNet does not have required Cyc preposition</TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#cycPreposition(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>t</B></TD><TD><I>transitivity</I></TD>
 *        <TD>excludes those possible matches where VerbNet's and Cyc's transitivity do not correspond</TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#transitivity(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>i</B></TD><TD><I>infinitive/gerund</I></TD>
 *        <TD>excludes those possible matches where VerbNet's and Cyc's infinitives or gerunds do not correspond</TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#infinitiveGerund(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>j</B></TD><TD><I>adjective</I></TD>
 *        <TD>excludes those possible matches where VerbNet's and Cyc's adjectives do not correspond</TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#adjective(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>f</B></TD><TD><I>fromLocation implies Source</I></TD>
 *        <TD>excludes those possible matches where Cyc uses <CODE>fromLocation</CODE> and VerbNet does not use <CODE>Source</CODE></TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#fromLocationImpliesSource(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>d</B></TD><TD><I>dbpb implies Agent</I></TD>
 *        <TD>excludes those possible matches where Cyc uses <CODE>doneBy</CODE> or <CODE>performedBy</CODE> and VerbNet does not use <CODE>Agent</CODE></TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#DBPBimpliesAgent(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>m</B></TD><TD><I>middle voice implies no Agent</I></TD>
 *        <TD>excludes those possible matches where the Cyc rule is a <CODE>MiddleVoiceFrame</CODE> but VerbNet uses <CODE>Agent</CODE></TD></TR>
 *    <TR valign='top'><TD align='center'>&nbsp;</TD><TD>&nbsp;</TD><TD>{@link vn.Matcher#middleVoiceNoAgent(String, String)}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>a</B></TD><TD><I>all</I></TD>
 *        <TD>applies all above constraints except 0, in the above order</TD></TR>
 * </TABLE>
 * <BR>
 * If -A0 is supplied, no other constraints may be used with it.  Constraints will be applied
 * to each possible match in the order that they are supplied in the -A operator's argument.
 * So, -Apt and -Atp will produce the same match output file but if the -Sd flag is provided
 * the it's possible that the reason a possible match was discarded will be different.
 * Using the -Sd flag, the output sent to stderr for -Aabcde and -Aecabd will be the exact
 * same (same reason for discards) if and only if the constraints a, b, c, d, and e discard
 * mutually exclusive sets of possible matches.  The final results will not change though,
 * regardless of the order.<BR><BR>
 * The additional flags that apply to the VxC system are supplied by the -S operator.
 * Multiple flags can be specified by this operator.  Here are all the VxC system flags
 * and the corresponding variables that represent them:
 * <BR><BR>
 * <TABLE style='border:1px #000000 solid' cellpadding=4 align='center'>
 *    <TR valign='top'><TD align='center'><U>Flag</U></TD><TD><U>Name</U></TD>
 *        <TD><U>Variable</U></TD></TR>
 *    <TR valign='top'><TD align='center'><B>c</B></TD><TD><I>show class match counts</I></TD>
 *        <TD>{@link vn.Cyc#flShowClassMatchCounts}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>d</B></TD><TD><I>show discards</I></TD>
 *        <TD>{@link vn.Cyc#flShowDiscards}</TD></TR>
 *    <TR valign='top'><TD align='center'><B>m</B></TD><TD><I>manual mapping classes only</I></TD>
 *        <TD>{@link vn.Cyc#flManualClassesOnly}</TD></TR>
 * </TABLE>
 * <BR>Finally, to give you an idea of the maginitude of the data (numbers as of VxC v1.0):
 * <BR><CODE>&nbsp;&nbsp;&nbsp;29,245 verb-frame pairs x 3,256 Cyc rules = 95,221,720 possible matches</CODE>
 * <BR><CODE>&nbsp;&nbsp;&nbsp;26,128 naive matches</CODE>
 *
 *
 * @see vn.Inspector
 * @see vn.EventManager
 *
 * @author Derek Trumbo
 * @version 1.0, 2006.10.25
 */
public class Cyc
{

   ////////////
   // Fields //
   ////////////

   // ------------------------ //
   // -- VxC Flags (-S <s>) -- //
   // ------------------------ //

   /**
    * A string representation of all of the flags supplied on the
    * command line.  All flags can be separate from one another
    * or combined into a single command line token.  This
    * string accumulates all the flags supplied on the command line
    * so they can all be printed out nicely in the VxC header.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printHeader()
    */
   static String flags;

   /**
    * A flag set in {@link vn.Inspector#analyzeArguments(String[])} and
    * used to indicate that the user would like the VxC system to
    * print a line of text to stderr for each possible match that was
    * discarded.  This line of text includes the reason for discarding.
    * This is shown by displaying the constraint symbol corresponding
    * to the constraint method which signalled to discard the possible match.
    * Remember that constraints are applied in the order they appear
    * in the -A operator's argument.  Therefore, the reason for discard
    * could be different based on different arrangements of this arugment
    * (i.e. pti vs. itp).
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean flShowDiscards;

   /**
    * A flag set in {@link vn.Inspector#analyzeArguments(String[])} and
    * used to indicate that the user would like the VxC system that
    * classes not covered by the manual mapping should be ignored (this will save you time)
    * Most of the final results are based off of the manual mapping.
    * Sometimes it makes sense to ignore all other classes, if you're
    * just looking to gauge performance of the matching algorithm.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#setClass(String)
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean flManualClassesOnly;

   /**
    * A flag set in {@link vn.Inspector#analyzeArguments(String[])} and
    * used to indicate that the user would like the VxC system that the
    * number of good matches found in each class and each subclass should
    * be printed.  This printing of a class's or subclass's match count
    * takes place right before a new class begins (or when the &lt;/FRAMES>
    * tag is reached).  The sum of all these counts will be equal to
    * <CODE>[Correct Matches] + [Incorrect Matches]</CODE> in the final
    * results section.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printClassMatchCount(int)
    */
   static boolean flShowClassMatchCounts;

   // ----------------------------- //
   // -- Cyc Input File (-C <c>) -- //
   // ----------------------------- //

   /**
    * The file containing all the <CODE>verbSemTrans</CODE> rules from Cyc.
    * One is provided in the <CODE>vxc.tar.gz</CODE> download file for VxC.
    * This file was obtained by performing these steps:
    * <UL>
    *    <LI>Open Cyc Knowledge Browser</LI>
    *    <LI>Log In</LI>
    *    <LI>Search for the <CODE>verbSemTrans</CODE> constant</LI>
    *    <LI>On left-hand frame click on "Predicate Extent"</LI>
    *    <LI>Copy all the text that loads into the right-hand frame and paste into a new file</LI>
    *    <LI>Remove all text before the line "Mt : BritishEnglishMt"</LI>
    *    <LI>Remove all rules for microtheories other than "GeneralEnglishMt"</LI>
    *    <LI>Remove all rules containing:
    *       <UL>
    *          <LI><CODE>WordWithPrefixFn</CODE> or <CODE>WordWithPrefixFn</CODE></LI>
    *          <LI><CODE>(verbSemTrans *-MWW</CODE> - rules with multi-words as first predicate argument</LI>
    *          <LI><CODE>[False](not</CODE> - negated rules</LI>
    *       </UL>
    *    </LI>
    * </UL
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printHeader()
    * @see vn.Cyc#addRuleNumbersAndCompress()
    */
   static File cycFile;

   /**
    * The compressed version of the Cyc input file ({@link vn.Cyc#cycFile}).
    * When the data is extracted from Cyc, each rule spans multiple lines.
    * The method {@link vn.Cyc#addRuleNumbersAndCompress()} removes newline
    * characters in order to create a Cyc rule file with one rule per line.
    * This file is always named FILE.vxc.compressed where FILE is the name
    * of the Cyc input file provided on the command line (-C operator).  The compressed
    * file is placed into the directory where the <CODE>java vn.Inspector</CODE>
    * command was executed.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printHeader()
    * @see vn.Cyc#addRuleNumbersAndCompress()
    * @see vn.Cyc#loadCycRules()
    */
   static File cycFileCmp;

   /**
    * All Cyc rules.  This is an array of arrays.  This array contains one
    * element for each letter of the alphabet.  Each element is itself an
    * {@link java.util.ArrayList} object.  Each rule is added to the array
    * that corresponds to the rule's verb.  This is done to decrease
    * access time when checking to see if a VerbNet syntax matches
    * any Cyc rules.  Each of the 26 arrays are held in unsorted order.
    * There exist many other ways to store these rules so as to achieve
    * even faster access times.
    *
    * @see vn.Cyc#loadCycRules()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static ArrayList[] cycRules;

   // ---------------------------------- //
   // -- Manual Mapping File (-M <m>) -- //
   // ---------------------------------- //

   /**
    * The manual mapping file.  This contains all the manual mappings
    * performed by the author of this software upon the original
    * creation of VxC.  This should be a file in XML format with an
    * inline DTD.  One is provided in the <CODE>vxc.tar.gz</CODE> download file
    * for VxC.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printHeader()
    * @see vn.Cyc#loadManualFile()
    */
   static File manFile;

   /**
    * All matches found by the automatic matching algorithm.
    * A {@link vn.Cyc.Match} object is added to this array
    * every time the automatic matching algorithm has no
    * reason, based on the desired constraints, to discard
    * the match.
    *
    * @see vn.Cyc#initialize()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.Cyc#compareMatches()
    */
   private static ArrayList AM;

   /**
    * All matches specified in the manual mapping file.  There is
    * one {@link vn.Cyc.Match} object added to this array
    * for each <tuple> or <tuple-m> element in the file.
    *
    * @see vn.Cyc#initialize()
    * @see vn.Cyc#loadManualFile()
    * @see vn.Cyc#compareMatches()
    */
   private static ArrayList MM;

   /**
    * The list of classes and subclasses covered by the manual
    * mapping.  One string is added to this array for each
    * <vnclass> element in the file.  This is used to determine
    * if the current class or subclass being scanned by the
    * Inspector was covered by the manual mapping file.
    *
    * @see vn.Cyc#setClass(String)
    * @see vn.Cyc#initialize()
    * @see vn.Cyc#loadManualFile()
    * @see vn.Cyc#compareMatches()
    */
   private static ArrayList manClasses;

   /**
    * The number of rules discarded by the automatic matching
    * algorithm from classes covered by the manual mapping
    * file.  This statistic is required in the final results
    * section to produce the correct numbers.
    *
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.Cyc#compareMatches()
    */
   private static int numAMDiscardsInManClasses;

   /**
    * The VerbNet and Cyc versions to which the manual mapping file
    * corresponds.  The manual mapping file is created with
    * respect to a given VerbNet version and that version is
    * stored in an attribute of the document root element of the
    * file.  This value will be replicated to the match output
    * file to give that file some version context.  It should
    * be noted that VxC should never be run with a VerbNet
    * version different than that listed in the manual mapping
    * file that you are using - this can cause inaccurate
    * matches and discards.  The same can be said for Cyc.
    * The verbSemTrans rules contained inside the download were
    * extracted from the version of ResearchCyc listed in the
    * manual mapping file.  This version number is more for
    * informational purposes, rather than for compatibility
    * purposes, since the only real important warning concerning
    * the Cyc rules and the manual mapping file is that the Cyc
    * rule numbers shown in the manual mapping file must correspond
    * to the order in which they appear in the included Cyc
    * rule file.  So the manual mapping file will be invalid
    * if paired with a different verbSemTrans file that is ordered
    * differently.
    *
    * @see vn.Cyc#loadManualFile()
    * @see vn.Cyc#setUpMatchFile()
    */
   private static String manualVersion;

   // -------------------------------- //
   // -- Match Output File (-F <f>) -- //
   // -------------------------------- //

   /**
    * The file to which matches found by the automatic matching
    * algorithm should be written.  The output format will be in
    * XML and should be very similar to the manual mapping file
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printHeader()
    * @see vn.Cyc#setUpMatchFile()
    */
   static File matchFile;

   /**
    * The {@link java.io.PrintWriter} object tied to the match
    * output file.
    *
    * @see vn.Cyc#setUpMatchFile()
    * @see vn.Cyc#closeExternalData()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static PrintWriter matchpw;

   // -------------------------------- //
   // -- Match Constraints (-A <a>) -- //
   // -------------------------------- //

   /**
    * All constraint symbols used with the -A operator.  These
    * are all available constraints that the user can specify.
    * See the {@link vn.Cyc} class's description for an explanation
    * of each constraint.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    */
   static final String ALL_MATCH_CONSTRS = "0anptijfdm";

   /**
    * A string representation of all of the constraints supplied on the
    * command line.  All constraints can be separate from one another
    * on the command line (i.e. you can specify multiple -A operators).
    * This string represents all -A operator arguments as one.
    *
    * @see vn.Inspector#analyzeArguments(String[])
    * @see vn.Cyc#printHeader()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static String matchConstraints = "";

   // ------------------ //
   // -- Other Fields -- //
   // ------------------ //

   /**
    * The current class or subclass that the Inspector is scanning.
    * This is important for determining whether the current class
    * is covered by the manual mapping file and for writing to the
    * match output file, etc.
    *
    * @see vn.Cyc#setClass(String)
    * @see vn.Cyc#printClassMatchCount(int)
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static String curClass;

   /**
    * Whether or not the current class is covered by the manual mapping
    * file.  If the -Sm flag is supplied then the no possible matches will
    * be examined in classes that are not covered in the manual mapping file.
    *
    * @see vn.Cyc#setClass(String)
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static boolean isManualClass;

   /**
    * The number of possible matches that the automatic matching algorithm has
    * marked as 'good' in the current class or subclass.  This is reset at the beginning
    * of each class or subclass and is incremented each time a match is
    * added to {@link vn.Cyc#AM}.  This is used only to implement the
    * -Sc flag ({@link vn.Cyc#flShowClassMatchCounts}).
    *
    * @see vn.Cyc#setClass(String)
    * @see vn.Cyc#printClassMatchCount(int)
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static int classMatchCount;

   ////////////////////
   // Helper Methods //
   ////////////////////

   /**
    * Used as shorthand for <CODE>System.out.println</CODE>.
    *
    * @param s the string to print to stdout, followed by a carriage return
    * @see     java.io.PrintStream#println(String)
    */
   private static void println( String s ) { System.out.println( s ); }

   /**
    * Used as shorthand for <CODE>System.out.print</CODE>.
    *
    * @param s the string to print to stdout
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
    * Sets the current class or subclass that the Inspector is scanning.
    * Sets whether or not this class or subclass was covered
    * in the manual mapping file.  Resets the class match counter.
    *
    * @param newClassName the new class name  ('ID' attribute from VerbNet XML files)
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    * @see vn.Cyc#isManualClass
    * @see vn.Cyc#manClasses
    */
   static void setClass( String newClassName )
   {
      curClass = newClassName;

      isManualClass = false;

      // Look for the class name in the list of classes
      // that are covered in the manual mapping file.
      for( int c = 0; c < manClasses.size(); c++ )
         if( ( ( String ) manClasses.get( c ) ).equalsIgnoreCase( curClass ) )
         {
            isManualClass = true;
            break;
         }

      // Reset the class match counter.
      classMatchCount = 0;
   }

   /**
    * Prints the number of matches found by the
    * automatic matching algorithm for this class
    * or subclass.  This method does nothing if
    * {@link vn.Cyc#flShowClassMatchCounts} is <CODE>false</CODE>.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static void printClassMatchCount( int level )
   {
      if( flShowClassMatchCounts )
      {
         for( int l = 0; l < level; l++ )
            print( "   " );

         println( curClass + ": match count = " + classMatchCount );
      }
   }

   /////////////////
   // Constructor //
   /////////////////

   /**
    * This constructor is private because the class is not intended to ever
    * be instantiated.  The VxC application is a very procedural process and
    * thus all the members are static.
    */
   private Cyc() {}

   ////////////////////
   // Initialization //
   ////////////////////

   /**
    * Initializes some class variables and prints the header.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    * @see vn.Cyc#printHeader()
    */
   static void initialize()
   {
      AM         = new ArrayList();
      MM         = new ArrayList();
      manClasses = new ArrayList();

      printHeader();
   }

   /**
    * Prints the header for this execution of the VxC mapper.  All command
    * line arguments are regurgitated and the date and time of this execution
    * is shown.
    *
    * @see vn.Inspector#filePath(File)
    * @see vn.Cyc#initialize()
    */
   private static void printHeader()
   {
      println( "--- VxC: Header ---------------------------------------------->" );
      println( "| Cyc Input File:         " + Inspector.filePath( cycFile ) );
      println( "| Cyc Input File (cmp.):  " + Inspector.filePath( cycFileCmp ) );
      println( "| Manual Mapping File:    " + Inspector.filePath( manFile ) );
      println( "| Match Output File:      " + Inspector.filePath( matchFile ) );
      println( "| Match Constraints:      " + matchConstraints );
      println( "| Flags:                  " + flags );
      println( "| Executed On:            " + new Date().toString() );
      println( "-------------------------------------------------------------->" );
   }

   ///////////////////
   // External Data //
   ///////////////////

   /**
    * Loads all the Cyc rules from the Cyc input file and the manual
    * matches from the manual mapping file. Also prepares the match
    * output file to be written to.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static void loadExternalData()
   {
      try
      {

         // Compress the original Cyc input file into another
         // file which has one line per verbSemTrans rule.
         addRuleNumbersAndCompress();

         // Read in all the Cyc rules into class-level data structures.
         loadCycRules();

         // Read in all the manual matches from the manual mapping file.
         loadManualFile();

         // Prepare the match output file to be written to.
         setUpMatchFile();
      }

      // There was an error with the Cyc input file...
      catch( InvalidCycInputFileException icife )
      {
         eprintln( "ERROR: Invalid Cyc input file format." );
      }

      // There was an error instantiating the DocumentBuilder object...
      catch( ParserConfigurationException e )
      {
         eprintln( "ERROR: Parser configuration error." );
      }

      // There was an error with the Cyc input file or match output file...
      catch( IOException ioe )
      {
         eprintln( "ERROR: I/O exception occurred while loading Cyc rules or " +
                   "match output file could not be opened for writing." );
      }

      // There was an error with the parsing of the manual mapping file...
      catch( SAXException saxe )
      {
         eprintln( "ERROR: XML parse problem.  " + saxe.getMessage() );
      }

      // There was another, unexpected error...
      catch( Exception e )
      {
         eprintln( "ERROR: " + e.getMessage() );
      }
   }

   /**
    * Writes the end tag of the document element to the
    * match output file and closes the stream.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static void closeExternalData()
   {
      matchpw.println( "</verbnet-cyc_MappingData>" );
      matchpw.close();
   }

   /**
    * Compresses the original Cyc input file into another
    * file which has one line per verbSemTrans rule.
    * The automatic matching algorithm gains nothing by having
    * the Cyc rules spread across multiple files so it converts
    * the Cyc input file into a condensed format for efficiency.
    * The output file name is the input file name with the
    * characters ".vxc.compressed" appended to it and is located
    * the directory where <CODE>java vn.Inspector</CODE> was invoked.
    * <BR><BR>This method also adds rule numbers for convenient identification
    * of Cyc rules.  They are assigned in the order that the rules are
    * are encountered in the file.  The Cyc rule numbering applied in this method
    * corresponds to those used in the manual mapping file.
    *
    * @see vn.Cyc#loadExternalData()
    */
   private static void addRuleNumbersAndCompress() throws InvalidCycInputFileException, IOException
   {

      // Initialize a reader for the Cyc input file and a writer
      // for the compressed file.
      BufferedReader in = new BufferedReader( new FileReader( cycFile ) );
      PrintWriter pw    = new PrintWriter( new BufferedWriter( new FileWriter( cycFileCmp ) ) );

      String line;
      String outputLine = "";
      int ruleNum = 1;

      // Read all the lines from the input file and write off
      // lines in the appropriate format.
      while( ( line = in.readLine() ) != null )
      {

         // Skip all blank lines in the input file.
         if( line.trim().equals( "" ) )
            continue;

         // If this is the first line of the rule...
         if( line.indexOf( "verbSemTrans" ) != -1 )
         {

            // If the output line is not blank, it needs to be
            // sent to the compressed file now (it's the previous
            // rule).
            if( !outputLine.equals( "" ) )
               pw.println( outputLine );

            // Initialize the output line to the new rule's number.
            outputLine = "RULE #" + ruleNum++;
         }

         // Else if the line does not contain a verbSemTrans
         // token and we haven't processed a rule yet, then
         // there's a problem with the file.
         else if( ruleNum == 1 )
            throw new InvalidCycInputFileException( "Cyc input file is not of the proper format." );

         // Append the current line from the input file onto
         // the growing output line with a space preceding it.
         outputLine += " " + line.trim();
      }

      // Send the last output line to the compressed file.
      if( !outputLine.equals( "" ) )
         pw.println( outputLine );

      // Close both streams.
      pw.close();
      in.close();
   }

   /**
    * Loads the Cyc rules stored in the compressed file into class-level
    * data structures.  Each line of the file represents a different
    * rule and will be placed into a list containing rules whose verb
    * starts with the same letter.
    *
    * @see vn.Cyc#loadExternalData()
    * @see vn.Cyc#cycRules
    */
   private static void loadCycRules() throws IOException
   {
      cycRules = new ArrayList[ 26 ];     // Initialize the array.

      // Initialize reader.
      BufferedReader in = new BufferedReader( new FileReader( cycFileCmp ) );
      String line;

      while( ( line = in.readLine() ) != null )
      {

         // Grab the verb for this rule (returned in lower case).
         String cycVerb = cycExtractVerb( line );

         // Decide on which rule list to use ('a'=0, 'z'=25).
         int whichLetter = cycVerb.charAt( 0 ) - 97;

         // If the list has not yet been initialized, do so.
         if( cycRules[ whichLetter ] == null )
            cycRules[ whichLetter ] = new ArrayList( 1000 );

         // Add the rule to the chosen list.
         cycRules[ whichLetter ].add( line );
      }

      in.close();
   }

   /**
    * Prepares the match output file to be written to.  This opens
    * and initializes the stream that will be written to each time a match is
    * found.
    *
    * @see vn.Cyc#loadExternalData()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static void setUpMatchFile() throws IOException
   {
      matchpw = new PrintWriter( new BufferedWriter( new FileWriter( matchFile ) ) );

      Calendar runTime = Calendar.getInstance();

      String today = runTime.get( Calendar.YEAR ) + "." +
                     ( runTime.get( Calendar.MONTH ) + 1 ) + "." +
                     runTime.get( Calendar.DAY_OF_MONTH );

      matchpw.println( "<?xml version=\"1.0\"?>" );
      matchpw.println( "<!DOCTYPE verbnet-cyc_MappingData [" );
      matchpw.println( "  <!ELEMENT verbnet-cyc_MappingData (tuple)*>" );
      matchpw.println( "  <!ELEMENT tuple (#PCDATA)>" );
      matchpw.println( "" );
      matchpw.println( "  <!ATTLIST verbnet-cyc_MappingData date CDATA #IMPLIED>" );
      matchpw.println( "  <!ATTLIST verbnet-cyc_MappingData vn-version CDATA #IMPLIED>" );
      matchpw.println( "  <!ATTLIST verbnet-cyc_MappingData cyc-version CDATA #IMPLIED>" );
      matchpw.println( "" );
      matchpw.println( "  <!-- GOOD MATCHES -->" );
      matchpw.println( "  <!ATTLIST tuple vnc   CDATA #REQUIRED>     <!-- VN CLASS -->" );
      matchpw.println( "  <!ATTLIST tuple vnm   CDATA #REQUIRED>     <!-- VN MEMBER -->" );
      matchpw.println( "  <!ATTLIST tuple vnf   CDATA #REQUIRED>     <!-- VN FRAME -->" );
      matchpw.println( "  <!ATTLIST tuple crule CDATA #REQUIRED>     <!-- CYC RULE # -->" );
      matchpw.println( "]>" );

      matchpw.println( "<verbnet-cyc_MappingData date='" + today + "' " + manualVersion + ">" );
   }

   /**
    * Loads the manual matches stored in the manual mapping file into
    * the list of manual matches {@link vn.Cyc#MM}.  Loads the
    * names of all the classes covered by the manual mapping file into
    * the list of manual mapping classes {@link vn.Cyc#manClasses}.
    * This method assumes there are both &lt;tuple> and &lt;tuple-m> (maybe tuples)
    * elements in the manual mapping file.  This method considers both
    * of them to be good matches.  Only the final results section makes
    * a distinction when printing out those matches which the automatic
    * matching algorithm failed to match by printing the word 'maybe'
    * next to a match if it was so identified in the manual mapping file.
    *
    * @see vn.Cyc#loadExternalData()
    * @see vn.Cyc#MM
    * @see vn.Cyc#manClasses
    */
   private static void loadManualFile() throws Exception
   {

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

      // Validate the structure of this file. Errors will be caught
      // by the calling method and sent to stderr.
      dbf.setValidating( true );

      DocumentBuilder db = dbf.newDocumentBuilder();

      // Parse the manual mapping file into a Document object.
      Document doc = db.parse( manFile );

      // Add both normal matches and maybe matches to the MM array.
      for( int q = 0; q < 2; q++ )
      {

         // Grab the nodes with the correct tag name.
         NodeList members = doc.getElementsByTagName( ( q == 0 ) ? "tuple" : "tuple-m" );

         // For each node returned, add it to the MM array.
         for( int m = 0; m < members.getLength(); m++ )
         {
            Element e = ( Element ) members.item( m );

            Match newMatch = new Match();

            // Set all instance variables of this new match object.
            newMatch.vnClass       = e.getAttribute( "vnc" );
            newMatch.verb          = e.getAttribute( "vnm" );
            newMatch.frame         = e.getAttribute( "vnf" );
            newMatch.cycRuleNum    = e.getAttribute( "crule" );
            newMatch.maybe         = ( q == 1 );
            newMatch.isManualClass = true;

            // Add it to the list.
            MM.add( newMatch );
         }
      }

      Element docElem = doc.getDocumentElement();

      // Extract the version info that will be replicated to the match output file.
      manualVersion = "vn-version='" + docElem.getAttribute( "vn-version" ) +
                      "' cyc-version='" + docElem.getAttribute( "cyc-version" ) + "'";

      // Grab the nodes identifying which classes and subclasses
      // are covered by the manual mapping file.
      NodeList vnclasses = doc.getElementsByTagName( "vnclass" );

      // ...and add them to the list.
      for( int c = 0; c < vnclasses.getLength(); c++ )
      {
         Element e = ( Element ) vnclasses.item( c );
         manClasses.add( e.getAttribute( "ID" ) );
      }
   }

   //////////////////////
   // Cyc Manipulation //
   //////////////////////

   /**
    * Returns the verb of a Cyc rule.  This is the contained in the first
    * argument after the verbSemTrans predicate. For example, the following
    * rule contains the verb "jump":
    * <PRE>  RULE #2244 [Def](verbSemTrans Jump-TheWord 0 IntransitiveVerbFrame ...</PRE>
    *
    * @param rule the Cyc rule to examine
    * @return the verb of the Cyc rule
    * @see vn.Cyc#loadCycRules()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static String cycExtractVerb( String rule )
   {
      int vst = rule.indexOf( "verbSemTrans " );
      int spc = rule.indexOf( " ", vst + 13 );
      int hyp = rule.lastIndexOf( "-", spc );

      // Return the characters between the 'verbSemTrans' predicate
      // and the hyphen of the '-TheWord' token.
      return rule.substring( vst + 13, hyp ).toLowerCase();
   }

   /**
    * Returns whether or not a Cyc rule deals with a particle construction
    * (i.e.&nbsp;"shear off").  A Cyc rule deals with a particle construction
    * if it contains the token <CODE>ParticleCompFrameFn</CODE>.
    *
    * @param rule the Cyc rule to examine
    * @return whether or not the Cyc rule deals with a particle construction
    */
   static boolean cycHasParticle( String rule )
   {
      return rule.indexOf( "ParticleCompFrameFn" ) != -1;
   }

   /**
    * Returns the particle in a particle construction (i.e.&nbsp;"off" in "shear off").
    *
    * @param rule the Cyc rule to examine
    * @return the particle associated with this rule or a zero-length string
    *         if the Cyc rule does not contain a particle.
    */
   static String cycExtractParticle( String rule )
   {

      // Return a zero-length string if there is no particle.
      if( !cycHasParticle( rule ) )
         return "";

      int ppc = rule.indexOf( "ParticleCompFrameFn " );
      int par = rule.indexOf( ")", ppc );
      int spc = rule.lastIndexOf( " ", par );

      // Extract the last argument of the 'ParticleCompFrameFn' predicate.
      String prep = rule.substring( spc + 1, par ).toLowerCase();

      // Strip -MWW or -TheWord from it.
      if( prep.endsWith( "-mww" ) )
         prep = prep.substring( 0, prep.length() - 4 );

      else // -TheWord
         prep = prep.substring( 0, prep.length() - 8 );

      // Return the particle with all hyphens replaced with underscores
      // (i.e. off-of -> off_of) so it is congruent with VerbNet syntax.
      return prep.replaceAll( "-", "_" );
   }

   /**
    * Returns whether or not a Cyc rule specifies a preposition (i.e.&nbsp;"bicker with").
    * A Cyc rule specifies a preposition
    * if it contains the token <CODE>PPCompFrameFn</CODE>.  The Cyc rule that specifies
    * a preposition in this matter is saying that the Cyc rule is only valid when
    * the verb is used with the given preposition.
    *
    * @param rule the Cyc rule to examine
    * @return whether or not the Cyc rule specifies a preposition using which the
    *         predicate rule is valid
    * @see vn.Matcher#cycPreposition(String, String)
    */
   static boolean cycHasPreposition( String rule )
   {
      return rule.indexOf( "PPCompFrameFn" ) != -1;
   }

   /**
    * Returns the preposition specified by a Cyc rule.   For example, the following
    * rule (uncompressed) contains the verb and preposition "bicker with":
    * <PRE>[Def](verbSemTrans Bicker-TheWord 0
    *    (PPCompFrameFn TransitivePPFrameType With-TheWord)
    *    ...</PRE>
    *
    * @param rule the Cyc rule to examine
    * @return the preposition specified by the Cyc rule or a zero-length string
    *         if the Cyc rule does not specify a preposition.
    * @see vn.Matcher#cycPreposition(String, String)
    */
   static String cycExtractPreposition( String rule )
   {

      // Return a zero-length string if there is no preposition.
      if( !cycHasPreposition( rule ) )
         return "";

      int ppc = rule.indexOf( "PPCompFrameFn " );
      int par = rule.indexOf( ")", ppc );
      int spc = rule.lastIndexOf( " ", par );

      // Extract the last argument of the 'PPCompFrameFn' predicate.
      String prep = rule.substring( spc + 1, par ).toLowerCase();

      // Strip -MWW or -TheWord from it.
      if( prep.endsWith( "-mww" ) )
         prep = prep.substring( 0, prep.length() - 4 );

      else // -TheWord
         prep = prep.substring( 0, prep.length() - 8 );

      // Return the preposition with all hyphens replaced with underscores
      // (i.e. off-of -> off_of) so it is congruent with VerbNet syntax.
      return prep.replaceAll( "-", "_" );
   }

   /**
    * Returns the rule number applied to this Cyc rule in
    * {@link vn.Cyc#addRuleNumbersAndCompress()}.
    *
    * @param rule the Cyc rule to examine
    * @return the rule number of this Cyc rule
    * @see vn.Cyc#addRuleNumbersAndCompress()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static String cycExtractRuleNum( String rule )
   {
      int hash = rule.indexOf( "#" );
      int spc  = rule.indexOf( " ", hash );

      // Return the string between the pound sign and
      // the first space that follows it.
      return rule.substring( hash + 1, spc );
   }

   /**
    * Returns the transitivity of this Cyc rule represented by a
    * small handful of characters.  The different abbreviations and the
    * tokens involved in the Cyc rules that correspond to them are:
    * <UL>
    *    <LI><CODE>T - TransitiveNPFrame</CODE></LI>
    *    <LI><CODE>I - IntransitiveVerbFrame</CODE></LI>
    *    <LI><CODE>M - Middle Voice Frame</CODE></LI>
    *    <LI><CODE>TGPF - TransitiveGerundPhraseFrame</CODE></LI>
    *    <LI><CODE>TIPF - TransitiveInfinitivePhraseFrame</CODE></LI>
    *    <LI><CODE>DDPFT - DitransitivePPFrameType</CODE></LI>
    *    <LI><CODE>?</CODE> - any other type</CODE></LI>
    * </UL>
    *
    * @param rule the Cyc rule to examine
    * @return a string representing the transitivity of the Cyc rule
    * @see vn.Matcher#transitivity(String, String)
    */
   static String cycExtractTrans( String rule )
   {
      if( rule.indexOf( "TransitiveNPFrame" ) != -1 )               return "T";
      if( rule.indexOf( "IntransitiveVerbFrame" ) != -1 )           return "I";
      if( rule.indexOf( "MiddleVoiceFrame" ) != -1 )                return "M";
      if( rule.indexOf( "TransitiveGerundPhraseFrame" ) != -1 )     return "TGPF";
      if( rule.indexOf( "TransitiveInfinitivePhraseFrame" ) != -1 ) return "TIPF";

      if( rule.indexOf( "TransitivePPFrameType" ) != -1 &&
          rule.indexOf( ":OBJECT" ) == -1 ) return "TPPFT";    // Intransitive

      if( rule.indexOf( "DitransitivePPFrameType" ) != -1 &&
          rule.indexOf( ":OBJECT" ) != -1 ) return "DPPFT";    // Transitive

      // Return unknown transitivity.
      return "?";
   }

   //////////////////////////
   // Run-Time & Matching  //
   //////////////////////////

   /**
    * Searches the Cyc rules for a match to the verb-frame pair currently
    * being scanned by the Inspector.  The required verb-frame pair
    * information (verb, frame, syntax, semantics) is given to this method by
    * the {@link vn.EventManager} class.  Not all of the Cyc rules are
    * searched.  In order to increase the speed of this method,
    * the Cyc rules are broken apart into separate lists based on
    * the first letter of their verb. When this method is invoked,
    * it only scans the list corresponding to the first letter
    * of the VerbNet verb in question (there are even more efficient
    * ways of implementing this).
    * <BR><BR>For each Cyc rule visited, the constraints given
    * in the -A operator are applied in an attempt to discard the
    * [verb-frame, Cyc rule] match.  If none of the constraints
    * can discard the possible match, then it is considered a good match
    * and added to {@link vn.Cyc#AM}.<BR><BR>This method writes a line
    * to the match output file for every good match and if the proper
    * flag is supplied, it writes a line of text to stderr for every
    * possible match discarded within a manual mapping class.
    *
    * @param verb the verb from the VerbNet verb-frame pair
    * @param vnSyntax the syntax from the VerbNet verb-frame pair
    * @param vnSem the semantics from the VerbNet verb-frame pair.  This is a conglomeration
    *        of all the semantic predicates in one string.
    * @param vnFrameID the frame number from the VerbNet verb-frame pair
    * @param vnFrameDesc the primary and secondary frame descriptions from the VerbNet verb-frame pair
    * @see vn.Cyc#cycRules
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    * @see vn.Matcher
    */
   static void findCycMatches( String verb, String vnSyntax, String vnSem, int vnFrameID, String vnFrameDesc )
   {

      // Do not look for Cyc matches for this verb-frame pair
      // if the user only wants to process classes covered
      // in the manual mapping file and the current class
      // being processed is not such a class.
      if( flManualClassesOnly && !isManualClass )
         return;

      // Lower case the VerbNet verb for the verbs December, UPS, and FedEx.
      verb = verb.toLowerCase();

      // If the VerbNet verb begins with question mark (which means it
      // has questionable membership in the class it's in), ignore the
      // question mark and assume the verb is in the class.
      if( verb.charAt( 0 ) == '?' )
         verb = verb.substring( 1 );

      // Decide on which rule list to use ('a'=0, 'z'=25).
      int whichLetter = verb.charAt( 0 ) - 97;
      ArrayList whichList = cycRules[ whichLetter ];

      // Search through all Cyc rules in the list selected and
      // compare each one to the VerbNet verb-frame pair in question.
      for( int cw = 0; cw < whichList.size(); cw++ )
      {
         String cycRule = ( String ) whichList.get( cw );

         // Extract important parts from the current Cyc rule.
         String cycVerb = cycExtractVerb( cycRule );
         String ruleNum = cycExtractRuleNum( cycRule );
         String tr      = cycExtractTrans( cycRule );

         if( cycHasParticle( cycRule ) )
            cycVerb += "_" + cycExtractParticle( cycRule );

         boolean isMatch;

         // If just the null constraint is desired, just
         // call the null constraint method.
         if( matchConstraints.equals( "0" ) )
            isMatch = Matcher.nullMatch();

         // Else if there are real constraints to apply...
         else
         {

            // Assume this possible match is good and make the constraints
            // prove otherwise.
            isMatch = true;

            // Loop through all the constraints specified by the -A operator
            // and call the correct constraint method for each one.
            // The 'a' constraint is not seen here because if 'a' is supplied
            // it is immediately replaced with all the the rest (besides 0)
            // when the command line is being parsed.
            for( int c = 0; c < matchConstraints.length(); c++ )
            {
               char constr = matchConstraints.charAt( c );

               try
               {

                  // See how the method for the current constraint
                  // votes on this possible match.
                  switch( constr )
                  {
                     case 'n':  isMatch = Matcher.naive( verb, cycVerb );                          break;
                     case 'p':  isMatch = Matcher.cycPreposition( vnSyntax, cycRule );             break;
                     case 't':  isMatch = Matcher.transitivity( vnSyntax, cycRule );               break;
                     case 'i':  isMatch = Matcher.infinitiveGerund( vnSyntax, cycRule );           break;
                     case 'j':  isMatch = Matcher.adjective( vnSyntax, cycRule );                  break;
                     case 'f':  isMatch = Matcher.fromLocationImpliesSource( vnSyntax, cycRule );  break;
                     case 'd':  isMatch = Matcher.DBPBimpliesAgent( vnSyntax, cycRule );           break;
                     case 'm':  isMatch = Matcher.middleVoiceNoAgent( vnSyntax, cycRule );         break;
                     //case 'c':  isMatch = Matcher.bad_DBPBimpliesCause( vnSyntax, vnSem, cycRule );  break;
                     //case 'e':  isMatch = Matcher.bad_agentImpliesDBPB( vnSyntax, cycRule );         break;
                     default:
                        eprintln( "ERROR: Mapping system detected unknown constratint (" + constr + ")." );
                  }
               }

               // If there was any problem at all with the parsing, give
               // the possible rule the benefit of the doubt and pass it
               // for this constraint.
               catch( Exception e )
               {
                  isMatch = true;
               }

               // Show the reason a possible match was discarded if 1) it is so desired
               // by the user (flShowDiscards), 2) the constraint above has singalled to
               // discard it (!isMatch), 3) this is not the naive constraint (constr != 'n'),
               // and 4) this is a manual mapping class.
               if( constr != 'n' && !isMatch && flShowDiscards && isManualClass )
                  eprintln( "DISCARDED [REASON CONSTRAINT " + constr + "] [CLASS " + curClass + "] [VERB " + verb +
                     "] [SYN " + vnSyntax + "] [FR ID " + vnFrameID + "] [CYC " + cycRule + "]" );

               // If this particular constraint signalled to discard the possible match,
               // just stop processing further constraints because the possible match is
               // now invalid.
               if( !isMatch )
                  break;
            }
         }

         // In order to correctly calculate the counts in the final results area,
         // an additional statistic is needed - the number of possible matches
         // that the automatic matching algorithm discards inside a manual
         // mapping class.  This is important because:
         //   correctDiscard = numAMDiscardsInManClasses - failedToMatch
         if( isManualClass && !isMatch && Matcher.naive( verb, cycVerb ) )
            numAMDiscardsInManClasses++;

         // If the current Cyc rule matched with the verb-frame
         // being scanned by the Inspector system (i.e. the possible
         // match was not discarded by any of the constraints specified
         // by the -A operator)...
         if( isMatch )
         {

            // Create a new Match object.
            Match newMatch = new Match();

            // Initialize all new object's fields.
            newMatch.vnClass       = curClass;
            newMatch.verb          = verb;
            newMatch.cycRuleNum    = ruleNum;

            // Whether or not current class being scanned is covered by the
            // manual mapping file (see setClass(String)).
            newMatch.isManualClass = isManualClass;
            newMatch.frame         = "f" + vnFrameID + ";" + vnFrameDesc;

            // Write a line to the match output file for this match.
            matchpw.println( "   <tuple vnc='" + newMatch.vnClass    + "' vnm='"   +
                                                 newMatch.verb       + "' vnf='"   +
                                                 newMatch.frame      + "' crule='" +
                                                 newMatch.cycRuleNum + "' />" );

            // Add this match to the list of automatic matching algorithm matches.
            AM.add( newMatch );

            // Increment the number of matches found for this class.  This
            // is used only for the -Sc flag which prints out the number
            // of matches found for each class or subclass.
            classMatchCount++;

         }  // if( isMatch )

      }  // for( int cw = 0; cw < whichList.size(); cw++ )
   }

   /////////////
   // Results //
   /////////////

   /**
    * Calculates all statistics and displays the final results section.
    * All results are calculated using only the information in {@link vn.Cyc#AM},
    * {@link vn.Cyc#MM}, and {@link vn.Cyc#numAMDiscardsInManClasses}.
    * Final results are displayed to stdout and consist of three parts:
    * <UL>
    *    <LI>counts</LI>
    *    <LI>those manual mapping matches that were not considered matches by the
    *        automatic matching algorithm (all 'Failed To Match' matches)</LI>
    *    <LI>those matches effected by the automatic matching algorithm within a manual
    *        mapping class, but were not considered matches by the manual mapping file
    *        (all 'Incorrect Match' matches)</LI>
    * </UL>
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static void compareMatches()
   {
      int correctMatch   = 0;
      int incorrectMatch = 0;

      // Search through all automatic matches and
      // compare each one with all the manual matches
      // to find which were correct matches and which
      // were incorrect ones.
      for( int a = 0; a < AM.size(); a++ )
      {
         Match am = ( Match ) AM.get( a );

         for( int m = 0; m < MM.size(); m++ )
         {
            Match mm = ( Match ) MM.get( m );

            // If the current automatic match is equal
            // to the current manual match, increment
            // the counter, and mark both as being correct.
            // This is a known correct match (because it happened
            // in the realm of the manual mapping file).
            if( am.equals( mm ) )
            {
               correctMatch++;

               am.isCorrectMatch = true;
               mm.isCorrectMatch = true;
            }
         }

         // Record all instances where the automatic matching
         // algorithm effected a match in a class covered by the manual mapping
         // file but such a match was not included by the manual mapping file.
         // This is a known incorrect match (because it happened
         // in the realm of the manual mapping file).
         if( am.isManualClass && !am.isCorrectMatch )
            incorrectMatch++;
      }

      // Calculate the number of those manual mapping matches that
      // the automatic matching algorithm failed to match.
      int failedToMatch = ( MM.size() - correctMatch );

      // Calculate the number of possible matches that the automatic
      // matching algorithm correctly discarded (the manual mapping file
      // also discards these, i.e. does not include them).
      int correctDiscard = numAMDiscardsInManClasses - failedToMatch;

      // Calculate the number of matches the manual mapping file
      // discards (i.e. does not include).  The manual mapping file
      // only includes those good matches.  So add up all statistics
      // calculated so far - this will be equal to all possible matches
      // for the manual mapping classes - and subtract the number of
      // good matches read out of the manual mapping file.  This is
      // used only for the Discard-Discard percentage.
      int manDiscard = correctMatch + correctDiscard + failedToMatch + incorrectMatch - MM.size();

      // Decide on label to show for which classes were processed.
      String classScope = ( flManualClassesOnly ) ? "manual mapping classes only" : "all classes";

      // Display all counts, along with the date and time the VxC completed.
      println( "--- VxC: Automatic Mapping Results --------------------------->" );
      println( "| Completed On:                                " + new Date().toString() );
      println( "| Total Manual Mapping Matches:                " + MM.size() + " (in " + manClasses.size() + " classes & subcl)" );
      println( "| Total Auto Mapping Matches:                  " + AM.size() + " (across " + classScope + ")" );
      println( "| Correct Match (MM and AM ok'ed):             " + correctMatch + "  ^" );
      println( "| Correct Discard (MM and AM discarded):       " + correctDiscard + "  ^");
      println( "| Failed To Match (MM ok'ed, AM discarded):    " + failedToMatch + "  v" );
      println( "| Incorrect Match (MM discarded, AM ok'ed):    " + incorrectMatch + "  v" );
      println( "| % Coverage Of OK-OK MM:                      " + roundPct( ( double ) correctMatch / MM.size() ) + "%" );
      println( "| % Coverage Of Discard-Discard MM:            " + roundPct( ( double ) correctDiscard / manDiscard ) + "%" );
      println( "-------------------------------------------------------------->" );

      // Display failed to match section.
      println( "--- VxC: Failed To Match ------------------------------------->" );

      // Print those manual mapping matches that the automatic matching
      // algorithm failed to match.  This is used just as additional information
      // to help tweak the algorithm's accuracy.
      if( failedToMatch != 0 )
         for( int m = 0; m < MM.size(); m++ )
         {
            Match mm = ( Match ) MM.get( m );

            if( !mm.isCorrectMatch )
               println( "" + mm );
         }

      // Else show 'none' if there were no manual mapping matches that the automatic
      // matching algorithm failed to match.
      else
         println( "| <none>" );

      println( "-------------------------------------------------------------->" );

      // Display incorrect match section.
      println( "--- VxC: Incorrect Match ------------------------------------->" );

      // Print those matches that the automatic matching algorithm
      // incorrectly effected.
      if( incorrectMatch != 0 )
         for( int a = 0; a < AM.size(); a++ )
         {
            Match am = ( Match ) AM.get( a );

            if( am.isManualClass && !am.isCorrectMatch )
               println( "" + am );
         }

      // Else show 'none' if there were matches the automatic matching algorithm
      // incorrectly effected.
      else
         println( "| <none>" );

      println( "-------------------------------------------------------------->" );
   }

   /**
    * Returns as an integer the first two significant figures of a floating point number
    * between 0 and 1, inclusive.  For example, 0.37945261 is converted to 38.  This is
    * done so that the returned value represents a percent value.
    *
    * @param d the floating point value to round and convert to a percentage
    * @return an integer value representing the rounded value of the input value times 100.
    * @see vn.Cyc#compareMatches()
    */
   private static int roundPct( double d )
   {
      return ( int ) ( d * 100 + 0.5 );
   }

   //////////////////////////
   // Supplemental Classes //
   //////////////////////////

   /**
    * This class represents a match between a VerbNet verb-frame pair
    * and a Cyc rule.  This match could either be a match specified by the
    * manual mapping file ( &lt;tuple> or &lt;tuple-m> elements) or a match
    * found by the automatic matching algorithm.  Here is an example match
    * from the manual mapping file:
    * <TABLE style='border:1px #000000 solid' cellpadding=4 align='center' width='80%'>
    *    <TR><TD><PRE> VerbNet Class: banish-10.2
    *    Member: extradite
    *    Frame: Basic Transitive
    *       Example: "The king banished the general."
    *       Syntax: %Agent V %Theme
    *       Semantics: cause(Agent, E) location(start(E), Theme, ?Source)
    *                  location(end(E), Theme, ?Destination)</PRE></TD></TR>
    *    <TR><TD style='border-top:1px #000000 solid'><PRE> RULE #388
    * [Def](verbSemTrans Extradite-TheWord 0 TransitiveNPFrame
    *          (and
    *             (isa :ACTION Extradition)
    *             (performedBy :ACTION :SUBJECT)
    *             (transferredObject :ACTION :OBJECT)))</PRE></TD></TR>
    * </TABLE>
    *
    * @see vn.Cyc#loadManualFile()
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   private static class Match
   {

      ////////////
      // Fields //
      ////////////

      /**
       * The verb of this match.  This will be the same for the VerbNet
       * verb-frame pair and the Cyc match because no {@link vn.Cyc.Match}
       * object is created without the naive constraint at least being
       * applied to a possible match.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc#findCycMatches(String, String, String, int, String)
       */
      String verb;

      /**
       * The VerbNet class to which the verb-frame pair in this match
       * belongs.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc#findCycMatches(String, String, String, int, String)
       */
      String vnClass;

      /**
       * Whether or not the VerbNet class for this match is covered
       * in the manual mapping file.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc#findCycMatches(String, String, String, int, String)
       * @see vn.Cyc#compareMatches()
       */
      boolean isManualClass;

      /**
       * The number of the Cyc rule involved in this match.  This is
       * assigned by the {@link vn.Cyc#addRuleNumbersAndCompress()} method
       * and is assigned in the order that the rules are found in the file.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc#findCycMatches(String, String, String, int, String)
       */
      String cycRuleNum;

      /**
       * A string representation of the VerbNet frame involved in the verb-frame
       * pair.  This is a conglomeration of the frame ID and the frame description.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc#findCycMatches(String, String, String, int, String)
       */
      String frame;

      /**
       * Whether or not a manual mapping match or an automatic match has a
       * an equal match in the opposite group.  When the automatic matching
       * algorithm makes a match that was included in the manual mapping file,
       * both the {@link vn.Cyc.Match} object in the manual group ({@link vn.Cyc#MM}) and the
       * {@link vn.Cyc.Match} object in the automatic
       * group ({@link vn.Cyc#AM}) have this instance variable set to <CODE>true</CODE>.
       * Use solely by the {@link vn.Cyc#compareMatches()} method when calculating
       * the final results.
       *
       * @see vn.Cyc#compareMatches()
       */
      boolean isCorrectMatch;

      /**
       * Whether or not this match object, if representing a manual mapping
       * match, was listed as a maybe match (via the &lt;tuple-m> element) inside the manual mapping file.
       * This instance variable is unused for those matches found during the
       * automatic matching algorithm.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc.Match#toString()
       */
      boolean maybe;

      /////////////////
      // Constructor //
      /////////////////

      /**
       * Constructs a {@link vn.Cyc.Match} object.
       *
       * @see vn.Cyc#loadManualFile()
       * @see vn.Cyc#findCycMatches(String, String, String, int, String)
       */
      Match() {}

      ///////////////////
      // Other Methods //
      ///////////////////

      /**
       * Returns a string representation of this {@link vn.Cyc.Match} object.
       * All instance variables except {@link vn.Cyc.Match#isManualClass}
       * are included in the representation.  If this method is being called
       * it is being called on a match that is in {@link vn.Cyc#MM} (a manual
       * mapping class match). The {@link vn.Cyc.Match#maybe}
       * instance variable is only included if it is true and manifests itself
       * as the string <CODE>(maybe)</CODE> appended to the normal representation.
       *
       * @return the string representation of the match
       * @see vn.Cyc#compareMatches()
       */
      public String toString()
      {
         String s = "| [CLASS " + vnClass + "] [VERB " + verb + "] [FRAME " + frame + "] [CYC RULE #" + cycRuleNum + "]";

         // Add an additional maybe if the match is a
         // maybe match from the manual mapping file.
         if( maybe )
            s += " (maybe)";

         return s;
      }

      /**
       * Returns whether or not two matches represent a match
       * between the same verb-frame pair and Cyc rule.  In other words,
       * if both matches have the same VerbNet class, verb, and VerbNet frame ID,
       * they represent the same VerbNet verb-frame pair.  If they both have the
       * same Cyc rule number, they represent the same Cyc rule.  The two
       * are equal if they both represent the same verb-frame pair and the same
       * Cyc rule.
       *
       * @param m the other {@link vn.Cyc.Match} object to which this object
       *        should be compared
       * @return whether or not this match is the same as the match given
       * @see vn.Cyc#compareMatches()
       */
      boolean equals( Match m )
      {
          return vnClass.equals( m.vnClass ) && verb.equals( m.verb ) &&
                 frame.equals( m.frame ) && cycRuleNum.equals( m.cycRuleNum );
      }
   }

   /**
    * Exception class for identifying if the Cyc input file is invalid.  It
    * must be in the format as described in {@link vn.Cyc#cycFile}.
    *
    * @see vn.Cyc#loadExternalData()
    */
   private static class InvalidCycInputFileException extends Exception
   {

      /**
       * Constructs the exception object with the given message.
       *
       * @param message the text of the exception
       */
      public InvalidCycInputFileException( String message )
      {
         super( message );
      }
   }
}

