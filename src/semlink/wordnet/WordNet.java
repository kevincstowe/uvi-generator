
///////////////////
// UVI Generator //
// Derek Trumbo  //
///////////////////

package semlink.wordnet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import semlink.util.ProgressManager;
import slflixer.cc.util.mm.MembershipMap;

/**
 * This class manages the extraction of the WordNet data for the goal
 * of showing sense numbers next to the verbs in the members section
 * of each class or subclass.  The file given to the UVIG is the
 * 'index.sense' file from the WordNet system.  This file however
 * is named 'wordnet.s' however, per the standards of the supplemental
 * files.  The index.sense file is very, very large.  Therefore,
 * the data structures in this class are designed in such a way
 * so as not to require a huge amount of processing time upon
 * each regeneration of the UVI.
 *
 * @see uvi.Sweeper#printMembers()
 * @see uvi.Generator#addOthers(int)
 *
 * @author Derek Trumbo
 */
public class WordNet
{

    ////////////
    // Fields //
    ////////////

    /**
     * The number of lists the sense keys are divided into.  There is
     * one for each letter of the alphabet (this is to speed sorting
     * and access times).
     */
    private static final int WN_SENSE_LISTS = 26;

    /**
     * Holds all the sense keys in VerbNet. This array of arrays contains
     * one array per letter of the English alphabet.  Each sense key will
     * be added to the list that corresponds to the letter with which it
     * begins.  Additionally each list will also be held in sorted order.
     * Here's an example of what this data structure might hold:
     *
     * <TABLE cellpadding=5>
     * <TR><TD align='center'>A</TD><TD align='center'>B</TD><TD align='center'>C</TD>
     * <TD align='center'>...</TD></TR>
     * <TR><TD>
     * abandon%2:38:00<BR>abandon%2:40:00<BR>abash%2:37:00<BR>
     * abate%2:30:00<BR>abate%2:30:01<BR>abduct%2:35:02<BR>...
     * </TD><TD>
     * baa%2:32:00<BR>babble%2:32:00<BR>babble%2:32:02<BR>
     * babble%2:39:00<BR>backbite%2:32:00<BR>backpack%2:38:00<BR>...
     * </TD><TD>
     * cabbage%2:40:00<BR>cable%2:32:00<BR>cackle%2:29:00<BR>
     * cackle%2:32:00<BR>cackle%2:32:01<BR>caddy%2:33:00<BR>...
     * </TD><TD>&nbsp;</TD></TR>
     * </TABLE>
     */
    public static ArrayList[] allSenseLists = new ArrayList[ WN_SENSE_LISTS ];

    public static int getKeyCount() {
        int count = 0;
        for(ArrayList list : allSenseLists) {
            count += list.size();
        }
        return count;
    }

    static
    {

        // Instantiate each array.
        for( int i = 0; i < WN_SENSE_LISTS; i++ ) {
            allSenseLists[ i ] = new ArrayList();
        }
    }

    private static MembershipMap<String, String> wnsnParseErrors = new MembershipMap<String, String>();
    public static MembershipMap<String, String> getWNSNParseErrors() {
        return wnsnParseErrors;
    }
    private static MembershipMap<String, String> nonExistentWNSNErrors = new MembershipMap<String, String>();
    public static MembershipMap<String, String> getNonExistentWNSNErrors() {
        return nonExistentWNSNErrors;
    }
    private static MembershipMap<String, String> allVerbs = new MembershipMap<String, String>();
    public static MembershipMap<String, String> getAllVerbs() {
        return allVerbs;
    }

    public static int numberUncertain = 0;
    public static int totalVnWNSK = 0;
    public static int totalIndexSenseWNSK = 0;

    /////////////////
    // Constructor //
    /////////////////

    /**
     * This constructor is private because the class is not intended to ever
     * be instantiated.  The UVI generation is a very procedural process and
     * thus all the members are static.
     */
    private WordNet() {}

    /////////////
    // Methods //
    /////////////

    /**
     * Scans through all the VerbNet XML files and extract just the WordNet
     * sense keys.  These are added to the per-letter array of arrays in
     * sorted order (the array for each letter is sorted).  The reason for
     * this is that the index.sense file itself is in sorted order and this
     * will allow us to perform just a single pass over this 200,000 line file.
     *
     * @param xmlFiles a list of all the XML files in the XML input directory.
     *        This is received from the {@link uvi.Generator} class.
     * @see uvi.Generator#addOthers(int)
     * @see uvi.WordNet#allSenseLists
     */
    public static void preScan( File[] xmlFiles )
    {

        try
        {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // Don't perform any validation on this pre-scan.  The full-blown
            // validation will come during the primary scanning phase.
            dbf.setValidating( false );

            DocumentBuilder db = dbf.newDocumentBuilder();

            // Print the start of the progress bar.
            System.out.print( "         ");
            ProgressManager.reset();

            // Extract the sense keys from all the XML files.
            for( int x = 0; x < xmlFiles.length; x++ )
            {

                ProgressManager.next(x, xmlFiles.length);

                Document doc = db.parse( xmlFiles[ x ] );

                // Get a list of all MEMBER elements in this file.
                NodeList members = doc.getElementsByTagName( "MEMBER" );

                // Extract the sense keys from each member in this file.
                for( int m = 0; m < members.getLength(); m++ )
                {
                    String vb = ( ( Element ) members.item( m ) ).getAttribute( "name" );
                    String wn = ( ( Element ) members.item( m ) ).getAttribute( "wn" );

                    // Skip this member if it has no WordNet sense keys.
                    if( wn.equals( "" ) ) {
                        continue;
                    }

                    // Create an array whose elements are all the WordNet senses
                    // for this member.  Lower case here for extra robustness.
                    String[] thisVerbsSenses = wn.toLowerCase().split( "  *" );

                    // Add each sense key to the list.
                    for( int s = 0; s < thisVerbsSenses.length; s++ )
                    {
                        totalVnWNSK++;
                        String sense = thisVerbsSenses[ s ];
                        boolean exists = false;
                        int i;

                        // Ignore any question marks preceding the sense key.
                        if( sense.charAt( 0 ) == '?' ) {
                            sense = sense.substring( 1 );
                            numberUncertain++;
                        }

                        if( !sense.matches( "[a-z_-]+%[0-9]:[0-9][0-9]:[0-9][0-9]" ) )
                        {
                            wnsnParseErrors.addMembership(sense, xmlFiles[ x ].getName());
                            continue;
                        }

                        // Select the correct list for this sense key ('a'==97).
                        ArrayList theList = allSenseLists[ sense.charAt( 0 ) - 97 ];

                        // Find the spot in which to insert the key alphabetically.
                        for( i = theList.size(); i > 0; i-- )
                        {
                            WordNetSense wns = ( WordNetSense ) theList.get( i - 1 );

                            int comparison = wns.key.compareTo( sense );

                            // If the one in the list is "less than" the one being
                            // added, then this is the correct location for insertion.
                            if( comparison < 0 ) {
                                break;
                            } else if( comparison == 0 )
                            {
                                exists = true;

                                // Add this file to the key's file list.
                                wns.files += ", " + xmlFiles[ x ].getName();

                                break;
                            }
                        }

                        // If the key didn't already exist in the list, add it.
                        if( !exists ) {
                            theList.add( i, new WordNetSense( sense, xmlFiles[ x ].getName() ) );
                        }
                    }
                }
            }

            ProgressManager.finish();
        }

        // Show any errors that occur.
        catch( Exception e )
        {
            System.err.println( "ERROR: Problem with the WordNet pre-scan.  " + e.getMessage() );
        }
    }

    /**
     * Searches WordNet's sense file for sense numbers that correspond
     * to the sense keys found during the VerbNet pre-scan.  Each
     * {@link uvi.WordNet.WordNetSense} object in the {@link uvi.WordNet#allSenseLists}
     * data structure will have its sense number filled in if the object's
     * sense key exists in the file.  The {@link uvi.WordNet#allSenseLists}
     * data structure is in sorted order and the index.sense file is in sorted
     * order so this allows this method to perform just one pass over this
     * 200,000+ line file.
     *
     * @param senseIndexFile the {@link java.io.File} object corresponding to the
     *        WordNet index.sense file (named wordnet.s in the UVIG system)
     */
    public static void loadSenseNumbers( File senseIndexFile )
    {
        try
        {
            BufferedReader senseIndex = new BufferedReader( new FileReader( senseIndexFile ) );
            String line;

            int list = 0;
            int wnsx = 0;        // Entry in current list that we are
            // examining (need a sense number for).

            System.out.print( "         ");
            ProgressManager.reset();

            // Grab the first object whose sense key is to be looked for
            // (this is the first object in the 'a' list).
            WordNetSense wns = ( WordNetSense ) allSenseLists[ list ].get( wnsx );

            // Process each line in the file, checking to see if it
            // corresponds to a key that is required by the VerbNet system.
            while( ( line = senseIndex.readLine() ) != null )
            {
                totalIndexSenseWNSK++;
                if(line.indexOf("%2") != -1) {
                    allVerbs.addMembership(line.substring(0, line.indexOf("%2")), "A");
                }

                ProgressManager.next(list, WN_SENSE_LISTS);

                if(list == WN_SENSE_LISTS) {
                    continue;
                }

                // Flag used to continue to compare successive VerbNet sense keys
                // to the current sense key for this line of the file.
                boolean keepGoing = true;

                while( keepGoing )
                {

                    // If the current line from the file corresponds to the current
                    // sense key being looked for (a.k.a. we have a match)...
                    if( line.startsWith( wns.key ) )
                    {

                        // Locate the pertinent spaces.
                        int lastSpace = line.lastIndexOf( " " );
                        int nextLastSpace = line.lastIndexOf( " ", lastSpace - 1 );

                        // Extract the sense number.
                        wns.num = line.substring( nextLastSpace + 1, lastSpace );

                        // Move to the next sense key for which we need to find a sense number.
                        wnsx++;

                        // If we've reached the bottom of the list, move to the next list.
                        if( wnsx == allSenseLists[ list ].size() )
                        {
                            wnsx = 0;      // First element in next list.

                            list++;        // Move to next list.

                            // Skip any empty lists (like 'X').
                            while( list < allSenseLists.length && allSenseLists[ list ].size() == 0 ) {
                                list++;
                            }

                            // If we've run out of lists to process, then discontinue processing.
                            if( list == allSenseLists.length )
                            {
                                break;
                            }
                        }

                        // Grab the next object that needs a sense number.
                        wns = ( WordNetSense ) allSenseLists[ list ].get( wnsx );

                        // Don't keep examining this line, move to the next one.
                        keepGoing = false;
                    }

                    // Else if the current line is not what we are looking for,
                    // compare it to the one we are looking for and decide
                    // how to act.
                    else
                    {
                        int comparison = line.compareTo( wns.key );

                        // If the line in the file is lexicographically "larger"
                        // than the one we are looking for, then we did not find
                        // the one we are looking for in the file.
                        if( comparison > 0 )
                        {

                            nonExistentWNSNErrors.addMembership(wns.key, wns.files);

                            // Show a warning.
                            //                     System.err.println( "WARNING: [" + wns.files + "] WordNet sense key not found in wordnet.s: \"" +
                            //                        wns.key + "\"" );

                            // Move to the next sense key for which we need to find a sense number.
                            wnsx++;

                            // If we've reached the bottom of the list, move to the next list.
                            if( wnsx == allSenseLists[ list ].size() )
                            {
                                wnsx = 0;      // First element in next list.

                                list++;        // Move to next list.

                                // Skip any empty lists (like 'X').
                                while( list < allSenseLists.length && allSenseLists[ list ].size() == 0 ) {
                                    list++;
                                }

                                // If we've run out of lists to process, then discontinue processing.
                                if( list == allSenseLists.length )
                                {
                                    break;
                                }
                            }

                            // Grab the next object that needs a sense number.
                            wns = ( WordNetSense ) allSenseLists[ list ].get( wnsx );

                            // (keepGoing remains true)
                        } else {
                            keepGoing = false;
                        }
                    }
                }
            }

            ProgressManager.finish();

            senseIndex.close();
        }
        catch( Exception e )
        {
            System.err.println( "ERROR: Problem with the WordNet sense number retrieval.  " + e.getMessage() );
        }
    }

    /**
     * Returns the WordNet sense number associated with a given
     * WordNet sense key.  Searches the {@link uvi.WordNet#allSenseLists}
     * data structure looking for the appropriate {@link uvi.WordNet.WordNetSense}
     * object.  Once found, the sense number it contains is returned.
     *
     * @param key the sense key for which a sense number should be retrieved
     * @return the sense number corresponding to the given sense key, or <CODE>?</CODE>
     *         if the sense key does not exist in the lists
     * @see uvi.Sweeper#printMembers()
     */
    public static String getSenseNumber( String key )
    {

        // Ignore any question marks preceding the sense key.
        if( key.charAt( 0 ) == '?' ) {
            key = key.substring( 1 );
        }

        if( !key.matches( "[a-z_-]+%[0-9]:[0-9][0-9]:[0-9][0-9]" ) )
        {
            //  System.err.println( "ERROR: The sense key \"" + key + "\" is invalid." );
            return "?";
        }

        // Select the correct list for this sense key ('a'==97).
        ArrayList theList = allSenseLists[ key.charAt( 0 ) - 97 ];

        // Perform a linear search of the chosen list.
        for( int s = 0; s < theList.size(); s++ )
        {
            WordNetSense wns = ( WordNetSense ) theList.get( s );

            // If the desired sense key matches the one in the list
            // return the sense number associated with it.
            if( key.equals( wns.key ) ) {
                return wns.num;
            }
        }

        // Execution will only reach this statement for those keys not
        // found during the pre-scan phase.  This is because all other
        // keys are guaranteed to be in the list since the pre-scan
        // scans the same sense keys as the primary scan.
        return "?";
    }

    ///////////////////
    // Inner Classes //
    ///////////////////

    /**
     * Represents a WordNet sense by holding both the sense key
     * and the sense number of a given sense.
     */
    public static class WordNetSense
    {

        /**
         * Constructs a WordNetSense object with the given sense key.
         * The sense number is initialized to a question mark (?).
         * The sense number will be filled in by {@link uvi.WordNet#loadSenseNumbers(File)}
         * if it exists in the index.sense (wordnet.s) file.
         *
         * @param k the sense key
         * @param f the file that the key is from
         */
        WordNetSense( String k, String f )
        {
            key = k;
            num = "?";
            files = f;
        }

        /**
         * The sense key of this sense.
         *
         * @see uvi.WordNet#preScan(File[])
         * @see uvi.WordNet#loadSenseNumbers(File)
         * @see uvi.WordNet#getSenseNumber(String)
         */
        String key;

        /**
         * The sense number of this sense.
         *
         * @see uvi.WordNet#loadSenseNumbers(File)
         * @see uvi.WordNet#getSenseNumber(String)
         */
        String num;

        /**
         * A comma-delimited list of all the files that
         * use this key.
         *
         * @see uvi.WordNet#preScan(File[])
         * @see uvi.WordNet#loadSenseNumbers(File)
         */
        String files;
    }
}
