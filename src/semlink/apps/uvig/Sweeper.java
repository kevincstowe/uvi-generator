
///////////////////
// UVI Generator //
// Derek Trumbo  //
///////////////////

package semlink.apps.uvig;


import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import enums.RoleValue;

import java.nio.file.Files;
/**
 * This class essentially defines the HTML that should be mapped to the XML.
 * The name Sweeper has no particular significance.  This class is probably
 * the most important class of the UVI Generator program.  The rest of the
 * program is just infrastructure for the purpose of allowing this class
 * to do its job (for the most part).<BR><BR>The power of this class is in its
 * flexibility for future developers or VerbNet users.  If another node is
 * added to VerbNet, this program can be extended fairly simply by adding the
 * proper <CODE>startNODE</CODE> and <CODE>endNODE</CODE> methods.  If the
 * tags are in the XML files, the act of declaring the methods to this file
 * (in the same manner the other methods are declared) makes your code active.
 * This is because reflection is used.  See {@link uvi.Generator#executeHTMLMethod(String, Node)}.
 * Furthermore, if you want to change the way the XML files are displayed as
 * HTML, changing is easy.  If you want to change how the frames are displayed,
 * example, you would look in <CODE>startFRAMES</CODE>, <CODE>startFRAME</CODE>,
 * <CODE>endFRAME</CODE>, and <CODE>endFRAMES</CODE>.  There the HTML is fairly
 * clearly generated based on the children and attributes of each node.<BR><BR>Remember
 * that the HTML is generated in an "in-order" fashion.  First the <CODE>startFRAMES</CODE>
 * is executed, then the <CODE>startFRAME</CODE>, then the <CODE>startDESCRIPTION</CODE>,
 * etc.  Then the end tags are executed in the opposite order: <CODE>endDESCRIPTION</CODE>,
 * <CODE>endFRAME</CODE>, <CODE>endFRAMES</CODE>.
 * <BR><BR>
 * It's ok if a "start" or "end" method does not exist for a given XML element.
 * If no matching method is found, it is ignored.  For example, those nodes
 * which are defined in the DTD not to have children nodes hardly need "end"
 * methods declared, since you can just put the "closing" HTML at the end of the
 * node's "start" method.
 * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
 * When this documentation was created, only *.html files were used.  Later, they were
 * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
 *
 * @see uvi.Generator#executeHTMLMethod(String, Node)
 * @see uvi.Q
 *
 * @author Derek Trumbo
 */
public class Sweeper
{

    ////////////
    // Fields //
    ////////////

    /**
     * The maximum number of verb columns to display in the 'Members' area.
     * Verbs are displayed alphabetically downwards.  Initially set to 4.
     *
     * @see uvi.Sweeper#startMEMBERS(Node)
     * @see uvi.Sweeper#endMEMBERS(Node)
     */
    static int NUM_MEM_COL = 4;

    /**
     * The number of verbs (rows) to display per column if the number of
     * verbs is less than INIT_MEM_ROW * NUM_MEM_COL.  Initially set to 5.
     *
     * @see uvi.Sweeper#startMEMBERS(Node)
     * @see uvi.Sweeper#endMEMBERS(Node)
     */
    static int INIT_MEM_ROW = 5;

    /**
     * The current nesting level of the classes.  The top-level class
     * has a indent level of zero.
     *
     * @see uvi.Sweeper#startVNCLASS(Node)
     * @see uvi.Sweeper#startVNSUBCLASS(Node)
     * @see uvi.Sweeper#endVNCLASS(Node)
     * @see uvi.Sweeper#endVNSUBCLASS(Node)
     */
    private static int curIndentLevel;

    /**
     * The number of members to be shown per column in the 'Members' area.
     * This variable is calculated in each class's and subclass's MEMBERS element.
     * If there is fewer than or equal to INIT_MEM_ROW * NUM_MEM_COL verbs, the
     * number of members per column is just INIT_MEM_ROW.  If the number of verbs
     * exceeds INIT_MEM_ROW * NUM_MEM_COL then a formula is applied to increase
     * the number of verbs per column, so as to keep to the maximum number of
     * columns specified by {@link uvi.Sweeper#NUM_MEM_COL}.
     *
     * @see uvi.Sweeper#startMEMBERS(Node)
     * @see uvi.Sweeper#endMEMBERS(Node)
     * @see uvi.Sweeper#printMembers()
     */
    private static int memPerCol;

    /**
     * A counter maintained by each member section to keep track of which
     * member (verb) is being printed currently.
     *
     * @see uvi.Sweeper#startMEMBERS(Node)
     * @see uvi.Sweeper#endMEMBERS(Node)
     * @see uvi.Sweeper#printMembers()
     */
    private static int curMem;

    /**
     * The number of members for the current class or subclass.  This is set
     * upon entering a MEMBERS element.
     *
     * @see uvi.Sweeper#startMEMBERS(Node)
     * @see uvi.Sweeper#endMEMBERS(Node)
     */
    private static int totalMem;

    /**
     * The level of nested SELRESTRS being processed.  The definition of SELRESTRS
     * is recursive in the DTD (&lt;!ELEMENT SELRESTRS (SELRESTR|SELRESTRS)*&gt;)
     * and a | or an & should be printed if it's not the top-level element.
     * Currently, this only happens in a few files:  bring-11.3.html, carry-11.4.html,
     * cheat-10.6.html, drive-11.5.html, pour-9.5.html, send-11.1.html, slide-11.2.html,
     * spray-9.7.html, steal-10.5.html, throw-17.1.html.
     *
     * @see uvi.Sweeper#startSELRESTRS(Node)
     * @see uvi.Sweeper#endSELRESTRS(Node)
     */
    private static int restrsLevel = 0;

    /**
     * Whether or not the SELRESTRS about to be entered is under a PREP element
     * (as compared to a THEMROLE element).  This is set upon entering and
     * leaving a PREP element.  If the SELRESTRS is from a PREP element, is is
     * for a preposition class and is displayed in a different color
     *
     * @see uvi.Sweeper#startPREP(Node)
     * @see uvi.Sweeper#endPREP(Node)
     * @see uvi.Sweeper#startSELRESTRS(Node)
     * @see uvi.Sweeper#endSELRESTRS(Node)
     */
    private static boolean prepSelRestrs;

    /**
     * Used by various types of nodes for which there might be a list to
     * display to know if another of their same kind has already been
     * shown, so as to print a space, or a comma and a space.
     *
     * @see uvi.Sweeper#startSELRESTRS(Node)
     * @see uvi.Sweeper#startSELRESTR(Node)
     * @see uvi.Sweeper#startARGS(Node)
     * @see uvi.Sweeper#startARG(Node)
     * @see uvi.Sweeper#startSYNRESTRS(Node)
     * @see uvi.Sweeper#startSYNRESTR(Node)
     */
    private static boolean hasBeenOther;

    /**
     * Whether or not nodes which specify logical operators for their
     * children are using 'or'.  This only happens with SELRESTRS
     * and SYNRESTRS.  When their children execute, they know whether to
     * print a | or an &.
     *
     * @see uvi.Sweeper#startSELRESTRS(Node)
     * @see uvi.Sweeper#startSELRESTR(Node)
     * @see uvi.Sweeper#startSYNRESTRS(Node)
     * @see uvi.Sweeper#startSYNRESTR(Node)
     */
    private static boolean usingOr;

    /**
     * Used only to supply the correct web page link when add a member
     * to the index.  This will contain the value "send-11.1.html", for
     * example, for the main class and all subclasses being processed in
     * this file.
     *
     * @see uvi.Sweeper#setCurFile(String)
     * @see uvi.Sweeper#printMembers()
     */
    private static String curFile = "";

    /**
     * The name of the current class or subclass being processed.  This is
     * used not just to print the class name in the header, but also to
     * have for adding members to the index, and most importantly, displaying
     * contextual information when errors or warnings occur (so the user
     * knows in which class the error or warning occurred).
     *
     * @see uvi.Sweeper#startVNCLASS(Node)
     * @see uvi.Sweeper#startVNSUBCLASS(Node)
     * @see uvi.Sweeper#printMembers()
     */
    private static String curClass = "";

    /**
     * Holds all the member information for a given class/subclass.  Information
     * for a single member consists of the verb and its list of WordNet senses.
     * It is cleared at the start of each class/subclass, loaded up during the
     * <MEMBER> tag scanning, and sorted & dumped upon reaching the </MEMBERS>
     * tag.  This was only added to facilitate sorted member sections.  If sorting
     * was not a priority, then members would just be printed as they were
     * reached.
     *
     * @see uvi.Sweeper#startMEMBERS(Node)
     * @see uvi.Sweeper#startMEMBER(Node)
     * @see uvi.Sweeper#printMembers()
     */
    private static ArrayList members = new ArrayList();

    
    /**
     * Colors to group fn mappings by
     */
    private static String[] colors = {"green","blue","pink","orange","yellow","cyan","red","salmon","teal","navy","fuschia","lime","purple","gold"};
    
    //Mappings from top level vn class to fn-color map
    private static Map<String, Map<String,String>> class_to_color_map = new HashMap<String, Map<String,String>>();
    
    
    /////////////////
    // Constructor //
    /////////////////

    /**
     * This constructor is private because the class is not intended to ever
     * be instantiated.  The UVI generation is a very procedural process and
     * thus all the members are static.
     */
    private Sweeper() {}

    ////////////////////
    // Helper Methods //
    ////////////////////

    /**
     * Used as shorthand for <CODE>System.out.println</CODE>.
     *
     * @param s the string to print.
     * @see java.io.PrintStream#println(String)
     */
    private static void println( String s ) { System.out.println( s ); }

    /**
     * Used as shorthand for <CODE>System.out.print</CODE>.
     *
     * @param s the string to print.
     * @see java.io.PrintStream#print(String)
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
     * Sets the current file being processed.
     *
     * @param newFile the current file being processed in
     *        {@link uvi.Generator#generateHTMLFiles()}
     * @see uvi.Generator#generateHTMLFiles()
     */
    public static void setCurFile( String newFile )
    {
        curFile = newFile;
    }

    /**
     * Whether or not the given XML element has children with a given
     * tag name.
     *
     * @param n the node whose children should be checked
     * @param which the tag name to look for among the children
     * @return whether or not the node contains one or more children of
     *         the given type
     */
    private static boolean hasKids( Node n, String which )
    {
        return numKids( n, which ) != 0;
    }

    /**
     * Counts the number of children of a given XML element who have
     * the given tag name.
     *
     * @param n the node whose children should be checked
     * @param which the tag name to look for among the children
     * @return the number of children of the given type that
     *         the node contains
     */
    private static int numKids( Node n, String which )
    {
        NodeList kids = n.getChildNodes();

        int whichKid = 0;

        for( int k = 0; k < kids.getLength(); k++ )
        {
            Node kid = kids.item( k );

            if( kid.getNodeName().equals( which ) ) {
                whichKid++;
            }
        }
        
        return whichKid;
    }

    /**
     * Shows warnings concerning "curious" conditions in the VerbNet
     * organization.  The warnings are:
     * <UL>
     *   <LI>A class/subclass has a single subclass but no members</LI>
     *   <LI>A class/subclass has zero frames and zero subclasses</LI>
     *   <LI>A class/subclass has zero members and zero subclasses</LI>
     * </UL>
     *
     * @param mem the number of members the class/subclass has
     * @param fra the number of frames the class/subclass has
     * @param subc the number of subclasses the class/subclass has
     */
    private static void warningChecks( int mem, int fra, int subc )
    {
        if( mem == 0 && subc == 1 ) {
            eprintln( "WARNING: [" + curClass + "] Zero members in a class with a single subclass.  Possible merger?" );
        }

        if( fra == 0 && subc == 0 ) {
            eprintln( "WARNING: [" + curClass + "] Zero frames in a class with no further subclasses.  Incomplete class?" );
        }

        if( mem == 0 && subc == 0 ) {
            eprintln( "WARNING: [" + curClass + "] Zero members in a class with no further subclasses.  Incomplete class?" );
        }
    }

    /**
     * Prints the content of the Class Hierarchy box.  This is done recursively.
     *
     * @param cls the top level XML element representing the main class being printed
     * @param level the current subclass level (0 for the main class).  This is
     *        used to perform the proper indentation.
     * @param rootName the name of the main class being printed recursively.
     *        This is used only to flag subclasses whose names do not
     *        correspond to the naming convention of the main class.
     * @see uvi.Sweeper#startVNCLASS(Node)
     */
    private static void classHierarchyPrint( Element cls, int level, String rootName )
    {
        String indent = "";
        int subcCount = 0;

        // Print identation spacing.
        for( int l = 0; l < level; l++ ) {
            indent += "&nbsp;&nbsp;&nbsp;&nbsp;";
        }

        // Show an invalid-subclass-name warning.
        if( !cls.getAttribute( "ID" ).startsWith( rootName ) ) {
            eprintln( "ERROR: [" + rootName + "] Subclass \"" + cls.getAttribute( "ID" ) +
            "\" doesn't follow root class naming convention." );
        }

        // Show a star next to the name if there exist comments for the subclass.
        String star = "<?php print existCommentsStar( '" + cls.getAttribute( "ID" ) + "' ); ?>";

        // Show the name inside a link to the proper class/subclass.
        Q.oh( 7, indent + "<NOBR><A href='#" + cls.getAttribute( "ID" ) + "'>" + cls.getAttribute( "ID" ) +
            "<FONT class='Small'>" + star + "</FONT></A></NOBR><BR>" );

        NodeList kids = cls.getChildNodes();

        // Recursively print the links for this class's subclasses.
        for( int k = 0; k < kids.getLength(); k++ )
        {
            Node kid = kids.item( k );
            String nodeName = kid.getNodeName();

            if( nodeName.equalsIgnoreCase( "SUBCLASSES" ) )
            {
                Element scls = ( Element ) kid;
                NodeList skids = scls.getChildNodes();

                for( int j = 0; j < skids.getLength(); j++ )
                {
                    Node skid = skids.item( j );
                    String sNodeName = skid.getNodeName();

                    if( sNodeName.equalsIgnoreCase( "VNSUBCLASS" ) )
                    {
                        Element newCls = ( Element ) skid;
                        classHierarchyPrint( newCls, level + 1, rootName );
                        subcCount++;
                    }
                }
            }
        }

        // Show if there are no subclasses for the main class.
        if( level == 0 && subcCount == 0 ) {
            Q.oh( 7, "&nbsp;&nbsp;&nbsp;&nbsp;<FONT class='AbsenceOfItems'>no subclasses</FONT>" );
        }
    }

    //////////////////////////////////
    // HTML Generators For XML Tags //
    //////////////////////////////////

    /**
     * Generates the HTML for the beginning of a VNCLASS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active VNCLASS element
     */
    public static void startVNCLASS( Node n )
    {
        curClass = ( ( Element ) n ).getAttribute( "ID" );

        // Add this class name to the complete list.
        Generator.classHierarchy.add( curClass + "$$" + curFile );

        curIndentLevel = 0;

        int m = numKids( ( ( Element ) n ).getElementsByTagName( "MEMBERS" ).item( 0 ),    "MEMBER" );
        int f = numKids( ( ( Element ) n ).getElementsByTagName( "FRAMES" ).item( 0 ),     "FRAME" );
        int s = numKids( ( ( Element ) n ).getElementsByTagName( "SUBCLASSES" ).item( 0 ), "VNSUBCLASS" );

        String stats = "Members: " + m + ", Frames: " + f;

        Q.oh( 2, "<A name='" + curClass + "'></A>" );
        Q.oh( 2, "<TABLE width='100%' cellpadding=0 cellspacing=0><TR valign='top'>" );
        Q.oh( 3, "<TD width='75%' align='center'>" );
        Q.oh( 4, "<TABLE class='ClassTitleBox' cellspacing=0 cellpadding=7 width='100%'>" );
        Q.oh( 5, "<TR>" );
        Q.oh( 6, "<?php $star = existCommentsStar( '" + curClass + "' ); if( $star == '*' ) {" );
        Q.oh( 6, "?><TD width='20%' class='SubclassTopLink' align='left'>" );
        Q.oh( 7, "<NOBR><A href='#" + curClass + "-comments' class='GoCommentsLink'>Go To Comments</A></NOBR>" );
        Q.oh( 6, "</TD>" );
        Q.oh( 6, "<?php } else {" );
        Q.oh( 6, "?><TD width='20%' class='SubtleText' align='left'>No Comments</TD>" );
        Q.oh( 6, "<?php }" );
        Q.oh( 6, "?><TD align='center'><NOBR>" + curClass + "</NOBR><BR><FONT class='SubtleText'>" + stats + "</FONT></TD>" );
        Q.oh( 6, "<TD width='20%' class='SubclassTopLink' align='right'>" );
        Q.oh( 7, "<NOBR><A href='javascript:postComment(\"" + curFile + "\", \"" + curClass + "\");'>Post Comment</A></NOBR>" );
        Q.oh( 6, "</TD>" );
        Q.oh( 5, "</TR>" );
        Q.oh( 4, "</TABLE>" );
        Q.oh( 3, "</TD>" );
        Q.oh( 3, "<TD>&nbsp;</TD>" );
        Q.oh( 3, "<TD align='right' width='25%'>");
        Q.oh( 4, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
        Q.oh( 5, "<TR class='ClassSectionHeadRow'><TD>Class Hierarchy</TD></TR>" );
        Q.oh( 5, "<TR>" );
        Q.oh( 6, "<TD>" );

        classHierarchyPrint( ( Element ) n, 0, curClass );

        Q.oh( 6, "</TD>" );
        Q.oh( 5, "</TR>" );
        Q.oh( 4, "</TABLE>" );
        Q.oh( 3, "</TD>" );
        Q.oh( 2, "</TR></TABLE>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "" );

        warningChecks( m, f, s );
    }

    /**
     * Generates the HTML for the end of a VNCLASS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active VNCLASS element
     */
    public static void endVNCLASS( Node n )
    {
        Q.oh( 2, "<FORM name='frmPostComment' method='post' action='../postcomment.php'>" );
        Q.oh( 3, "<INPUT type='hidden' name='txtFileName'>" );
        Q.oh( 3, "<INPUT type='hidden' name='txtClassName'>" );
        Q.oh( 2, "</FORM>" );
    }

    /**
     * Generates the HTML for the beginning of a VNSUBCLASS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active VNSUBCLASS element
     */
    public static void startVNSUBCLASS( Node n )
    {
        curClass = ( ( Element ) n ).getAttribute( "ID" );

        curIndentLevel++;

        // Construct a prefix for the subclass.
        String subc = "";
        for( int x = 0; x < curIndentLevel; x++ ) {
            subc += "@";
        }

        // Add this class name to the complete list.
        Generator.classHierarchy.add( subc + curClass + "$$" + curFile );

        int m = numKids( ( ( Element ) n ).getElementsByTagName( "MEMBERS" ).item( 0 ),    "MEMBER" );
        int f = numKids( ( ( Element ) n ).getElementsByTagName( "FRAMES" ).item( 0 ),     "FRAME" );
        int s = numKids( ( ( Element ) n ).getElementsByTagName( "SUBCLASSES" ).item( 0 ), "VNSUBCLASS" );

        // Make sure the proper string is appended with the name of the current
        // file being generated since this class/file does indeed have at least
        // one subclass.
        if( Generator.classesWithSubclasses.indexOf( "@@" + curFile + "@@" ) == -1 ) {
            Generator.classesWithSubclasses += "@@" + curFile + "@@";
        }

        // Increment the number of subclasses that exist.
        Generator.totalSubclasses++;

        String stats = "Members: " + m + ", Frames: " + f;

        Q.oh( 2, "" );
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "<A name='" + curClass + "'></A>" );
        Q.oh( 2, "<TABLE cellspacing=0 cellpadding=0 width='100%'>" );
        Q.oh( 3, "<TR>" );
        Q.oh( 4, "<TD width='5%'>&nbsp;</TD>" );
        Q.oh( 4, "<TD>" );
        Q.oh( 5, "<TABLE class='SubclassTitleBox' cellspacing=0 cellpadding=7 width='100%'>" );
        Q.oh( 6, "<TR>" );
        Q.oh( 6, "<?php $star = existCommentsStar( '" + curClass + "' ); if( $star == '*' ) {" );
        Q.oh( 6, "?><TD width='20%' class='SubclassTopLink'>" );
        Q.oh( 7, "<NOBR><A href='#" + curClass + "-comments' class='GoCommentsLink'>Go To Comments</A></NOBR>" );
        Q.oh( 6, "</TD>" );
        Q.oh( 6, "<?php } else {" );
        Q.oh( 6, "?><TD width='20%' class='SubtleText'>No Comments</TD>" );
        Q.oh( 6, "<?php }" );
        Q.oh( 7, "?><TD align='center' width='60%'>" + curClass + "<BR><FONT class='SubtleText'>" + stats + "</FONT></TD>" );
        Q.oh( 7, "<TD class='SubclassTopLink' width='20%' align='right'>" );
        Q.oh( 8, "<NOBR><A href='javascript:postComment(\"" + curFile + "\", \"" + curClass + "\");'>Post Comment</A> | " );
        Q.oh( 9, "<A href='#top'>Top</A></NOBR>" );
        Q.oh( 7, "</TD>" );
        Q.oh( 6, "</TR>" );
        Q.oh( 5, "</TABLE>" );
        Q.oh( 5, "" );
        Q.oh( 5, "<BR>" );
        Q.oh( 5, "" );

        warningChecks( m, f, s );
    }

    /**
     * Generates the HTML for the end of a VNSUBCLASS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active VNSUBCLASS element
     */
    public static void endVNSUBCLASS( Node n )
    {
        curIndentLevel--;

        Q.oh( 4, "" );
        Q.oh( 4, "<!-- Close Subclass -->" );
        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );
    }

    /**
     * Generates the HTML for the beginning of a MEMBERS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active MEMBERS element
     */
    public static void startMEMBERS( Node n )
    {
        Q.oh( 2, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
        Q.oh( 3, "<TR class='ClassSectionHeadRow'>" );
        Q.oh( 4, "<TD>Members</TD>" );
        Q.oh( 4, "<TD align='right'>" );
        Q.oh( 5, "<A href='javascript:showMembersKey();'><IMG src='../images/key.gif' width=27 height=15 alt='Key' border=0></A>" );
        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 3, "<TR>" );
        Q.oh( 4, "<TD colspan=2>" );
        
        // Clear out the members from the previous class/subclass.
        members.clear();

        totalMem = numKids( n, "MEMBER" );

        if( totalMem == 0 ) {
            Q.oh( 5, "&nbsp;&nbsp;&nbsp;&nbsp;<FONT class='AbsenceOfItems'>no members</FONT>" );
        } else
        {
            Q.oh( 5, "<TABLE width='100%'>" );
            Q.oh( 6, "<TR valign='top'>" );


            // Calculate the number of members per column.
            if( totalMem < INIT_MEM_ROW ) {
                memPerCol = totalMem;
            } else if( totalMem <= NUM_MEM_COL * INIT_MEM_ROW ) {
                memPerCol = INIT_MEM_ROW;
            } else
            {
                int extra = ( totalMem % NUM_MEM_COL == 0 ) ? 0 : 1;
                memPerCol = ( int ) ( ( ( double ) totalMem / NUM_MEM_COL ) + extra );
            }

            curMem = 0;
        }
    }

    /**
     * Generates the HTML for the end of a MEMBERS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active MEMBERS element
     * @see uvi.Sweeper#printMembers()
     */
    public static void endMEMBERS( Node n )
    {
        if( totalMem != 0 )
        {

            // Print all the HTML for the class members whose information was collected
            // and stored during the <MEMBER> tag scanning.
            printMembers();

            if( curMem % memPerCol != 0 )
            {
                while( curMem % memPerCol != 0 )
                {
                    Q.oh( 9, "<TR><TD>&nbsp;</TD></TR>" );
                    curMem++;
                }

                Q.oh( 8, "</TABLE>" );
                Q.oh( 7, "</TD>" );
            }

            // Fill the rest of the table with empty cells.
            if( totalMem >= INIT_MEM_ROW ) {
                while( curMem < NUM_MEM_COL * INIT_MEM_ROW )
                {
                    Q.oh( 7, "<TD width='25%' align='left'>" );
                    Q.oh( 8, "<TABLE class='YesCaps'>" );
                    Q.oh( 9, "<TR><TD>&nbsp;</TD></TR>" );
                    curMem++;

                    while( curMem % memPerCol != 0 )
                    {
                        Q.oh( 9, "<TR><TD>&nbsp;</TD></TR>" );
                        curMem++;
                    }

                    Q.oh( 8, "</TABLE>" );
                    Q.oh( 7, "</TD>" );
                }
            }

            Q.oh( 6, "</TR>" );
            Q.oh( 5, "</TABLE>" );
        }

        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "" );
    }

    /**
     * Stores all the information for this member into the {@link uvi.Sweeper#members}
     * array to be processed later in {@link uvi.Sweeper#endMEMBERS(Node)}, once it is completely
     * sorted.  A formal sorting algorithm is not used, but rather when each new member
     * is added inside this method, it is inserted in alphabetical order.  Also, the
     * sorting only occurs if the "-s" or "--sort" flag was supplied to the program.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active MEMBER element
     */
    public static void startMEMBER( Node n )
    {
        String vbName = ( ( Element ) n ).getAttribute( "name" );
        String wn     = ( ( Element ) n ).getAttribute( "wn" );
        String grp    = ( ( Element ) n ).getAttribute( "grouping" );
        String feats  = ( ( Element ) n ).getAttribute( "features" );
        
        // If there are no WordNet senses for this verb, then
        // Make sure at least one character survives to separate
        // the delimiters.
        if( wn.equals( "" ) ) {
            wn = " ";
        }
        if( grp.equals( "" ) ) {
            grp = " ";
        }

        if( feats.equals( "" )) {
        	feats = " ";
        }
        // If sorting is disabled or this is the first member,
        // just add its information to the array.
        if( !Generator.flSort || members.size() == 0 ) {
            members.add( vbName + "@@" + wn + "@@" + grp + "@@" + feats + "@@");

            // Else if sorting is enabled and this isn't the first member...
        } else
        {
            int m;

            // Locate the insertion point.
            for( m = members.size(); m > 0; m-- )
            {

                // Isolate current member.
                String mm = ( String ) members.get( m - 1 );

                // Strip off WordNet senses.
                mm = mm.substring( 0, mm.indexOf( "@@" ) );

                // If the array list entry is lexicographically less than
                // the new verb name, then it's in sorted order.
                if( mm.compareTo( vbName ) < 0 ) {
                    break;
                }
            }

            // Add the verb at the located position.
            members.add( m, vbName + "@@" + wn + "@@" + grp + "@@" + feats + "@@");
        }
    }

    /**
     * Generates the HTML for the end of a MEMBER element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT MEMBER EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startMEMBER(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active MEMBER element
     */
    public static void endMEMBER( Node n )
    {
        // not useful because <!ELEMENT MEMBER EMPTY>
    }

    /**
     * Prints the HTML for all the members in the current class/subclass.
     * This directly uses the information gathered in {@link uvi.Sweeper#startMEMBER(Node)}.
     *
     * @see uvi.Sweeper#startMEMBER(Node)
     * @see uvi.Sweeper#endMEMBERS(Node)
     * @see uvi.Generator#addOthers(int)
     * @see uvi.WordNet
     * @see uvi.VN_FN_Map
     */
    private static void printMembers()
    {
    	//Get colors to print FN mappings
    	Set<String> class_set = new HashSet<String>(Arrays.asList(VN_FN_Map.getFrameNet( curClass, "" ).split("@@")));
    	List<String> all_fns = new ArrayList<String>(class_set);
 	
    	Map<String, String> fn_to_color_map;
    	
    	if (class_to_color_map.containsKey(curClass.split("-")[0])){
	    	fn_to_color_map = class_to_color_map.get(curClass.split("-")[0]);
	    }
    	else{
    		fn_to_color_map = new HashMap<String,String>();
    		for (int i = 0; i < colors.length; i++){
	    		if (i < all_fns.size()){
	    			fn_to_color_map.put(all_fns.get(i), colors[i]);
	    		}
	    	}
    		class_to_color_map.put(curClass.split("-")[0], fn_to_color_map);
    	}
    	
   	
        for( int v = 0; v < members.size(); v++ )
        {

            // Extract parts of member information.
            String[] parts = ( ( String ) members.get( v ) ).split( "@@" );
            String vbName  = parts[ 0 ];
            String wn      = parts[ 1 ].trim();    // Trim important.
            String grp     = parts[ 2 ].trim();
            String feats   = parts[ 3 ].trim();
            
            // Start the current column.
            if( curMem % memPerCol == 0 )
            {
                Q.oh( 7, "<TD width='25%' align='left'>" );
                Q.oh( 8, "<TABLE class='YesCaps'>" );
            }

            // Initialize the link string strings.
            String fnLinks  = "";
            String wnLinks  = "";
            String grpLinks = "";

            if(!Generator.flNoVnWnLinks) {

                // If the current member has WordNet information...
                if( !wn.equals( "" ) )
                {
                    String[] senses = wn.split( "  *" );     // The wn= attribute value is space-delimited.
                    wnLinks = "wn ";

                    // Add a link (in the form of a WordNet sense number)
                    // for each WordNet sense.
                    for( int s = 0; s < senses.length; s++ )
                    {

                        // Get the sense number associated with this sense key.
                        // These were loaded ahead of time in the Generator.addOthers method
                        // combined with the WordNet class.
                        String senseNum = WordNet.getSenseNumber( curClass, senses[ s ] );

                        String modKey = senses[ s ].replace( '%', '-' );

                        String wnVersion = Generator.getProperty("wnVersion", "");
                        wnVersion = "v" + wnVersion.replace('.', '-');

                        wnLinks += "<A href='javascript:wordNetMessage( \"" + wnVersion + "\", \"" + vbName + "\", \"" + senseNum + "\", \"" + modKey + "\" );' " +
                        "class='MemberLinkNumbers'>" + senseNum + "</A>";

                        // Add a space between the links.
                        if( s != senses.length - 1 ) {
                            wnLinks += ", ";
                        }
                    }
                }
            }

            // If the current member has grouping information...
            if( !grp.equals( "" ) )
            {
                if(!grp.matches("[a-z]+\\.[0-9][0-9]( [a-z]+\\.[0-9][0-9])*")) {
                    eprintln( "ERROR: [" + curClass + "] The grouping group name \"" + grp + "\" is invalid." );
                } else {
                    String[] groups = grp.split( "  *" );     // The grouping= attribute value is space-delimited.
                    grpLinks = "g ";

                    // Add a link (in the form of a grouping number)
                    // for each group.
                    for( int g = 0; g < groups.length; g++ )
                    {
                        String[] gparts = groups[g].split("\\.");

                        grpLinks += "<A href='" + Generator.grpURL + gparts[0] + "-v.html" +
                        "' class='MemberLinkNumbers'>" + Integer.parseInt(gparts[1]) + "</A>";

                        // Add a space between the links.
                        if( g != groups.length - 1 ) {
                            grpLinks += ", ";
                        }
                    }
                }
            }

            if(!Generator.flNoVnFnMap) {

                // Get an array containing all FrameNet frames associated with this class and verb.
                String[] fn = VN_FN_Map.getFrameNet( curClass, vbName ).split( "@@" );

                // If the current member has FrameNet information...
                if( !fn[ 0 ].equals( "" ) )
                {
                    fnLinks = "fn ";

                    String fnUrlPattern = Generator.getProperty("fnUrlPattern", "ErrorNoFnUrlPatternDefined-{1}");

                    // Add a link for each FrameNet link.
                    for( int f = 0; f < fn.length; f++ )
                    {
                        String fullUrl = fnUrlPattern.replaceAll("\\{1\\}", fn[ f ]);

                        fnLinks += "<A href='" + fullUrl +
                        "' class='MemberLinkNumbers' " +
                        "style='color:" + fn_to_color_map.get(fn[f]) + "'>" + ( all_fns.indexOf(fn[f]) + 1) + "</A>";

                        // Add a space between the links.
                        if( f != fn.length - 1 ) {
                            fnLinks += ", ";
                        }
                    }
                }
            }

            //Create features string
            
            if ( !feats.equals( "" ) ){
            	String buf = " [<FONT class=MemberLinks>";
            	for (String f : feats.split(" ")){
                    Generator.refMap.addMembership( "VerbFeatures", f.substring(1) );

            		buf += "<a href=\"../verbfeatures/" + f.substring(1) + ".php\">"+f+"</a> ";
            	}
            	buf += "</FONT>]";
            }
            
            // If there are any links for this verb...
            if( !fnLinks.equals( "" ) || !wnLinks.equals( "" ) || !grpLinks.equals(""))
            {
            	String text_color = "";
            	String[] fn = VN_FN_Map.getFrameNet( curClass, vbName ).split( "@@" );
                if( !fn[ 0 ].equals( "" ) )
                	text_color = "style='color:" + fn_to_color_map.get(fn[0]) + "'";
                Q.oh( 9, "<TR><TD class='MemberCell'><a href=\"../search.php?txtSearchRequest=" + vbName + "\" class=VerbLinks title=\"" + feats + "\"" + text_color + ">" + vbName + "</a><FONT class=MemberLinks>" );

                Q.oh(10, false, "(");

                // Add any FrameNet links.
                if( !fnLinks.equals( "" ) )
                {
                    Q.oh( fnLinks );
                }

                // Add any WordNet links.
                if( !wnLinks.equals( "" ) )
                {
                    if(!fnLinks.equals("")) {
                        Q.oh("; ");
                    }
                    Q.oh( wnLinks );
                }

                // Add any grouping links.
                if( !grpLinks.equals( "" ) )
                {
                    if(!fnLinks.equals("") || !wnLinks.equals("")) {
                        Q.oh("; ");
                    }
                    Q.oh( grpLinks );
                }
                
                Q.oh(-1, true, ")");
                
                Q.oh( 9, "</FONT>" );
            } else {
                Q.oh( 9, "<TR><TD class='MemberCell'><a href=\"../search.php?txtSearchRequest=" + vbName + "\" class=VerbLinks title=\"" + feats + "\" >" + vbName + "</TD></TR>");
            }
            
            // If this is the last member in the column, close the column.
            if( curMem % memPerCol == memPerCol - 1 )
            {
                Q.oh( 8, "</TABLE>" );
                Q.oh( 7, "</TD>" );
            }

            // Add this verb to the index.
            Index.addLink( vbName, Generator.DS_VERBNET, curClass, curFile );

            // Increment the member counter.
            curMem++;
        }
    }

    /**
     * Generates the HTML for the beginning of a THEMROLES element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active THEMROLES element
     */
    public static void startTHEMROLES( Node n )
    {
        Q.oh( 2, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
        Q.oh( 3, "<TR class='ClassSectionHeadRow'>" );
        Q.oh( 4, "<TD>Roles</TD>" );
        Q.oh( 4, "<TD align='right'>" );
        Q.oh( 5, "<A href='reference.php'><IMG src='../images/ref.gif' width=26 height=15 alt='Reference' border=0></A>" );
        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 3, "<TR>" );
        Q.oh( 4, "<TD>" );

        if( !hasKids( n, "THEMROLE" ) ) {
            Q.oh( 5, "&nbsp;&nbsp;&nbsp;&nbsp;<FONT class='AbsenceOfItems'>no roles</FONT>" );
        } else {
            Q.oh( 5, "<UL>" );
        }
    }

    /**
     * Generates the HTML for the end of a THEMROLES element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active THEMROLES element
     */
    public static void endTHEMROLES( Node n )
    {
        if( hasKids( n, "THEMROLE" ) ) {
            Q.oh( 5, "</UL>" );
        }

        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );
    }

    /**
     * Generates the HTML for the beginning of a THEMROLE element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active THEMROLE element
     */
    public static void startTHEMROLE( Node n )
    {
        String tr = ( ( Element ) n ).getAttribute( "type" );
        Q.oh( 6, false, "<LI><a href=\"../themroles/" + tr + ".php\" class=VerbLinks>" + tr + "</a>");

        Generator.refMap.addMembership( "GenThemRole", tr );
    }

    /**
     * Generates the HTML for the end of a THEMROLE element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active THEMROLE element
     */
    public static void endTHEMROLE( Node n )
    {
        Q.oh( true, "</LI>" );
    }

    /**
     * Generates the HTML for the beginning of a SELRESTRS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SELRESTRS element
     */
    public static void startSELRESTRS( Node n )
    {
        if( hasKids( n, "SELRESTR" ) )
        {
            if( restrsLevel != 0 ) {
                Q.oh( usingOr ? " |" : " &" );
            }

            if( !prepSelRestrs ) {
                Q.oh( " <FONT class='SelRestrs'>[" );
            } else {
                Q.oh( "{" );
            }

            // Logical and's should be left out, which means it's implied.
            if( ( ( Element ) n ).getAttribute( "logic" ).equals( "and" ) ) {
                eprintln( "WARNING: [" + curClass + "] SELRESTRS specifies logic=\"and\" instead of leaving it out." );
            }

            if( ( ( Element ) n ).getAttribute( "logic" ).equals( "or" ) ) {
                usingOr = true;
            } else {
                usingOr = false;
            }
        }

        hasBeenOther = false;

        restrsLevel++;
    }

    /**
     * Generates the HTML for the end of a SELRESTRS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SELRESTRS element
     */
    public static void endSELRESTRS( Node n )
    {
        if( hasKids( n, "SELRESTR" ) )
        {
            if( !prepSelRestrs ) {
                Q.oh( false, "]</FONT>" );
            } else {
                Q.oh( false, "}" );
            }
        }

        restrsLevel--;
    }

    /**
     * Generates the HTML for the beginning of a SELRESTR element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SELRESTR element
     */
    public static void startSELRESTR( Node n )
    {
        String sp = hasBeenOther ? " " : "";
        String ex = "";
        String sr = ( ( Element ) n ).getAttribute( "type" );

        if( hasBeenOther ) {
            ex = usingOr ? "| " : "& ";
        }
        
        Q.oh( sp + ex + ( ( Element ) n ).getAttribute("Value") + "<a href=\"../selrestrs/" + sr + ".php\">" + sr + "</a>" );

        Generator.refMap.addMembership( "SelRestr", sr );

        hasBeenOther = true;
    }

    /**
     * Generates the HTML for the end of a SELRESTR element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT SELRESTR EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startSELRESTR(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SELRESTR element
     */
    public static void endSELRESTR( Node n )
    {
        // not useful because <!ELEMENT SELRESTR EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a FRAMES element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FRAMES element
     */
    public static void startFRAMES( Node n )
    {
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
        Q.oh( 3, "<TR class='ClassSectionHeadRow'>" );
        Q.oh( 4, "<TD>Frames</TD>" );
        Q.oh( 4, "<TD align='right'>" );
        Q.oh( 5, "<A href='reference.php'><IMG src='../images/ref.gif' width=26 height=15 alt='Reference' border=0></A>" +
            "<FONT size='-3'>&nbsp;</FONT>" +
        "<A href='javascript:showFrameKey();'><IMG src='../images/key.gif' width=27 height=15 alt='Key' border=0></A>" );
        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 3, "<TR>" );
        Q.oh( 4, "<TD colspan=2>" );

        if( !hasKids( n, "FRAME" ) ) {
            Q.oh( 5, "&nbsp;&nbsp;&nbsp;&nbsp;<FONT class='AbsenceOfItems'>no frames</FONT>" );
        }
    }

    /**
     * Generates the HTML for the end of a FRAMES element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FRAMES element
     */
    public static void endFRAMES( Node n )
    {
        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );

        // Print the comments for the main class.
        Q.oh( 2, "<?php printCommentBox( '" + curClass + "', 0, 0, 0, '..' ); ?>" );
    }

    /**
     * Generates the HTML for the beginning of a FRAME element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FRAME element
     */
    public static void startFRAME( Node n )
    {
        Q.oh( 5, "<TABLE class='YesCaps' width='100%' cellspacing=2 cellpadding=0>" );
    }

    /**
     * Generates the HTML for the end of a FRAME element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FRAME element
     */
    public static void endFRAME( Node n )
    {
        Q.oh( 5, "</TABLE>" );
    }

    /**
     * Generates the HTML for the beginning of a DESCRIPTION element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active DESCRIPTION element
     */
    public static void startDESCRIPTION( Node n )
    {
        String p = ( ( Element ) n ).getAttribute( "primary" ).trim().replaceAll("\\s+", " ");
        String s = ( ( Element ) n ).getAttribute( "secondary" ).trim().replaceAll("\\s+", " ");

        // Individually record both the primary and secondary descriptions.
        if( !p.equals("") ) {
            Generator.refFrameIndMap.addMembership( "primary", p );
        }
        if( !s.equals("") ) {
            Generator.refFrameIndMap.addMembership( "secondary", s );
        }

        if( !s.equals( "" ) ) {
            Generator.refFrameBothMap.addMembership( p, s );
        } else {
            Generator.refFrameBothMap.addMembership( p, "<I>(no secondary frame type)</I>" );
        }
        ////////////////////////////////

        Q.oh( 6, "<TR><TD class='FrameDescription' colspan=2>" + p + "</TD></TR>" );
    }

    /**
     * Generates the HTML for the end of a DESCRIPTION element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT DESCRIPTION EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startDESCRIPTION(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active DESCRIPTION element
     */
    public static void endDESCRIPTION( Node n )
    {
        // not useful because <!ELEMENT DESCRIPTION EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a EXAMPLES element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active EXAMPLES element
     */
    public static void startEXAMPLES( Node n )
    {
        Q.oh( 6, "<TR valign='top'>" );
        Q.oh( 7, "<TD width='100px'>&nbsp;&nbsp;&nbsp;<B>example</B></TD>" );
        Q.oh( 7, "<TD class='NoCaps'>" );
    }

    /**
     * Generates the HTML for the end of a EXAMPLES element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active EXAMPLES element
     */
    public static void endEXAMPLES( Node n )
    {
        Q.oh( 7, "</TD>" );
        Q.oh( 6, "</TR>" );
    }

    /**
     * Generates the HTML for the beginning of a EXAMPLE element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active EXAMPLE element
     */
    public static void startEXAMPLE( Node n )
    {
        NodeList kids = n.getChildNodes();

        for( int k = 0; k < kids.getLength(); k++ )
        {
            Node kid = kids.item( k );

            if( kid.getNodeName().equals( "#text" ) || kid.getNodeName().equals( "#cdata-section" ) )
            {
                String example = kid.getNodeValue();

                if( example.startsWith( "\"" ) && example.endsWith( "\"" ) ) {
                    eprintln( "ERROR: [" + curClass + "] EXAMPLE wrapped with quotes - invalid format." );
                } else if( example.indexOf( "\"" ) != -1 ) {
                    eprintln( "WARNING: [" + curClass + "] EXAMPLE contains double quote (\") - use single quote (') instead." );
                }

                Q.oh( 8, "\"" + example + "\"" );
            }
        }
    }

    /**
     * Generates the HTML for the end of a EXAMPLE element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT EXAMPLE (#PCDATA)></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startEXAMPLE(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active EXAMPLE element
     */
    public static void endEXAMPLE( Node n )
    {
        // not useful because <!ELEMENT EXAMPLE (#PCDATA)> (no children)
    }

    /**
     * Generates the HTML for the beginning of a SYNTAX element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNTAX element
     */
    public static void startSYNTAX( Node n )
    {
        Q.oh( 6, "<TR valign='middle'>" );
        Q.oh( 7, "<TD width='100px'>&nbsp;&nbsp;&nbsp;<B>syntax</B></TD>" );
        Q.oh( 7, "<TD>" );
        Q.oh( 8, false, "" );
    }

    /**
     * Generates the HTML for the end of a SYNTAX element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNTAX element
     */
    public static void endSYNTAX( Node n )
    {
        Q.oh( true, "" );
        Q.oh( 7, "</TD>" );
        Q.oh( 6, "</TR>" );
    }

    /**
     * Generates the HTML for the beginning of a SEMANTICS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SEMANTICS element
     */
    public static void startSEMANTICS( Node n )
    {
        Q.oh( 6, "<TR valign='top'>" );
        Q.oh( 7, "<TD width='100px'>&nbsp;&nbsp;&nbsp;<B>semantics</B></TD>" );
        Q.oh( 7, "<TD>" );
    }

    /**
     * Generates the HTML for the end of a SEMANTICS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SEMANTICS element
     */
    public static void endSEMANTICS( Node n )
    {
        Q.oh( 7, "</TD>" );
        Q.oh( 6, "</TR>" );
    }

    /**
     * Generates the HTML for the beginning of a PRED element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active PRED element
     */
    public static void startPRED( Node n )
    {
        String pr = ( ( Element ) n ).getAttribute( "value" );
        String bl = ( ( Element ) n ).getAttribute( "bool" );
        String extra = "";

        // Check if the predicate is negated, add appropriate HTML.
        if( bl.equals( "!" ) ) {
            extra = "<FONT class='Negated'>&#172;</FONT>";
        }
        else if (bl.equals( "?" ) ) {
        	extra = "<FONT class='Negated'>possible </FONT>";
        }

        Q.oh( 8, false, "<NOBR>" + extra + "<FONT class='Predicate'><a href=\"../predicates/" + pr + ".php\" class=PredLinks>" + pr + "</a>(</FONT>");

        Generator.refMap.addMembership( "Predicate", pr );
    }

    /**
     * Generates the HTML for the end of a PRED element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active PRED element
     */
    public static void endPRED( Node n )
    {
        String bl = ( ( Element ) n ).getAttribute( "bool" );
        String extra = "";

        // Check if the predicate is negated, add appropriate HTML.
/*        if( bl.equals( "!" ) ) {
            extra = "<FONT class='Negated'>)</FONT>";
        }
        else if (bl.equals( "?" ) ) {
        	extra = "<FONT class='Negated'>)</FONT>";
        }
*/
        Q.oh( true, "<FONT class='Predicate'>)" + extra + "</FONT></NOBR>");
    }

    /**
     * Generates the HTML for the beginning of a ARGS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ARGS element
     */
    public static void startARGS( Node n )
    {
        hasBeenOther = false;
    }

    /**
     * Generates the HTML for the end of a ARGS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ARGS element
     */
    public static void endARGS( Node n )
    {
        // No need to print any more HTML here.  Taken care of by endPRED.
    }

    /**
     * Generates the HTML for the beginning of a ARG element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ARG element
     */
    public static void startARG( Node n )
    {
        String cm = hasBeenOther ? ", " : "";
        String value = ( ( Element ) n ).getAttribute( "value");
        if (Pattern.matches(".*E[0-9].*", value)){
        	value = value.replaceAll("E([0-9])", "E<sub>$1</sub>");
        }
        if (value.startsWith("?")){
        	value = value.subSequence(1, value.length()).toString();
        }
        if (RoleValue.parseValue(value) != null)
        	Q.oh( cm + "<a href=\"../themroles/" + value + ".php\" class=VerbLinks>" + value + "</a>" );
        else
        	Q.oh( cm + value );
        	

        hasBeenOther = true;
    }

    /**
     * Generates the HTML for the end of a ARG element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT ARG EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startARG(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ARG element
     */
    public static void endARG( Node n )
    {
        // not useful because <!ELEMENT ARG EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a NP element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active NP element
     */
    public static void startNP( Node n )
    {
        String tr = ( ( Element ) n ).getAttribute( "value" );

        if (!tr.contains("?")){       
        	Q.oh( " <FONT class='ThemRole'><a href=\"../themroles/" + tr + ".php\" class=VerbLinks>" + tr + "</a></FONT> ");
        	Generator.refMap.addMembership( "NPThemRole", tr );
        }
        else{
        	Q.oh( " <FONT class='ThemRole'>?<a href=\"../themroles/" + tr.substring(1) + ".php\" class=VerbLinks>" + tr.substring(1) + "</a></FONT> ");        	
        }
    }

    /**
     * Generates the HTML for the end of a NP element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active NP element
     */
    public static void endNP( Node n )
    {
        // No need to print any more HTML here.  All NP SYNRESTRS/SELRESTRS
        // will have printed themselves before this point though.
    }

    /**
     * Generates the HTML for the beginning of a PREP element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active PREP element
     */
    public static void startPREP( Node n )
    {
        String v = ( ( Element ) n ).getAttribute( "value" ).toLowerCase();

        if( v.equals( "" ) )
        {
            int sr = numKids( n, "SELRESTRS" );

            if( sr != 0 ) {
                sr = numKids( ( ( Element ) n ).getElementsByTagName( "SELRESTRS" ).item( 0 ), "SELRESTR" );
            }

            if( sr == 0 ) {
                eprintln( "ERROR: [" + curClass + "] Empty PREP element, no 'value' nor 'SELRESTRS'." );
            }
        }
        else
        {
            if( v.endsWith( "spatial" )   || v.endsWith( "loc" ) || v.endsWith( "path" ) ||
                            v.endsWith( "dir" )       || v.endsWith( "src" ) || v.endsWith( "dest" ) ||
                            v.endsWith( "dest_conf" ) || v.endsWith( "dest_dir" ) )
            {
                eprintln( "WARNING: [" + curClass + "] Preposition class (" + v +
                ") specified in 'value' attribute, instead of SELRESTRS element." );
            }
        }

        Q.oh( " <FONT class='Preposition'>{" + v );
        prepSelRestrs = true;
    }

    /**
     * Generates the HTML for the end of a PREP element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active PREP element
     */
    public static void endPREP( Node n )
    {
        prepSelRestrs = false;

        Q.oh( "}</FONT> " );
    }

    /**
     * Generates the HTML for the beginning of a FEATURES element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FEATURES element
     */
    public static void startFEATURES( Node n )
    {
        if( hasKids( n, "FEATURE" ) )
        {
            //if( restrsLevel != 0 )            // If SYNRESTRS were recursively defined
            //   Q.oh( (usingOr)?" | ":" & " );

            Q.oh( "<FONT class='SynRestrs'><B>&lt;" );

        }

        hasBeenOther = false;

        //restrsLevel++;        // If SYNRESTRS were recursively defined
    }

    /**
     * Generates the HTML for the end of a FEATURES element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNRESTRS element
     */
    public static void endFEATURES( Node n )
    {
        if( hasKids( n, "FEATURE" ) ) {
            Q.oh( "&gt;</B></FONT> " );
        }

        //restrsLevel--;        // If SYNRESTRS were recursively defined
    }

    /**
     * Generates the HTML for the beginning of a FEATURE element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FEATURE element
     */
    public static void startFEATURE( Node n )
    {
        String sp = hasBeenOther ? " " : "";
        String ex = "";
        String sr = ( ( Element ) n ).getAttribute( "type" );

        if( hasBeenOther ) {
            ex = usingOr ? "| " : "& ";
        }

        Q.oh( sp + ex + ( ( Element ) n ).getAttribute( "Value" ) + sr );

        Generator.refMap.addMembership( "VerbFeatures", sr );

        hasBeenOther = true;
    }

    /**
     * Generates the HTML for the end of a FEATURE element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT FEATURE EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startFEATURE(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active FEATURE element
     */
    public static void endFEATURE( Node n )
    {
        // not useful because <!ELEMENT SYNRESTR EMPTY>
    }
    
    /**
     * Generates the HTML for the beginning of a SYNRESTRS element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNRESTRS element
     */
    public static void startSYNRESTRS( Node n )
    {
        if( hasKids( n, "SYNRESTR" ) )
        {
            //if( restrsLevel != 0 )            // If SYNRESTRS were recursively defined
            //   Q.oh( (usingOr)?" | ":" & " );

            Q.oh( "<FONT class='SynRestrs'><B>&lt;" );

            if( ( ( Element ) n ).getAttribute( "logic" ).equals( "and" ) ) {
                eprintln( "WARNING: [" + curClass + "] SYNRESTRS declares logic=\"and\" instead of leaving it out." );
            }

            if( ( ( Element ) n ).getAttribute( "logic" ).equals( "or" ) ) {
                usingOr = true;
            } else {
                usingOr = false;
            }
        }

        hasBeenOther = false;

        //restrsLevel++;        // If SYNRESTRS were recursively defined
    }

    /**
     * Generates the HTML for the end of a SYNRESTRS element.
     * Occurs after all child nodes' HTML has already been
     * output.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNRESTRS element
     */
    public static void endSYNRESTRS( Node n )
    {
        if( hasKids( n, "SYNRESTR" ) ) {
            Q.oh( "&gt;</B></FONT> " );
        }

        //restrsLevel--;        // If SYNRESTRS were recursively defined
    }

    /**
     * Generates the HTML for the beginning of a SYNRESTR element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNRESTR element
     */
    public static void startSYNRESTR( Node n )
    {
        String sp = hasBeenOther ? " " : "";
        String ex = "";
        String sr = ( ( Element ) n ).getAttribute( "type" );

        if( hasBeenOther ) {
            ex = usingOr ? "| " : "& ";
        }

        Q.oh( sp + ex + ( ( Element ) n ).getAttribute( "Value" ) + "<a href=\"../synrestrs/" + sr + ".php\">" + sr + "</a>" );

        Generator.refMap.addMembership( "SynRestr", sr );

        hasBeenOther = true;
    }

    /**
     * Generates the HTML for the end of a SYNRESTR element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT SYNRESTR EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startSYNRESTR(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active SYNRESTR element
     */
    public static void endSYNRESTR( Node n )
    {
        // not useful because <!ELEMENT SYNRESTR EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a VERB element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active VERB element
     */
    public static void startVERB( Node n )
    {
        Q.oh( " <FONT class='VerbV'>V</FONT> " );
    }

    /**
     * Generates the HTML for the end of a VERB element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT VERB EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startVERB(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active VERB element
     */
    public static void endVERB( Node n )
    {
        // not useful because <!ELEMENT VERB EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a ADJ element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ADJ element
     */
    public static void startADJ( Node n )
    {
        Q.oh( " ADJ " );
    }

    /**
     * Generates the HTML for the end of a ADJ element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT ADJ EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startADJ(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ADJ element
     */
    public static void endADJ( Node n )
    {
        // not useful because <!ELEMENT ADJ EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a ADV element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ADV element
     */
    public static void startADV( Node n )
    {
        Q.oh( " ADV " );
    }

    /**
     * Generates the HTML for the end of a ADV element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT ADV EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startADV(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active ADV element
     */
    public static void endADV( Node n )
    {
        // not useful because <!ELEMENT ADV EMPTY>
    }

    /**
     * Generates the HTML for the beginning of a LEX element.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active LEX element
     */
    public static void startLEX( Node n )
    {
        Q.oh( " <FONT class='LexicalItem'>(" + ( ( Element ) n ).getAttribute( "value" ) + ")</FONT> " );
    }

    /**
     * Generates the HTML for the end of a LEX element.
     * Although it is specified for completeness, this method does
     * not do anything useful.  This is because <CODE>&lt;!ELEMENT LEX EMPTY></CODE>
     * in the DTD, so any "closing HTML" could just be placed at the
     * end of the {@link uvi.Sweeper#startLEX(Node)} method.
     *
     * @param n the {@link org.w3c.dom.Node} object representing
     *        the active LEX element
     */
    public static void endLEX( Node n )
    {
        // not useful because <!ELEMENT LEX EMPTY>
    }
}
