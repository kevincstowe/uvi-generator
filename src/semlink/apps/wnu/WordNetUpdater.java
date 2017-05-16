
/////////////////////
// WordNet Updater //
// Derek Trumbo    //
/////////////////////

package semlink.apps.wnu;

import static java.lang.System.err;
import static java.lang.System.out;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;

import semlink.util.Parser;

/**
 * This class facilitates the updating of the WordNet sense keys within
 * the VerbNet XML files.  The default action of this program is to
 * display statistics associated with the desired translation (doesn't
 * change anything or create new files).  If you would like the program to
 * generate a new set of XML files with the sense key translations effected,
 * you must supply the -n (--new-xml) flag and provide an XML output directory.
 * <BR><BR>
 * This program accepts as its first two arguments, the XML input directory
 * and the WordNet sense mapping directory.  The latter is the directory
 * that is provided directory from the WordNet download site.  The
 * program looks inside for a '*.verb.mono' file and a '*.verb.poly' file.
 * These will be the files used to translate the VerbNet sense keys.
 * <BR><BR>
 * The statistics report is always sent to standard out.
 */
public class WordNetUpdater
{

    ////////////
    // Fields //
    ////////////

    /**
     * An array of strings representing the flags passed to the program
     * These are all of the command-line arguments that begin with a hypen ('-').
     * This array is loaded in {@link vn.WordNetUpdater#analyzeArguments(String[])} referenced
     * in {@link vn.WordNetUpdater#printStartInfo()}.  They are printed in the starting banner
     * of the program.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#printStartInfo()
     * @see vn.WordNetUpdater#printUsage()
     * @see vn.WordNetUpdater#existsFlag(String)
     */
    private static ArrayList flags;

    /**
     * An array of strings representing the non-flag command-line parameters.
     * This array should hold the XML input directory, the WN mapping directory,
     * and the XML output directory,
     * and will be validated as such.  The arguments will be expected in that
     * order, with the XML output directory being optional.
     * This variable does not need to be class-level; it could be local.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     */
    private static ArrayList nonFlags;

    /**
     * An array of all possible flags that this program accepts.  Each flag
     * has both a short form and a long form.  All supplied flags are validated
     * against this array and the existence of any unknown flags triggers
     * an exception.  This variable does not need to be class-level; it could
     * be local.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     */
    private static String[] allFlags = { "-?", "--help",
                                         "-n", "--new-xml",
                                         "-o", "--overwrite" };

    /**
     * A flag set in {@link vn.WordNetUpdater#analyzeArguments(String[])} and used once only inside
     * of said method.  It is class-level for documentation purposes only.  This
     * flag is set if the user specifies they would like to see the help for the
     * tool.  If the user places a '-?' or '--help' anywhere on the command line
     * then this flag is set to <CODE>true</CODE>, the usage message is displayed
     * and the program exits.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#printUsage()
     */
    private static boolean flHelp;

    /**
     * A flag set in {@link vn.WordNetUpdater#analyzeArguments(String[])} and used once only inside
     * of said method.  It is class-level for documentation purposes only.
     * If this flag is set to <CODE>true</CODE> then the user is requesting that
     * XML is generated with the sense keys translated.  This will imply the user has
     * supplied the XML output directory.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#printUsage()
     * @see vn.WordNetUpdater#updateWordNetSenses()
     */
    public static boolean flNewXML;

    /**
     * A flag set in {@link vn.WordNetUpdater#analyzeArguments(String[])} and used throughout the rest
     * of the program.  If this variable is set to <CODE>true</CODE>, the program
     * will write over existing files that it needs to create.  If this flag
     * is not specified then the program will not destroy existing files.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     */
    public static boolean flOverwrite;

    /**
     * An array of {@link java.io.File} objects that represent all the XML files in the
     * specified XML input directory.  This is loaded up in {@link vn.WordNetUpdater#analyzeArguments(String[])}
     * and referenced in ?.  If there are no XML files in
     * the XML input directory an exception is thrown and the program exits.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     */
    private static File[] xmlFiles;

    /**
     * The {@link java.io.File} object that represents the user's desired XML input directory.
     * This is used to gather the XML files, check that a DTD file exists, and show in
     * the starting banner of the program.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#printStartInfo()
     */
    private static File vnIn;

    /**
     * The {@link java.io.File} object that represents the user's desired HTML output directory.
     * This is referenced essentially whenever a method calls
     * as this method always uses the desired HTML output directory since the program
     * does not output to any other directory for any reason.  This is first set in
     * {@link vn.WordNetUpdater#analyzeArguments(String[])}, and referenced to create HTML files and copy
     * supplemental files.  It is also shown in the starting banner of the program.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#printStartInfo()
     */
    private static File vnOut;

    /**
     *
     */
    private static File wnMap;

    /**
     *
     */
    private static File wnMono;

    /**
     *
     */
    private static File wnPoly;

    private static String fromVersion;
    private static String toVersion;

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
     * Sorts the already loaded XML files array {@link vn.WordNetUpdater#xmlFiles}.
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
     * be instantiated.  The WordNet updating is a very procedural process and
     * thus all the members are static.
     */
    private WordNetUpdater() {}

    //////////
    // Main //
    //////////

    /**
     *
     */
    public static void main( String args[] )
    {
        try
        {

            // Check command-line arguments for accuracy and set appropriate variables.
            analyzeArguments( args );

            // Print the configuration information.
            printStartInfo();

            // Verify the syntax of the XML files.
            verifyXMLSyntax();

            //
            verifyWordNetVersions();

            //
            updateWordNetSenses();
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

        // User does not wish to continue after viewing versions.
        catch( IncorrectVersion iv )
        {
            out.println( "Update aborted by user." );
        }

        // The input or output directory was not valid.
        catch( InvalidPathException ide )
        {
            err.println( "ERROR: " + ide.getMessage() );
        }

        // There are some invalid XML files that would break the manual parsing
        // in the Translator class.
        catch( BadXMLException bxe )
        {
            err.println( "ERROR: " + bxe.getMessage() );
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
     * method can be invoked from main either because the user has specified invalid
     * command-line arguments or because the user has requested to see help for
     * the program.  This message is also the 'help' message.
     *
     * @see vn.WordNetUpdater#main(String[])
     */
    private static void printUsage()
    {
        out.println( "WordNetUpdater for VerbNet" );
        out.println( "Usage: vn_wnu [flags] <xml-input-dir> <wn-map-dir> [<xml-output-dir>]" );
        out.println( "Flags:" );
        out.println( "  -?, --help        Usage" );
        out.println( "  -n, --new-xml     Generate new VerbNet XML files with translated keys." );
        out.println( "                    Requires VerbNet XML output directory." );
        out.println( "  -o, --overwrite   Overwrite existing files" );
    }

    /**
     * Prints information before the HTML generation process has completed.  This method
     * is called from {@link vn.WordNetUpdater#main(String[])}.  The information printed includes: the desired
     * XML input directory, the desired HTML output directory, supplied flags, and
     * date and time when the program was executed.
     *
     * @see vn.WordNetUpdater#main(String[])
     */
    private static void printStartInfo()
    {
        String af = "",
        dt = new Date().toString(),        // Get today's date and time.
        wm,
        wp,
        vi,
        vo;

        // Create a string list of all the flags, or 'none' if there aren't any.
        if( flags.size() == 0 ) {
            af = "(none)";
        } else {
            for( int f = 0; f < flags.size(); f++ ) {
                af += flags.get( f ) + " ";
            }
        }

        // Get string representations of the files' paths.
        wm = filePath( wnMono );
        wp = filePath( wnPoly );
        vi = filePath( vnIn );

        if( vnOut == null ) {
            vo = "(none)";
        } else {
            vo = filePath( vnOut );
        }

        // Output the banner.
        out.println( "WordNetUpdater for VerbNet" );
        out.println( "====================================================" );
        out.println( "WordNet Mapping Mono File:    " + wm );
        out.println( "WordNet Mapping Poly File:    " + wp );
        out.println( "VerbNet XML Input Directory:  " + vi );
        out.println( "VerbNet XML Output Directory: " + vo );
        out.println( "Flags:                        " + af );
        out.println( "Executed On:                  " + dt );
        if( vnOut == null )
        {
            out.println( "" );
            out.println( "NOTE: New XML files not requested.  Just a report will be printed." );
        }
        out.println( "====================================================" );
    }

    ////////////////////////////
    // Command-Line Arguments //
    ////////////////////////////

    /**
     * Validates all of the command-line arguments and sets appropriate class-level variables.
     *
     * @param args the command-line arguments from {@link vn.WordNetUpdater#main(String[])}
     * @throws InvalidCommandLineArgumentException if the command-line contains invalid flags
     *         or does not contain the two requisite paths.
     * @throws InvalidPathException if the paths requested do not exist or do not have
     *         the appropriate permissions.
     * @throws UserWantsHelpMessage if the user has supplied the help flag.
     * @see vn.WordNetUpdater#existsFlag(String)
     * @see vn.WordNetUpdater.MyFilter
     */
    private static void analyzeArguments( String args[] )
    throws InvalidCommandLineArgumentException,
    InvalidPathException,
    UserWantsHelpMessage,
    IncorrectVersion,
    IOException
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

        // Expand each combined flag (i.e. -on into -o -n).
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
        flNewXML    = existsFlag( "-n" ) || existsFlag( "--new-xml"   );
        flOverwrite = existsFlag( "-o" ) || existsFlag( "--overwrite" );

        // If a help flag was present, stop this method and return to main to
        // show the help/usage message.
        if( flHelp ) {
            throw new UserWantsHelpMessage();
        }

        // If there are not exactly two non-flag arguments, then show an error message.
        // The two non-flag arguments are the XML input directory path and HTML output
        // directory path.
        if( nonFlags.size() != 2 && !flNewXML || nonFlags.size() != 3 && flNewXML ) {
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
                throw new InvalidCommandLineArgumentException( "Unknown command line flag: " + flags.get(f) );
            }
        }

        // Create the File objects for the supplied paths.
        vnIn  = new File( ( String ) nonFlags.get( 0 ) );
        wnMap = new File( ( String ) nonFlags.get( 1 ) );

        if( flNewXML ) {
            vnOut  = new File( ( String ) nonFlags.get( 2 ) );
        }


        if( !vnIn.exists() ) {
            throw new InvalidPathException( "XML input directory path does not exist."     );
        }
        if( !vnIn.isDirectory() ) {
            throw new InvalidPathException( "XML input directory path is not a directory." );
        }
        if( !vnIn.canRead() ) {
            throw new InvalidPathException( "XML input directory not readable."            );
        }

        if( !wnMap.exists() ) {
            throw new InvalidPathException( "WordNet mapping directory path does not exist." );
        }
        if( !wnMap.isDirectory() ) {
            throw new InvalidPathException( "WordNet mapping directory path is not a directory."  );
        }
        if( !wnMap.canRead() ) {
            throw new InvalidPathException( "WordNet mapping directory not readable."   );
        }

        if( flNewXML )
        {
            if( !vnOut.exists() ) {
                throw new InvalidPathException( "XML output directory path does not exist."     );
            }
            if( !vnOut.isDirectory() ) {
                throw new InvalidPathException( "XML output directory path is not a directory." );
            }
            if( !vnOut.canWrite() ) {
                throw new InvalidPathException( "XML output directory not modifiable."          );
            }
        }

        findMonoAndPolyFiles();

        if( !wnMono.exists() ) {
            throw new InvalidPathException( "WordNet mapping mono path does not exist." );
        }
        if( !wnMono.isFile() ) {
            throw new InvalidPathException( "WordNet mapping mono path is not a file."  );
        }
        if( !wnMono.canRead() ) {
            throw new InvalidPathException( "WordNet mapping mono file not readable."   );
        }

        if( !wnPoly.exists() ) {
            throw new InvalidPathException( "WordNet mapping poly path does not exist." );
        }
        if( !wnPoly.isFile() ) {
            throw new InvalidPathException( "WordNet mapping poly path is not a file."  );
        }
        if( !wnPoly.canRead() ) {
            throw new InvalidPathException( "WordNet mapping poly file not readable."   );
        }

        verifyWordNetFileNames();

        // Get the list of all XML files in the input directory.
        xmlFiles = vnIn.listFiles( new MyFilter( "xml" ) );

        // Make sure there is at least one XML file in the input directory.
        if( xmlFiles.length == 0 ) {
            throw new InvalidPathException( "There are no XML files in the XML input directory." );
        }

        sortXMLFiles();
    }

    /**
     * Checks whether the given flag was supplied by the user.
     *
     * @param flag the flag to look for
     * @return <CODE>true</CODE> if the flag exists in the user's supplied
     *         command-line arguments.
     * @see vn.WordNetUpdater#analyzeArguments(String[])
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

    private static void findMonoAndPolyFiles() throws InvalidPathException
    {
        File[] m = wnMap.listFiles( new MyFilter( "verb.mono" ) );
        File[] p = wnMap.listFiles( new MyFilter( "verb.poly" ) );

        if( m.length == 0 ) {
            throw new InvalidPathException( "No *.verb.mono file found in supplied mapping directory." );
        } else if( m.length > 1 ) {
            throw new InvalidPathException( "Multiple *.verb.mono files found in supplied mapping directory." );
        } else if( p.length == 0 ) {
            throw new InvalidPathException( "No *.verb.poly file found in supplied mapping directory." );
        } else if( p.length > 1 ) {
            throw new InvalidPathException( "Multiple *.verb.poly files found in supplied mapping directory." );
        }

        wnMono = m[ 0 ];
        wnPoly = p[ 0 ];
    }

    private static void verifyWordNetFileNames() throws InvalidPathException
    {
        String m = wnMono.getName();
        String p = wnPoly.getName();

        int mto   = m.indexOf( "to" );
        int mverb = m.indexOf( "verb" );

        int pto   = p.indexOf( "to" );
        int pverb = p.indexOf( "verb" );

        if( mto == -1 ) {
            throw new InvalidPathException( "*.verb.mono file should have file name in format v1tov2.verb.mono." );
        }
        if( pto == -1 ) {
            throw new InvalidPathException( "*.verb.poly file should have file name in format v1tov2.verb.poly." );
        }

        String mV1 = m.substring( 0, mto );
        String mV2 = m.substring( mto + 2, mverb - 1 );

        String pV1 = p.substring( 0, pto );
        String pV2 = p.substring( pto + 2, pverb - 1 );

        if( mV1.equals( "" ) || mV2.equals( "" ) ) {
            throw new InvalidPathException( "*.verb.mono file should have file name in format v1tov2.verb.mono." );
        }
        if( pV1.equals( "" ) || pV2.equals( "" ) ) {
            throw new InvalidPathException( "*.verb.poly file should have file name in format v1tov2.verb.poly." );
        }
        if( !mV1.equals( pV1 ) || !mV2.equals( pV2 ) ) {
            throw new InvalidPathException( "The versions of *.verb.mono and *.verb.poly do not match." );
        }

        fromVersion = mV1;
        toVersion = mV2;
    }

    //
    private static void verifyXMLSyntax() throws BadXMLException
    {
        out.println( "Verifying XML..." );
        if(true) {
            return;
        }
        if( Parser.parse( xmlFiles ) ) {
            out.println( "...XML OK." );
        } else {
            throw new BadXMLException( "There were syntax errors encountered in the VerbNet XML files.  Update aborted." );
        }
    }

    //
    private static void verifyWordNetVersions() throws IncorrectVersion, IOException
    {
        out.println( "The following versions have been found on the specified WordNet mapping files:" );
        out.println( "  Old Version: " + fromVersion );
        out.println( "  New Version: " + toVersion );
        out.print( "Do you wish to proceed? (y/n): " );

        int response = System.in.read();

        out.println( "" );

        if( ( char ) response != 'y' && ( char ) response != 'Y' ) {
            throw new IncorrectVersion();
        }
    }

    //////////////////////
    // WordNet Updating //
    //////////////////////

    /**
     * xxx
     *
     * @see vn.WordNetUpdater#main(String[])
     */
    private static void updateWordNetSenses() throws InvalidPathException
    {
        //Translator.translate( xmlFiles, wnMono, wnPoly, vnOut );
        WordNetAnalyzer analyze = new WordNetAnalyzer(vnIn, vnOut, wnMono, wnPoly);
        analyze.update();

        if( flNewXML )
        {
            File dtdSource = new File( vnIn, "vn_class-3.dtd" );
            File xsdSource = new File( vnIn, "vn_schema-3.xsd" );

            if( !dtdSource.exists() ) {
                throw new InvalidPathException( "DTD file does not exist in XML input directory.  Cannot copy."     );
            }
            if( !dtdSource.isFile() ) {
                throw new InvalidPathException( "DTD file is not a regular file." );
            }
            if( !dtdSource.canRead() ) {
                throw new InvalidPathException( "DTD file not readable." );
            }

            if( !xsdSource.exists() ) {
                throw new InvalidPathException( "XSD file does not exist in XML input directory.  Cannot copy."     );
            }
            if( !xsdSource.isFile() ) {
                throw new InvalidPathException( "XSD file is not a regular file." );
            }
            if( !xsdSource.canRead() ) {
                throw new InvalidPathException( "XSD file not readable." );
            }

            File dtdTarget = new File( vnOut, "vn_class-3.dtd" );
            File xsdTarget = new File( vnOut, "vn_schema-3.xsd" );

            copyFile( dtdSource, dtdTarget );
            copyFile( xsdSource, xsdTarget );
        }
    }

    /**
     * Performs a low-level copy of one file into another.
     *
     * @param src  the source file
     * @param dest the destination file
     */
    private static void copyFile( File src, File dest )
    {
        try
        {
            if( dest.exists() && !flOverwrite )
            {
                err.println( "ERROR: Output file \"" + dest.getName() + "\" already exists and overwrite not specified, skipping." );
                return;
            }

            FileChannel sourceChannel      = new FileInputStream( src ).getChannel();
            FileChannel destinationChannel = new FileOutputStream( dest ).getChannel();

            sourceChannel.transferTo( 0, sourceChannel.size(), destinationChannel );

            sourceChannel.close();
            destinationChannel.close();
        }
        catch( Exception e )
        {
            err.println( "ERROR: Problem copying image file  \"" + src.getName() +
                "\" to output directory.  " + e.getMessage() );
        }
    }

    //////////////////////////
    // Supplemental Classes //
    //////////////////////////

    /**
     * Exception class for identifying when the user did not supply a
     * command-line of the proper format.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#main(String[])
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
     * Exception class for identifying when the user did not supply
     * a valid XML input directory, a valid WordNet sense mapping
     * directory, or a valid HTML output directory.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     * @see vn.WordNetUpdater#main(String[])
     */
    private static class InvalidPathException extends IOException
    {

        /**
         * Constructs the exception object with the given message.
         *
         * @param message the text of the exception
         */
        public InvalidPathException( String message )
        {
            super( message );
        }
    }

    /**
     * Exception class for identifying when the user requests to
     * view the help/usage message for the program.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
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
     * Exception class for identifying when the user has been
     * shown the WN versions located in the mapping file
     * but he/she does not wish to continue.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
     */
    private static class IncorrectVersion extends Exception
    {

        /**
         * Constructs the exception object.
         */
        public IncorrectVersion()
        {
            super();
        }
    }

    /**
     * Exception class for identifying that some of the
     * VerbNet XML files contained invalid XML syntax
     * that would break the manual parsing in the
     * Translator class.
     *
     * @see vn.WordNetUpdater#verifyXMLSyntax()
     */
    private static class BadXMLException extends Exception
    {

        /**
         * Constructs the exception object.
         */
        public BadXMLException( String message )
        {
            super( message );
        }
    }

    /**
     * Decides which files to select for the {@link java.io.File#listFiles()}
     * method of the {@link java.io.File} class.
     *
     * @see vn.WordNetUpdater#analyzeArguments(String[])
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
}

