
///////////////////
// XML Validator //
// Derek Trumbo  //
///////////////////

package semlink.apps.val;

import static java.lang.System.err;
import static java.lang.System.out;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import semlink.util.Parser2;

/**
 * This class allows a user to specify one or more "XML Input Objects" on the command
 * line which can be either files or directories.  These objects will be validated
 * using a Java XML parser and any errors found are sent to standard error.  See
 * the 'run' script and usage for more information.
 *
 * @author Derek Trumbo
 */
public class XMLValidator
{

   ////////////
   // Fields //
   ////////////

   /**
    * An array of strings representing the flags passed to the program
    * These are all of the command-line arguments that begin with a hypen ('-').
    * This array is loaded in {@link vn.XMLValidator#analyzeArguments(String[])} referenced
    * in {@link vn.XMLValidator#printStartInfo()}.  They are printed in the starting banner
    * of the program.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
    * @see vn.XMLValidator#printStartInfo()
    * @see vn.XMLValidator#printUsage()
    * @see vn.XMLValidator#existsFlag(String)
    */
   private static ArrayList flags;

   /**
    * An array of strings representing the non-flag command-line parameters.
    * These will be the "XML Input Objects" that the user wants validated.
    * This variable does not need to be class-level; it could be local.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
    */
   private static ArrayList nonFlags;

   /**
    * An array of all possible flags that this program accepts.  Each flag
    * has both a short form and a long form.  All supplied flags are validated
    * against this array and the existence of any unknown flags triggers
    * an exception.  This variable does not need to be class-level; it could
    * be local.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
    */
   private static String[] allFlags = { "-?", "--help",
                                        "-q", "--quiet" };

   /**
    * A flag set in {@link vn.XMLValidator#analyzeArguments(String[])} and used once only inside
    * of said method.  It is class-level for documentation purposes only.  This
    * flag is set if the user specifies they would like to see the help for the
    * tool.  If the user places a '-?' or '--help' anywhere on the command line
    * then this flag is set to <CODE>true</CODE>, the usage message is displayed
    * and the program exits.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
    * @see vn.XMLValidator#printUsage()
    */
   private static boolean flHelp;

   /**
    * A flag set in {@link vn.XMLValidator#analyzeArguments(String[])} and used throughout the rest
    * of the program.  If this variable is set to <CODE>true</CODE>, only error
    * and warning messages will be sent to standard out.
    *
    * @see vn.XMLValidator#main(String[])
    * @see vn.XMLValidator#analyzeArguments(String[])
    * @see vn.XMLValidator#performParsing()
    */
   static boolean flQuiet;

   /**
    * Holds all the "XML Input Objects" supplied by the user.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
    */
   private static ArrayList<ArgumentGlob> globs = new ArrayList<ArgumentGlob>();

   ////////////////////
   // Helper Methods //
   ////////////////////

   /**
    * Returns a descriptive path for a {@link java.io.File} object.  This is
    * only an issue due to the possibility of the more desired method,
    * {@link java.io.File#getCanonicalPath()} to throw an exception.  If an
    * exception is thrown, the basic {@link java.io.File#getPath()} is returned.
    *
    * @param f the file whose path to retrieve
    * @return a descriptive path
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
    * Sorts an array of {@link java.io.File} objects.
    * This is because different operating systems will return a directory's
    * contents in different orders but we want to always process the files
    * in a consistent fashion.
    */
   private static void sortXMLFiles( File[] xmlFiles )
   {
      for( int a = 0; a < xmlFiles.length - 1; a++ )
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

   /////////////////
   // Constructor //
   /////////////////

   /**
    * This constructor is private because the class is not intended to ever
    * be instantiated.  The XML validation is a very procedural process and
    * thus all the members are static.
    */
   private XMLValidator() {}

   //////////
   // Main //
   //////////

   /**
    * Drives the entire generation process.  This is the only public method.
    * All program-ending exceptions are caught here and displayed.
    *
    * @param args contains the command-line arguments for the program
    */
   public static void main( String args[] )
   {
      try
      {

         // Check command-line arguments for accuracy and set appropriate variables.
         analyzeArguments( args );

         // Print an initial message with basic information.
         if( !flQuiet )
            printStartInfo();

         // Parse and validate all the files and directories supplied by the user.
         performParsing();

         // Print a message to show the process is over.
         if( !flQuiet )
            printEndInfo();
      }

      // The command-line arguments were not valid.
      catch( InvalidCommandLineArgumentException iclae )
      {
         err.println( "ERROR: " + iclae.getMessage() );
         printUsage();
      }

      // A help flag has been supplied.
      catch( UserWantsHelpMessage uwhm )
      {
         printUsage();
      }

      // The files or directories supplied were invalid.
      catch( InvalidInputObjectException ide )
      {
         err.println( "ERROR: " + ide.getMessage() );
      }

      // Show any other errors that might occur.
      catch( Exception e )
      {
         err.println( "ERROR: [Generic/main] " + e.getMessage() );
      }
   }

   //////////////////////////
   // Informational Output //
   //////////////////////////

   /**
    * Prints the usage message.  This message includes all available flags.  This
    * method can be invoked from {@link vn.XMLValidator#main(String[])} either
    * because the user has specified invalid
    * command-line arguments or because the user has requested to see help for
    * the program.  This message is also the 'help' message.
    *
    * @see vn.XMLValidator#main(String[])
    */
   private static void printUsage()
   {
      out.println( "Usage: xml_val [flags] <xml-input-object-1> [<xml-input-object-2> ...]" );
      out.println( "Flags:" );
      out.println( "  -?, --help         Usage" );
      out.println( "  -q, --quiet        Only output error and warning messages" );
      out.println( "An \"XML Input Object\" is either a file in XML format (does not have to end in '.xml')" );
      out.println( "or a directory containing '.xml' files (only the '.xml' files will be validated)." );
      out.println( "There can be any number of these objects on the command line.  All objects will be" );
      out.println( "validated in the order they are supplied." );
   }

   /**
    * Prints information before the validation process has begun.  This method
    * is called from {@link vn.XMLValidator#main(String[])}.  The information printed includes:
    * the "XML Input Objects" given, supplied flags, and date and time when the program was executed.
    *
    * @see vn.XMLValidator#main(String[])
    */
   private static void printStartInfo()
   {
      String af = "",
             dt = new Date().toString();        // Get today's date and time.

      // Create a string list of all the flags, or 'none' if there aren't any.
      if( flags.size() == 0 )
         af = "(none)";

      else
         for( int f = 0; f < flags.size(); f++ )
            af += flags.get( f ) + " ";

      // Output the banner.
      out.println( "XML Validator" );
      out.println( "====================================================" );
      out.println( "Flags:              " + af );
      out.println( "Executed On:        " + dt );
      out.println( "XML Input Objects:  " );

      for( ArgumentGlob glob : globs )
      {
         if( glob.subfiles != null )
         {
            out.println( "--> Directory       " + filePath( glob.argument ) );
            out.println( "    * Found " + glob.subfiles.length + " XML files inside" );
         }
         else
            out.println( "--> File:           " + filePath( glob.argument ) );
      }

      out.println( "====================================================" );
   }


   /**
    * Prints information after the validation process has completed.
    *
    * @see vn.XMLValidator#main(String[])
    */
   private static void printEndInfo()
   {
      out.println( "====================================================" );
      out.println( "Validation Complete" );
   }

   ////////////////////////////
   // Command-Line Arguments //
   ////////////////////////////

   /**
    * Validates all of the command-line arguments and sets appropriate class-level variables.
    *
    * @param args the command-line arguments from {@link vn.XMLValidator#main(String[])}
    * @throws InvalidCommandLineArgumentException if the command-line contains invalid flags
    *         or does not include any files or directories to scan
    * @throws InvalidInputObjectException if the paths requested do not exist or do not have
    *         the appropriate permissions.
    * @throws UserWantsHelpMessage if the user has supplied the help flag.
    * @see vn.XMLValidator#existsFlag(String)
    * @see vn.XMLValidator.MyFilter
    */
   private static void analyzeArguments( String args[] )
      throws InvalidCommandLineArgumentException,
             InvalidInputObjectException,
             UserWantsHelpMessage
   {

      // Allocate the command-line arrays.
      flags    = new ArrayList();
      nonFlags = new ArrayList();

      // Divide the command-line arguments into flags and non-flags.
      for( int a = 0; a < args.length; a++ )
      {
         if( args[ a ].startsWith( "-" ) )
            flags.add( args[ a ] );

         else
            nonFlags.add( args[ a ] );
      }

      // Expand each combined flag (i.e. -ov into -o -v).
      for( int f = 0; f < flags.size(); f++ )
      {
         String flag = ( String ) flags.get( f );

         if( flag.length() > 2 && flag.charAt( 0 ) == '-' && flag.charAt( 1 ) != '-' )
         {
            for( int g = 2; g < flag.length(); g++ )
               flags.add( "-" + flag.charAt( g ) );

            flags.set( f, flag.substring( 0, 2 ) );
         }
      }

      // Set the appropriate class-level booleans.
      flHelp  = existsFlag( "-?" ) || existsFlag( "--help" );
      flQuiet = existsFlag( "-q" ) || existsFlag( "--quiet" );

      // If a help flag was present, stop this method and return to main to
      // show the help/usage message.
      if( flHelp )
         throw new UserWantsHelpMessage();

      // If there is not at least one "XML Input Object" to validate, throw an exception.
      if( nonFlags.size() < 1 )
         throw new InvalidCommandLineArgumentException( "Invalid command line format." );

      // Validate the supplied flags.
      for( int f = 0; f < flags.size(); f++ )
      {
         boolean found = false;

         for( int g = 0; g < allFlags.length; g++ )
            if( flags.get( f ).equals( allFlags[ g ] ) )
            {
               found = true;
               break;
            }

         if( !found )
            throw new InvalidCommandLineArgumentException( "Unknown command line flag." );
      }

      // Create argument globs out of each of the non-flag items supplied
      // on the command-line.
      for( Object nonFlag : nonFlags )
      {
         File flagFile = new File( ( String ) nonFlag );

         if( !flagFile.exists() )
            throw new InvalidInputObjectException( "XML input file or directory does not exist: " + nonFlag );

         String type = ( flagFile.isDirectory() ? "directory" : "file" );

         if( !flagFile.canRead() )
            throw new InvalidInputObjectException( "XML input " + type + " not readable: " + nonFlag   );

         ArgumentGlob glob = new ArgumentGlob();

         glob.argument = flagFile;

         if( flagFile.isDirectory() )
         {

            // Get the list of all XML files in the input directory.
            glob.subfiles = flagFile.listFiles( new MyFilter( "xml" ) );

            // Sort so the output is nicer.
            sortXMLFiles( glob.subfiles );
         }

         globs.add( glob );
      }
   }

   /**
    * Checks whether the given flag was supplied by the user.
    *
    * @param flag the flag to look for
    * @return <CODE>true</CODE> if the flag exists in the user's supplied
    *         command-line arguments.
    * @see vn.XMLValidator#analyzeArguments(String[])
    */
   private static boolean existsFlag( String flag )
   {
      for( int f = 0; f < flags.size(); f++ )
         if( flags.get( f ).equals( flag ) )       // Case-sensitive comparison
            return true;

      return false;
   }

   /////////////
   // Parsing //
   /////////////

   /**
    * Perform the XML parsing and validation on all the arguments supplied on the
    * command-line.
    *
    * @see vn.XMLValidator#main(String[])
    */
   private static void performParsing()
   {
      for( ArgumentGlob glob : globs )
      {

         // If this argument is a file...
         if( glob.subfiles == null )
         {
            if( !flQuiet )
               out.println( "--> File: Validating \"" + filePath( glob.argument ) + "\"..." );

            // Parse the single file for errors.
            if( Parser2.parse( glob.argument ) && !flQuiet )
               out.println( "   ...XML OK." );
         }

         // Else if it's a directory...
         else
         {
            if( !flQuiet )
               out.println( "--> Directory: Validating \"" + filePath( glob.argument ) + "\"..." );

            // Parse all the files in the directory.
            if( Parser2.parse( glob.subfiles ) && !flQuiet )
               out.println( "   ...XML OK." );
         }
      }
   }

   //////////////////////////
   // Supplemental Classes //
   //////////////////////////

   /**
    * Exception class for identifying when the user did not supply a
    * command-line of the proper format.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
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
    * Exception class for identifying when the user specifies an "XML Input Object"
    * that does not exist or does not have the appropriate permissions.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
    */
   private static class InvalidInputObjectException extends IOException
   {

      /**
       * Constructs the exception object with the given message.
       *
       * @param message the text of the exception
       */
      public InvalidInputObjectException( String message )
      {
         super( message );
      }
   }

   /**
    * Exception class for identifying when the user requests to
    * view the help/usage message for the program.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
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
    * Decides which files to select for the {@link java.io.File#listFiles()}
    * method of the {@link java.io.File} class.
    *
    * @see vn.XMLValidator#analyzeArguments(String[])
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
       * @param newExt the extension of the files to select (e.g. "xml")
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
    * Represents an "XML Input Object" (file or directory) supplied on the command line
    * along with the files it contains in the case of a directory.
    *
    * @see vn.XMLValidator#globs
    */
   private static class ArgumentGlob
   {

      /**
       * An "XML Input Object" (file or directory) supplied on the command line.
       */
      File argument;

      /**
       * If this glob refers to a directory, the list of
       * files inside the directory.  Has the value of <CODE>null</CODE>
       * if this glob doesn't refer to a directory.
       */
      File[] subfiles;
   }
}

