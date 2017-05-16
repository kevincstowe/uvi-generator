
///////////////////
// UVI Generator //
// Derek Trumbo  //
///////////////////

///////////////////////////////////////////////////////////
// THIS CLASS NO LONGER USED - REPLACED BY MembershipMap //
///////////////////////////////////////////////////////////

package semlink.apps.uvig;

import java.util.ArrayList;

/**
 * Holds tokens (or 'words') separated into different lists.  Each token is
 * only allowed to be present once in a given list (a.k.a. each
 * list holds unique tokens only).  This class is used to
 * aggregate tokens during the XML parsing to be used later on
 * a reference page.
 *
 * @see uvi.Sweeper#startNP(Node)
 * @see uvi.Sweeper#startSELRESTR(Node)
 * @see uvi.Sweeper#startSYNRESTR(Node)
 * @see uvi.Sweeper#startPRED(Node)
 * @see uvi.Sweeper#startTHEMROLE(Node)
 * @see uvi.Sweeper#startDESCRIPTION(Node)
 * @see uvi.Generator#generateReferencePage()
 *
 * @author Derek Trumbo
 */
public class WordLists
{

    ////////////
    // Fields //
    ////////////

    /**
     * All lists of tokens.  Each element of this array is a single
     * {@link uvi.WordLists.WordList} object which stores the name
     * of the list and all tokens for that list.
     */
    public ArrayList lists = new ArrayList();

    /////////////////
    // Constructor //
    /////////////////

    public WordLists() {}

    /////////////
    // Methods //
    /////////////

    /**
     * Adds a token to the specified list.  If the list does not yet exist,
     * one is created.  The token is not added to the list if it already exists.
     * The token is added in alphabetical order.
     *
     * @see uvi.Sweeper#startNP(Node)
     * @see uvi.Sweeper#startSELRESTR(Node)
     * @see uvi.Sweeper#startSYNRESTR(Node)
     * @see uvi.Sweeper#startPRED(Node)
     */
    public void addWord( String list, String word )
    {
        boolean found = false;

        // Search the lists array to see if a list with the given name
        // alreay exists and add the token to it if it does.
        for( int l = 0; l < lists.size(); l++ )
        {
            WordList wl = ( WordList ) lists.get( l );

            // If the list has been found...
            if( wl.listName.equals( list ) )
            {
                boolean wFound = false;

                wl.count++;

                // Check to see if the token is already in this list.
                for( int w = 0; w < wl.tokens.size(); w++ ) {
                    if( ( ( WordListItem ) wl.tokens.get( w ) ).token.equals( word ) )
                    {
                        wFound = true;

                        ( ( WordListItem ) wl.tokens.get( w ) ).count++;

                        break;
                    }
                }

                // If the token was not found in this list, then add the
                // new token to the list in sorted order.
                if( !wFound )
                {
                    int w;

                    // Search for the insertion point.
                    for( w = 0; w < wl.tokens.size(); w++ ) {
                        if( ( ( WordListItem ) wl.tokens.get( w ) ).token.compareTo( word ) > 0 ) {
                            break;
                        }
                    }

                    // Add the token to this list in alphabetical order.
                    wl.tokens.add( w, new WordListItem( word ) );
                }

                found = true;
                break;
            }
        }

        // If a list with the specified name does not yet exist,
        // create it, add the token to it, and add the list to the
        // lists array.
        if( !found )
        {
            WordList wl = new WordList( list );
            wl.tokens.add( new WordListItem( word ) );
            lists.add( wl );
        }
    }

    /**
     * Returns a list with the specified name.
     *
     * @param listName the name of the list to return
     * @return the {@link uvi.WordLists.WordList} object that has
     *         the given name, or <CODE>null</CODE> if no such list
     *         exists
     * @see uvi.Generator#generateReferencePage()
     */
    public WordList getList( String listName )
    {
        for( int l = 0; l < lists.size(); l++ )
        {
            WordList wl = ( WordList ) lists.get( l );

            if( wl.listName.equals( listName ) ) {
                return wl;
            }
        }

        return null;
    }

    /**
     * Sorts the main list of lists by the list counts.
     *
     * @see uvi.Generator#printReferenceColumnSpecial(String)
     */
    public void sortByListCounts()
    {
        for( int a = 0; a < lists.size() - 1; a++ ) {
            for( int b = a + 1; b < lists.size(); b++ )
            {
                WordList al = ( WordList ) lists.get( a );
                WordList bl = ( WordList ) lists.get( b );

                if( al.count < bl.count )
                {
                    lists.set( a, bl );
                    lists.set( b, al );
                }
                else if( al.count == bl.count && al.listName.compareTo( bl.listName ) > 0 )
                {
                    lists.set( a, bl );
                    lists.set( b, al );
                }
            }
        }
    }

    ///////////////////
    // Inner Classes //
    ///////////////////

    /**
     * Holds the information for a single list of tokens.  This includes
     * the name of the list (which is an arbitrary token itself) and
     * the list of tokens (stored as {@link java.lang.String}s).
     */
    static class WordList
    {

        /**
         * The name of the list (i.e.&nbsp;'SelRestr').
         */
        String listName;

        /**
         * The number of times this list is added to (regardless of whether
         * the token being added to the list is not already added because it's
         * already in the list)
         */
        int count;

        /**
         * An array of tokens for this list.
         */
        ArrayList tokens = new ArrayList();

        /**
         * Constructs a blank {@link uvi.WordLists} object with the given name.
         */
        WordList( String newName )
        {
            listName = newName;
            count = 1;
        }
    }

    /**
     * Represents a token and its associated reference count.
     */
    static class WordListItem
    {
        /**
         * The text of the token.
         */
        String token;

        /**
         * The number of times this token was referenced.
         */
        int count;

        public WordListItem( String t )
        {
            token = t;
            count = 1;
        }
    }
}

