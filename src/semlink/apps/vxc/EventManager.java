
////////////////////////////
// VxC: A VN-Cyc Mapper   //
// Built on the Inspector //
// University of Colorado //
// Fall 2006              //
////////////////////////////

package semlink.apps.vxc;

import org.w3c.dom.*;
import java.io.*;

/**
 * This class invokes methods from the {@link vn.Cyc} class as certain Inspector events are
 * fired, essentially implementing the VxC application.  The approach VxC takes to extending
 * the Inspector is to put as little code into the {@link vn.EventManager} class as possible,
 * and utilize external classes for all additional required code ({@link vn.Cyc}, {@link vn.Matcher},
 * and {@link vn.PrepositionManager} in this case).<BR><BR>
 * At the beginning of the program
 * the {@link vn.Cyc} class is asked to initialize itself and read in both the Cyc
 * <CODE>verbSemTrans</CODE> rules as well as the manual mapping file.  At the start
 * of each class or subclass the event tells the {@link vn.Cyc} class what the current
 * class being processed is.  The class depth is also kept track of with {@link
 * vn.EventManager#classLevel}.  This is only used to implement the print class match
 * counts feature ({@link vn.Cyc#flShowClassMatchCounts}, -Sc on command-line).  At the
 * end of the program the {@link vn.Cyc} class is called upon to close any external
 * files it needs to and to print the final statistics of the automatic matching.
 * <BR><BR>For VxC to function correctly, the Inspector must be running in verb-frame
 * mode.  Therefore, the -q option is mandatory for each execution (-Vq also works
 * but no normal output is required to be printed).
 * <BR><BR>That said, the only other real processing that takes place in this class
 * happens at the end of each verb-frame pair.  At this point, the syntax and semantics for the
 * verb-frame pair has been visited and saved to {@link vn.EventManager#syn} and {@link vn.EventManager#sem} and
 * all remaining required information is stored in the <CODE>text</CODE> parameter
 * of the method.  The {@link vn.Cyc} class is called upon look for any matches
 * present for the given VerbNet verb-frame pair.
 * <BR><BR>
 * See the EVENT_PROGRAM, EVENT_CLASS, EVENT_SUBCLASS, EVENT_SYNTAX, EVENT_SEMANTIC_PRED, EVENT_VF_PAIR,
 * and EVENT_END_FRAMES events.
 *
 * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
 *
 * @author Derek Trumbo
 * @version 1.0, 2006.10.25
 */
public class EventManager
{

   /////////////////////////////////////////////////////////////////////////////
   // Researcher Fields - Can add needed class-level fields that will support //
   // fireEvent code right here.  This is important because if you want to    //
   // write your code in this class instead of an external class via method   //
   // calls, then you will need a mechanism to save information between calls //
   // of fireEvent.  The simple solution is to use static variables and       //
   // classes.  So for example, if you are extracting all verbs which         //
   // participate in frames that have an adverb one method is to run          //
   //   java vn.Inspector xml/ -q                                             //
   // and set the below example variable, verb, to the verb in the vf-pair    //
   // label when fireEvent calls on 'start' VF-PAIR and then when the 'start' //
   // SYNTAX event fires, the variable verb is waiting to be used however if  //
   // the syntax matches the rule.  One can utilize a static initializer      //
   // block to execute code even before the 'start' event for PROGRAM.        //
   /////////////////////////////////////////////////////////////////////////////

   /**
    * The syntax of the given verb-frame pair.  This is essentially just a temporary
    * storage location for the syntax of a verb-frame pair since the syntax is needed
    * when each verb-frame pair is processed at the end of each pair.
    * See EVENT_SYNTAX and EVENT_VF_PAIR.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   private static String syn;

   /**
    * The semantics of the given verb-frame pair.  This is essentially just a temporary
    * storage location for the semantics of a verb-frame pair since the semantics is needed
    * when each verb-frame pair is processed at the end of each pair.  This string
    * is the conglomeration of all semantic predicates in one.
    * See EVENT_SEMANTIC_PRED and EVENT_VF_PAIR.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   private static String sem;

   /**
    * Stores the depth of the current class or subclass.  The main class in a file
    * is level 0.  This is used only to implement the print class match
    * counts feature ({@link vn.Cyc#flShowClassMatchCounts}, -Sc on command-line).
    * See the EVENT_CLASS, EVENT_SUBCLASS, and EVENT_END_FRAMES events.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   private static int classLevel;

   // Static initializer block can be used for any required initialization code.
   //static
   //{
   //   syn = "";
   //}

   ///////////////////////////////////////////////
   // Researcher Method - Extend Inspector Here //
   ///////////////////////////////////////////////

   /**
    * Captures events from the Inspector scanning system and passes relevant information
    * from the XML files onto the {@link vn.Cyc} class so that it can look for possible
    * matches between the VerbNet and Cyc.  For VxC to function it must be running in
    * verb-frame pair mode.  This means that the member and frame events do not fire,
    * but rather the verb-frame pair events fire.  Upon visiting each verb-frame pair,
    * the verb, syntax, semantics and frame information are sent to the {@link vn.Cyc} class to be
    * compared to all the relevant Cyc rules.  When this method captures the 'end'
    * of program event, it asks the {@link vn.Cyc} class to print its results.
    *
    * @param type the type of element to which this event corresponds
    *        ({@link vn.EventManager#EVENT_PROGRAM}, {@link vn.EventManager#EVENT_FILE}, etc.)
    * @param startOrEnd whether this event is for the beginning of the
    *        element or the end of the element
    *        ({@link vn.EventManager#EVENT_START} or {@link vn.EventManager#EVENT_END})
    * @param curFile the name of the file currently being processed (i.e. approve-77.xml).  This argument is
    *        <CODE>null</CODE> for the PROGRAM element.
    * @param curClass the name of the class or subclass currently being processed (i.e. devour-39.4-1).
    *        This argument is <CODE>null</CODE> for the PROGRAM and FILE elements.
    * @param text the simple-text representation of the element to which this
    *        event corresponds.  This text is equivalent to the text that would be
    *        printed for this element had it been printed by the Inspector under the
    *        view options -Vab (full output).  For the PROGRAM element, this argument is
    *        equal to the concatenation of all the command line elements with spaces.
    * @param n the XML node corresponding to the element being visited.  This argument is
    *        <CODE>null</CODE> for the PROGRAM and FILE elements.  For verb-frame pairs,
    *        this is the XML node corresponding to the MEMBER element in the verb-frame
    *        pair.
    * @param nExtra the XML node corresponding to the FRAME element in the verb-frame
    *        pair being visited.  This argument is <CODE>null</CODE> for all other elements.
    * @see vn.Cyc#initialize()
    * @see vn.Cyc#loadExternalData()
    * @see vn.Cyc#closeExternalData()
    * @see vn.Cyc#compareMatches()
    * @see vn.Cyc#setClass(String)
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.Cyc#printClassMatchCount(int)
    */
   static void fireEvent( int type, int startOrEnd, String curFile, String curClass,
                          String text, Element n, Element nExtra )
   {
      switch( type )
      {

         // Text parameter for 'start' and 'end' events for program is the original
         // command line without the java command and class name used to execute the
         // program (just the program options).  These are separated by spaces.
         case EVENT_PROGRAM:

            // 'Start' event for program fires if there were no problems with the command
            // line arguments and if neither -? nor -k was specified.  This event fires
            // before the header is printed.
            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF PROGRAM            ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Show an error message and stop the program if VxC
               // is not running in verb-frame pair mode.
               if( !Inspector.flVFPairs )
               {
                  System.err.println( "ERROR: VxC must run in verb-frame pair mode.  Use -q or -Vq." );
                  System.exit( 0 );
               }

               // Initialize various data structures.
               Cyc.initialize();

               // Load Cyc rules and manual mapping file.
               Cyc.loadExternalData();
            }

            // 'End' event for program fires if and only if the 'start' event fired.
            // This event fires after the final counts have been printed.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF PROGRAM                  ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Finalize and close the match output file.
               Cyc.closeExternalData();

               // Compare the matches that the automatic matching against those
               // from the manual mapping file and display results to stdout.
               Cyc.compareMatches();
            }
            break;

         case EVENT_FILE:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF FILE               ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }

            // 'End' event for file fires after all the events for the main class
            // in the file have fired.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF FILE                     ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         case EVENT_CLASS:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF MAIN CLASS         ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Give the mapping system the name of the new class being entered.
               // This will also check whether the new class was included in the
               // manual mapping file.
               Cyc.setClass( n.getAttribute( "ID" ) );

               // Reset the class level to zero for this main class.
               classLevel = 0;
            }

            // 'End' event for class fires after events for the class's
            // members, thematic roles, frames, verb-frame pairs (if applicable),
            // and subclasses have all fired.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF MAIN CLASS               ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         case EVENT_SUBCLASS:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF SUBCLASS           ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Give the mapping system the name of the new class being entered.
               // This will also check whether the new class was included in the
               // manual mapping file.
               Cyc.setClass( n.getAttribute( "ID" ) );

               // Increase the current depth of classes.
               classLevel++;
            }

            // 'End' event for subclass fires after events for the subclass's
            // members, thematic roles, frames, verb-frame pairs (if applicable),
            // and subclasses have all fired.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF SUBCLASS                 ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Decrease the current depth of classes.
               classLevel--;
            }
            break;

         case EVENT_MEMBER:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF MEMBER             ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }

            // 'End' event for member fires immediately after 'start' event.  Only
            // fires at all for completeness.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF MEMBER                   ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         case EVENT_THEMROLE:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF THEMATIC ROLE      ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }

            // 'End' event for thematic role fires immediately after 'start' event.
            // Only fires at all for completeness.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF THEMATIC ROLE            ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         case EVENT_FRAME:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF FRAME              ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }

            // 'End' event for frame fires after events for the frame's
            // examples, syntax, and semantic predicates have fired.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF FRAME                    ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         // Almost 100% of frames have exactly 1 example.  A few have 2 or 3 examples.
         case EVENT_EXAMPLE:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF EXAMPLE            ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }

            // 'End' event for example fires immediately after 'start' event.  Only
            // fires at all for completeness.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF EXAMPLE                  ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         case EVENT_SYNTAX:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF SYNTAX             ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Save the syntax of this SYNTAX element for use in the verb-frame
               // pair event.  This also strips off the "SYNTAX: " token.
               syn = text.substring( 8 );
            }

            // 'End' event for syntax fires immediately after 'start' event.  Only
            // fires at all for completeness.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF SYNTAX                   ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         // Events do not fire for the SEMANTICS section as a whole, only
         // for individual predicates.
         case EVENT_SEMANTIC_PRED:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF SEMANTIC PRED      ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/

               // Append this
               // pair event.  This also strips off the "SEMANTIC PRED: " token.
               sem += text.substring( 15 ) + " ";
            }

            // 'End' event for semantic predicate fires immediately after 'start'
            // event.  Only fires at all for completeness.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF SEMANTIC PRED            ***
                *** Remember: curFile, curClass, text, and n are available     ***
                ******************************************************************/
            }
            break;

         // Events do not fire for individual members or frames when running
         // in verb-frame pair mode.
         case EVENT_VF_PAIR:

            if( startOrEnd == EVENT_START )
            {
               /******************************************************************
                *** Insert your code here for: BEGINNING OF VERB-FRAME PAIR    ***
                *** Remember: curFile, curClass, text, n, and nExtra           ***
                *** are available                                              ***
                ******************************************************************/

               // Reset the semantics string for this verb-frame pair (clear out
               // the semantic predicate information from the previous verb-frame
               // pair).
               sem = "";
            }

            // 'End' event for verb-frame pair fires after events for the verb-frame
            // pair's examples, syntax, and semantic predicates have fired.
            else
            {
               /******************************************************************
                *** Insert your code here for: END OF VERB-FRAME PAIR          ***
                *** Remember: curFile, curClass, text, n, and nExtra           ***
                *** are available                                              ***
                ******************************************************************/

               // Isolate the delimiters of the frame description (primary // secondary).
               int l = text.indexOf( "[" );
               int r = text.lastIndexOf( "]" );

               // Extract the full description from the event text and the verb
               // from one of the Element objects given.
               String fDesc = text.substring( l + 1, r );
               String verb  = n.getAttribute( "name" );

               // Hand all the information off to the Cyc mapping system to find
               // possible matches between this verb-frame pair and the Cyc rules.
               Cyc.findCycMatches( verb, syn, sem, Sweeper.curVFFrameNum, fDesc );
            }
            break;

         // This event fires when the FRAMES element closes in the XML file (a.k.a.
         // when the </FRAMES> closing tag  is reached).  Although this is not
         // synonymous with the end of a class, as each class contains subclasses
         // inside it, it does basically represent the end
         // of the content area for a class (members, thematic roles, and frames).
         // Another way to conceptualize this is that this event fires right
         // before the beginning of each new class and subclass, and once at the end
         // of the scanning.  This event is necessary if you want to create
         // a hierarchically-correct output.  The 'text' parameter is a zero-length
         // string and the 'n' parameter represents the FRAMES element.
         case EVENT_END_FRAMES:

            // Only an 'end' event is fired for this event.
            if( startOrEnd == EVENT_END )
            {
               /******************************************************************
                *** Insert your code here for: END OF FRAMES ELEMENT           ***
                *** Remember: curFile, curClass, and n are available           ***
                ******************************************************************/

               // Print the number of matches that this class or subclass alone had.
               Cyc.printClassMatchCount( classLevel );
            }
            break;
      }
   }

   ////////////
   // Fields //
   ////////////

   /**
    * Identifies that the event firing corresponds to the beginning of the element in question.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_START = 0;

   /**
    * Identifies that the event firing corresponds to the end of the element in question.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_END = 1;

   /**
    * Identifies that the event is for the entire program.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_PROGRAM = 0;

   /**
    * Identifies that the event is for a single XML file.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_FILE = 1;

   /**
    * Identifies that the event is for a main class.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_CLASS = 2;

   /**
    * Identifies that the event is for a subclass.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_SUBCLASS = 3;

   /**
    * Identifies that the event is for a single member (verb).
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_MEMBER = 4;

   /**
    * Identifies that the event is for a thematic role.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_THEMROLE = 5;

   /**
    * Identifies that the event is for a frame.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_FRAME = 6;

   /**
    * Identifies that the event is for an example.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_EXAMPLE = 7;

   /**
    * Identifies that the event is for syntax.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_SYNTAX = 8;

   /**
    * Identifies that the event is for a single semantic predicate.  There are no
    * events that fire for the SEMANTICS element as a whole, only one for each
    * semantic predicate.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_SEMANTIC_PRED = 9;

   /**
    * Identifies that the event is for a verb-frame pair.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_VF_PAIR = 10;

   /**
    * Identifies the moment right before another class begins.  This is synonymous with
    * &lt;/FRAMES> but not synonymous with end of class.  So a special event is needed.
    * This event only has an 'end' event.
    *
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    */
   static final int EVENT_END_FRAMES = 11;

   /**
    * The original stdout stream reference from the {@link java.lang.System} class.
    * This is saved in the static initializer for this class and exists just in case
    * the stdout stream is reset to a custom file in the
    * {@link vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)}.
    * A researcher can use this reference to reset the {@link java.lang.System} class's stdout
    * stream back to the original stream whenever desired.
    *
    * @see java.lang.System#out
    * @see java.lang.System#setOut(PrintStream)
    */
   private static PrintStream originalStdOut;

   /**
    * Save the original stdout stream reference in case it is reset to a
    * custom file in the fireEvent method.
    */
   static
   {
      originalStdOut = System.out;
   }

   /////////////////
   // Constructor //
   /////////////////

   /**
    * This constructor is private because the class is not intended to ever
    * be instantiated.  The Inspector is a very procedural process and
    * thus all the members are static.
    */
   private EventManager() {}

   /////////////////////////////////////////////////////
   // Inspector Implementation Method - Do Not Modify //
   /////////////////////////////////////////////////////

   /**
    * Fires an event if events are enabled for this execution of the Inspector.
    * This method exists only to implement the "suppress all events" option (-e)
    * of the Inspector.  Events are enabled by default.  This option must be
    * supplied to prevent them from firing.  If the events are not being
    * suppressed, this method simply hands the arguments given to it to the
    * real <CODE>fireEvent</CODE> method.  It also makes a quick conversion between
    * {@link org.w3c.dom.Node}s and {@link org.w3c.dom.Element}s for code simplification
    * purposes only.  One error message has been added to this method for the
    * VxC extension of the Inspector.  Do not modify this method.
    *
    * @param type the type of element to which this event corresponds
    *        ({@link vn.EventManager#EVENT_PROGRAM}, {@link vn.EventManager#EVENT_FILE}, etc.)
    * @param startOrEnd whether this event is for the beginning of the
    *        element or the end of the element
    *        ({@link vn.EventManager#EVENT_START} or {@link vn.EventManager#EVENT_END})
    * @param curFile the name of the file currently being processed (i.e. approve-77.xml).  This argument is
    *        <CODE>null</CODE> for the PROGRAM element.
    * @param curClass the name of the class or subclass currently being processed (i.e. devour-39.4-1).
    *        This argument is <CODE>null</CODE> for the PROGRAM and FILE elements.
    * @param text the simple-text representation of the element to which this
    *        event corresponds.  This text is equivalent to the text that would be
    *        printed for this element had it been printed by the Inspector under the
    *        view options -Vab (full output).  For the PROGRAM element, this argument is
    *        equal to the concatenation of all the command line elements with spaces.
    * @param n the XML node corresponding to the element being visited.  This argument is
    *        <CODE>null</CODE> for the PROGRAM and FILE elements.  For verb-frame pairs,
    *        this is the XML node corresponding to the MEMBER element in the verb-frame
    *        pair.
    * @param nExtra the XML node corresponding to the FRAME element in the verb-frame
    *        pair being visited.  This argument is <CODE>null</CODE> for all other elements.
    * @see vn.EventManager#fireEvent(int, int, String, String, String, Element, Element)
    * @see vn.Inspector#flSuppressEvents
    */
   static void fireEventMaybe( int type, int startOrEnd, String curFile, String curClass,
                               String text, Node n, Node nExtra )
   {
      if( !Inspector.flSuppressEvents )
         fireEvent( type, startOrEnd, curFile, curClass, text, ( Element ) n, ( Element ) nExtra );

      else
         System.err.println( "ERROR: Inspector suppressing events.  -e option not allowed with VxC." );
   }
}
