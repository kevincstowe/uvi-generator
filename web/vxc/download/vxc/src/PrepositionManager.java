
////////////////////////////
// VxC: A VN-Cyc Mapper   //
// Built on the Inspector //
// University of Colorado //
// Fall 2006              //
////////////////////////////

package vn;

/**
 * This class acts as a simple organizer for all of the preposition classes
 * in VerbNet, and the prepositions inside them.  This class is utilized
 * in place of what would have been a more complicated parsing routine.
 * This class basically encompasses all the information in the document:<BR>
 * &nbsp;&nbsp;&nbsp;<A href="http://verbs.colorado.edu/verb-index/preps.txt"
 * target="_blank">http://verbs.colorado.edu/verb-index/preps.txt</A><BR>
 * The uncertainties contained in the document above are ignored.  In other
 * words, <CODE>(?)isa(from,loc)</CODE> is taken to be <CODE>isa(from,loc)</CODE>
 * and <CODE>%only for intrans?</CODE> is ignored.
 *
 * @see vn.Matcher
 * @see vn.Matcher#cycPreposition(String, String)
 *
 * @author Derek Trumbo
 * @version 1.0, 2006.10.25
 */
public class PrepositionManager
{

   ////////////
   // Fields //
   ////////////

   /**
    * Represents a specific VerbNet preposition class.  This string will hold all
    * the prepositions in the class in a delimited list.
    *
    * @see vn.PrepositionManager#prepInClass(String, String)
    */
   private static String spatial, path, dest, loc, dir,
                         dest_dir, dest_conf, src;

   ////////////////////////
   // Static Initializer //
   ////////////////////////

   /**
    * This static initializer sets up the "database" of prepositions, in their
    * respective classes, just as they are organized in the preposition file.
    * This static initializer is loaded and executed the moment the Inspector
    * is run, so that if and when it calls prepInClass(), these are all ready
    * to go.  The ampersand is used as a delimiter in these strings.
    */
   static
   {

      // Set up the lowest-level classes.
      loc       = "&about&above&against&along&alongside&amid&among&amongst&around&astride&at&athwart&before&" +
                  "behind&below&beneath&beside&between&beyond&by&from&in&in_front_of&inside&near&next_to&off&" +
                  "on&opposite&out_of&outside&over&round&throughout&under&underneath&upon&within&";
      dir       = "&across&along&around&down&over&past&round&through&towards&up&";
      dest_dir  = "&for&at&to&towards&";
      dest_conf = "&into&onto&";
      src       = "&from&out&out_of&off&off_of&";

      // Set up the aggregate classes.
      dest    = dest_dir + dest_conf;
      path    = dir + dest + src;
      spatial = loc + path;
   }

   /////////////////
   // Constructor //
   /////////////////

   /**
    * This constructor is private because the class is not intended to ever
    * be instantiated.  The Inspector's job is very procedural and thus
    * all its members are static.
    */
   private PrepositionManager() {}

   /////////////
   // Methods //
   /////////////

   /**
    * Prints out the prepositions and their classes, held internally, in a format
    * identical to the original document at<BR>
    * &nbsp;&nbsp;&nbsp;<A href="http://verbs.colorado.edu/verb-index/preps.txt"
    * target="_blank">http://verbs.colorado.edu/verb-index/preps.txt</A><BR>
    * so that the output can be <CODE>diff</CODE>'ed with the original file when
    * tracking changes to the prepositions.  Provided more for verification
    * than anything else.
    */
   static void printPreps()
   {
      System.out.println( "% dynamic spatial prep" );
      System.out.println( "isa(src,path)" );
      System.out.println( "isa(dir,path)" );
      System.out.println( "isa(dest,path)" );
      System.out.println( "isa(dest_conf,dest)" );
      System.out.println( "isa(dest_dir,dest)" );
      System.out.println( "isa(path,spatial)" );
      System.out.println( "isa(loc,spatial)" );
      System.out.println( "" );

      String[] aa = src.split( "&" );
      System.out.println( "% src" );
      for( int x = 0; x < aa.length; x++ )
         if( !aa[ x ].equals( "" ) )
            System.out.println( "isa(" + aa[ x ] + ",src)" );

      System.out.println( "" );

      aa = dest_conf.split( "&" );
      System.out.println( "% dest_conf" );
      for( int x = 0; x < aa.length; x++ )
         if( !aa[ x ].equals( "" ) )
            System.out.println( "isa(" + aa[ x ] + ",dest_conf)" );

      System.out.println( "" );

      aa = dest_dir.split( "&" );
      System.out.println( "% dest_dir" );
      for( int x = 0; x < aa.length; x++ )
         if( !aa[ x ].equals( "" ) )
            System.out.println( "isa(" + aa[ x ] + ",dest_dir)" );

      System.out.println( "" );

      aa = dir.split( "&" );
      System.out.println( "% dir" );
      for( int x = 0; x < aa.length; x++ )
         if( !aa[ x ].equals( "" ) )
            System.out.println( "isa(" + aa[ x ] + ",dir)" );

      System.out.println( "" );

      aa = loc.split( "&" );
      System.out.println( "% loc" );
      for( int x = 0; x < aa.length; x++ )
         if( !aa[ x ].equals( "" ) )
            System.out.println( "isa(" + aa[ x ] + ",loc)" );
   }

   /**
    * Returns whether or not a preposition is contained within a certain
    * VerbNet class.
    *
    * @param cls the preposition class (i.e. "spatial" or "dest_dir")
    * @param prp the preposition (i.e. "around" or "between")
    * @return whether or not the preposition is contained within the given
    *         VerbNet class
    * @see vn.Matcher#cycPreposition(String, String)
    */
   static boolean prepInClass( String cls, String prp )
   {
      String use = "";

      if( cls.equalsIgnoreCase( "spatial" ) )          use = spatial;
      else if( cls.equalsIgnoreCase( "path" ) )        use = path;
      else if( cls.equalsIgnoreCase( "dest" ) )        use = dest;
      else if( cls.equalsIgnoreCase( "loc" ) )         use = loc;
      else if( cls.equalsIgnoreCase( "dir" ) )         use = dir;
      else if( cls.equalsIgnoreCase( "dest_dir" ) )    use = dest_dir;
      else if( cls.equalsIgnoreCase( "dest_conf" ) )   use = dest_conf;
      else if( cls.equalsIgnoreCase( "src" ) )         use = src;
      else
         System.err.println( "ERROR: Unknown preposition class (" + cls + ")" );

      return use.indexOf( "&" + prp + "&" ) != -1;
   }
}
