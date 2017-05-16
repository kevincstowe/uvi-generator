
///////////////////
// UVI Generator //
// Derek Trumbo  //
///////////////////

package semlink.apps.uvig;

import java.util.ArrayList;

/**
 * This class contains all the verbs encountered during the parsing process
 * and the links associated with each verb.  This class accumulates verbs and links
 * first during the XML parsing process and secondly during the PropBank and
 * FrameNet loading.  The VerbNet verbs are added when a MEMBER tag is located in
 * an XML file.  Once it contains all the links, the {@link uvi.Generator}
 * asks this class to sort each list of verbs (a list is a single letter, like A).
 * Finally, this class is the integral part in all of the index page creation.
 * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
 * When this documentation was created, only *.html files were used.  Later, they were
 * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
 *
 * @see uvi.Generator#generateHTMLFiles()
 * @see uvi.Generator#generateIndexFiles()
 * @see uvi.Generator#addOthers(int)
 * @see uvi.Sweeper#startMEMBER(Node)
 *
 * @author Derek Trumbo
 */
public class Index
{

    ////////////
    // Fields //
    ////////////

    /**
     * The number of letters in the alphabet.
     */
    static int LETTER_MAX = 26;

    /**
     * An array which can hold one {@link java.util.ArrayList} for each letter of the alphabet.
     * Each index element begins as <CODE>null</CODE>.  Once the first verb for
     * that element's corresponding letter (i.e. C: index[2]) has been
     * encountered, the element is assigned to an instantiated {@link java.util.ArrayList}.
     * That {@link java.util.ArrayList} will hold {@link uvi.Index.Entry} objects.  Each
     * {@link uvi.Index.Entry} object corresponds to a single English verb.
     */
    static ArrayList[] index = new ArrayList[ LETTER_MAX ];

    /////////////////
    // Constructor //
    /////////////////

    /**
     * This constructor is private because the class is not intended to ever
     * be instantiated.  The UVI generation is a very procedural process and
     * thus all the members are static.
     */
    private Index() {}

    /////////////
    // Methods //
    /////////////

    /**
     * Registers a verb and one link with the index.  If this is the first verb
     * with the given first-letter, a new {@link java.util.ArrayList} is created
     * and assigned to the proper <CODE>index</CODE> array element.  If the
     * corresponding list is created, but the verb does not exist, the verb
     * and its link is added to the list.  If the verb does exist in the list
     * for that first-letter already, then the link is simply added to the verb's
     * array of links (see {@link uvi.Index.Entry#links}).
     *
     * @param newVerb the verb found in the VerbNet, PropBank, or FrameNet sources
     * @param newType one of the values Generator.DS_*.  This is used to identify
     *        the source of this verb/link pair.
     * @param newText the text to use in the index for this link
     * @param newLink the <CODE>href</CODE> to use in the index for this link
     * @see uvi.Generator#addOthers(int)
     * @see uvi.Sweeper#startMEMBER(Node)
     * @see uvi.Index.Entry
     * @see uvi.Index.Link
     */
    static void addLink( String newVerb, int newType, String newText, String newLink )
    {
        newVerb = newVerb.toLowerCase();

        // Transform a lower-case letter into an integer on [0,25].
        int letter = newVerb.charAt( 0 ) - 97;

        // If the first letter is not a valid lower-case English letter...
        if( letter < 0 || letter >= LETTER_MAX )
        {

            // If the first character is a question mark, then ignore the
            // question mark and show a warning message to that effect.
            if( newVerb.toLowerCase().charAt( 0 ) == '?' )
            {

                // Remove the question mark.
                newVerb = newVerb.substring( 1 );

                // Get new index position.
                letter = newVerb.charAt( 0 ) - 97;

                // Show warning message.
                System.err.println( "WARNING: Verb '?" + newVerb + "' going to be added with link '?" + newText + "'." );

                // The link text will still reflect the fact that the verb
                // contained a question mark.
                newText = "?" + newText;
            }

            // The character is not valid, show error message and exit.
            else
            {
                System.err.println( "ERROR: Unknown initial letter for '" + newVerb + "'." );
                return;
            }
        }

        // Instantiate the array of entries for this letter if necessary.
        if( index[ letter ] == null ) {
            index[ letter ] = new ArrayList();
        }

        boolean found = false;

        // Check if the verb is already in the list for this letter.  If
        // found, just add the link to the existing entry's link list.
        for( int e = 0; e < index[ letter ].size(); e++ )
        {
            Entry ie = ( Entry ) index[ letter ].get( e );

            if( ie.verb.equals( newVerb ) )
            {
                ie.links.add( new Link( newType, newText, newLink ) );
                found = true;
            }
        }

        // If the verb was not in the list, add a new entry and start it
        // off with the single link.
        if( !found )
        {
            Entry ie = new Entry();
            ie.verb = newVerb;
            ie.links.add( new Link( newType, newText, newLink ) );
            index[ letter ].add( ie );
        }
    }

    /**
     * Sort the verbs in each index entry array since verbs from the
     * PropBank and FrameNet will have just been added to the end
     * of the lists.  Due to the relatively small number of verbs
     * in each list (on the order of hundreds), a simple and
     * <I>inefficient</I> exchange sort (n-squared) algorithm is used.
     *
     * @see uvi.Generator#generateHTMLFiles()
     */
    static void sort()
    {
        for( int i = 0; i < LETTER_MAX; i++ )
        {
            if( index[ i ] != null )
            {
                for( int a = 0; a < index[ i ].size() - 1; a++ ) {
                    for( int b = a + 1; b < index[ i ].size(); b++ )
                    {
                        Entry q1 = ( Entry ) index[ i ].get( a );
                        Entry q2 = ( Entry ) index[ i ].get( b );

                        if( q1.verb.compareTo( q2.verb ) > 0 )
                        {
                            index[ i ].set( a, q2 );
                            index[ i ].set( b, q1 );
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the total number of verbs represented in the index or the
     * total number of links in the index for a given data source.  If the value
     * -1 is supplied, the number of uniquely represented verbs represented in
     * the index is returned.  If one of the Generator.DS_* values is supplied,
     * the number of index links of that type is returned.
     *
     * @param  type either the value -1 or one of the Generator.DS_* values
     * @return the total number of verbs in the index (for all sources combined)
     *         or the number of links in the index for a single data source.
     * @see    uvi.Generator#generateIndexFiles()
     */
    static int getNumVerbs( int type )
    {
        int count = 0;

        if( type == -1 )
        {
            for( int i = 0; i < LETTER_MAX; i++ ) {
                if( index[ i ] != null ) {
                    count += index[ i ].size();
                }
            }
        }
        else
        {
            for( int i = 0; i < LETTER_MAX; i++ ) {
                if( index[ i ] != null ) {
                    for( int j = 0; j < index[ i ].size(); j++ ) {
                        for( int k = 0; k < ( ( Entry ) index[ i ].get( j ) ).links.size(); k++ ) {
                            if( ( ( Link ) ( ( Entry ) index[ i ].get( j ) ).links.get( k ) ).type == type ) {
                                count++;
                            }
                        }
                    }
                }
            }
        }

        return count;
    }



    ///////////////////
    // Inner Classes //
    ///////////////////

    /**
     * Represents the index entry for a single English verb and holds all relevant links
     * for that verb (from either VerbNet, PropBank, FrameNet).
     * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
     * When this documentation was created, only *.html files were used.  Later, they were
     * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
     *
     * @see uvi.Index#addLink(String, int, String, String)
     */
    static class Entry
    {

        /**
         * The verb (i.e.&nbsp;'abandon').
         */
        String verb;

        /**
         * An array of {@link uvi.Index.Link} objects which hold information that directly
         * feeds into how the index is shown to the user.  Multiple links for this verb
         * could come from any individual source.  So you could have a few VerbNet links,
         * one PropBank link, and a few FrameNet links for example.
         */
        ArrayList links = new ArrayList();
    }

    /**
     * Represents a single link for a verb.  This holds a flag for the source of the link,
     * the text to display for the link, and finally the web address to where the user
     * should be redirected if they click on the link.
     * <BR><BR><I>NOTE: Any reference to "HTML Files" should be taken as a synonym for "PHP Files."
     * When this documentation was created, only *.html files were used.  Later, they were
     * converted to *.php files to facilitate dynamic content (i.e. comments).</I>
     */
    static class Link
    {

        /**
         * One value from Generator.DS_*, indicating the data source of this link.
         */
        int type;

        /**
         * The text to display to the user in the index for this link.
         */
        String text;

        /**
         * The <CODE>href</CODE> for this link.
         */
        String link;

        /**
         * Constructs a link object with the specified information.
         */
        Link( int ty, String tx, String lk )
        {
            type = ty;
            text = tx;
            link = lk;
        }
    }
}

