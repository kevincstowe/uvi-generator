
///////////////////
// UVI Generator //
// Derek Trumbo  //
///////////////////

package semlink.apps.uvig;

import java.util.ArrayList;

/**
 * This class holds the VerbNet-FrameNet mapping created by Andrew Dolbey
 * in a format useful to the UVIG.
 *
 * @see uvi.Generator#generateHTMLFiles()
 * @see uvi.Generator#addOthers(int)
 *
 * @author Derek Trumbo
 */
public class VN_FN_Map
{

    ////////////
    // Fields //
    ////////////

    /**
     * An array which holds all of the mapping information.  Each element
     * of this array is an object which contains another array that holds
     * a list of VerbNet-FrameNet mapped pairs for that class.
     *
     * @see uvi.VN_FN_Map.ClassVerbs
     */
    private static ArrayList classes = new ArrayList();

    /////////////////
    // Constructor //
    /////////////////

    /**
     * This constructor is private because the class is not intended to ever
     * be instantiated.  The UVI generation is a very procedural process and
     * thus all the members are static.
     */
    private VN_FN_Map() {}

    /////////////
    // Methods //
    /////////////

    /**
     * Registers a VerbNet-FrameNet mapped pair to the mapping under for the
     * given class.  The VN-FN mapped pair is added to the list for that
     * class.  This is mainly for searching efficiency when frames are being
     * looked up in {@link uvi.Sweeper#printMembers()}.
     *
     * @param cls the class in which the verb is contained
     * @param vn  the VerbNet verb
     * @param fn  the FrameNet frame
     * @see uvi.Generator#addOthers(int)
     */
    static void addMapPair( String cls, String vn, String fn )
    {
        boolean found = false;

        // Search the main array to see if the requested class already exists.
        for( int c = 0; c < classes.size(); c++ )
        {
            ClassVerbs cv = ( ClassVerbs ) classes.get( c );

            // If the class already exists, then just add the mapping to it.
            if( cv.cls.equals( cls ) )
            {
                cv.pairs.add( new MapPair( vn, fn ) );
                found = true;
                break;
            }
        }

        // If the class was not yet found, create the class, add the mapping
        // to it and add the class to the main array.
        if( !found )
        {
            ClassVerbs newCV = new ClassVerbs();

            newCV.cls = cls;
            newCV.pairs.add( new MapPair( vn, fn ) );

            classes.add( newCV );
        }
    }

    /**
     * Returns the list of all FrameNet frames that have been mapped to the
     * verb in the given class.
     *
     * @param cls the VerbNet class currently being scanned in the Sweeper.
     *        An example is '9.1' since the VN-FN data source does not
     *        have full class names like the XML files (i.e. 'put-9.1').
     * @param vn the VerbNet verb being printed right now
     * @see uvi.Sweeper#printMembers()
     */
    static String getFrameNet( String cls, String vn )
    {
        String ret = "";
        String cnum = cls.substring( cls.indexOf( "-" ) + 1 );

        for( int c = 0; c < classes.size(); c++ )
        {
            ClassVerbs cv = ( ClassVerbs ) classes.get( c );

            // If the current class in the list (i.e. '9.1' is the same as
            // the class passed in, 'put-9.1', then it's a good match).
            if( cnum.equals( cv.cls ) )
            {

                // Search for the verb in this class's list
                for( int p = 0; p < cv.pairs.size(); p++ )
                {
                    MapPair mp = ( MapPair ) cv.pairs.get( p );

                    // If there is a match, add on the frame name. --- also allow for empty vn to return all fn mappings for this class
                    if( mp.vn.equalsIgnoreCase( vn ) || vn.equals("") )
                    {
                        ret += mp.fn + "@@";

                        // Signal that this map pair was utilized.
                        mp.beenUsed = true;
                    }
                }

                break;
            }
        }

        // Return all the frames that match (i.e. 'Cause_change_of_phase@@Change_of_phase@@')
        return ret;
    }

    /**
     * Prints all the information in the mapping.
     */
    static void printAll()
    {
        for( int c = 0; c < classes.size(); c++ )
        {
            ClassVerbs cv = ( ClassVerbs ) classes.get( c );

            for( int p = 0; p < cv.pairs.size(); p++ )
            {
                MapPair mp = ( MapPair ) cv.pairs.get( p );

                System.out.println( "VN-FN Mapping: match between \""
                    + cv.cls + "/" + mp.vn + "\" and \"" + mp.fn + "\" (" + ( mp.beenUsed ? "ok" : "not used" ) + ")" );
            }
        }
    }


    /**
     * Prints a warning message for each VN-FN match that was not utilized somewhere
     * in the UVI.
     *
     * @see uvi.Generator#generateHTMLFiles()
     * @see VN_FN_Map#getFrameNet(String, String)
     */
    static void printUnused()
    {
        for( int c = 0; c < classes.size(); c++ )
        {
            ClassVerbs cv = ( ClassVerbs ) classes.get( c );

            for( int p = 0; p < cv.pairs.size(); p++ )
            {
                MapPair mp = ( MapPair ) cv.pairs.get( p );

                if( !mp.beenUsed ) {
                    System.err.println( "WARNING: VN-FN Mapping contains unused match between \""
                        + cv.cls + "/" + mp.vn + "\" and \"" + mp.fn + "\"" );
                }
            }
        }
    }

    ///////////////////
    // Inner Classes //
    ///////////////////

    /**
     * Holds the mappings for a single VerbNet class.
     *
     * @see uvi.VN_FN_Map.MapPair
     */
    static class ClassVerbs
    {

        /**
         * The class (i.e.&nbsp;'9.1-1').
         */
        String cls;

        /**
         * The list of VerbNet-FrameNet mappings for this VerbNet class.
         *
         * @see uvi.VN_FN_Map.MapPair
         */
        ArrayList pairs = new ArrayList();
    }

    /**
     * Represents a single mapping between a VerbNet verb and a FrameNet
     * frame.
     */
    static class MapPair
    {

        /**
         * The VerbNet verb (i.e.&nbsp;'abandon').
         */
        String vn;

        /**
         * The FrameNet frame name (i.e.&nbsp;'Departing').
         */
        String fn;

        /**
         * Whether or not this mapping has been utilized somewhere in the
         * UVI.
         */
        boolean beenUsed;

        /**
         * Constructs a MapPair object with the specified information.
         *
         * @param newVN the VerbNet verb
         * @param newFN the FrameNet frame
         */
        MapPair( String newVN, String newFN )
        {
            vn = newVN;
            fn = newFN;
        }
    }
}

