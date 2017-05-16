
//////////////////
// XML Parser   //
// Derek Trumbo //
//////////////////

package semlink.util;

import java.io.File;
import java.io.FileNotFoundException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import slflixer.cc.util.mm.MembershipMap;

/**
 * This class parses a set of XML files and provides the XML {@link org.w3c.dom.Document}
 * object for each one, along with whether or not there was an error for each one.  It also
 * provides a convenience flag determining whether there was at least one parsing error
 * with the entire set of files.  Warning information is printed but whether or not a file
 * contained warnings is not queryable after the parsing.  This is because this class was
 * made specifically to catch structural errors that could hinder manual parsing of the files
 * (and warnings are not a concern in this).  All output from this class is sent to stderr.
 *
 * @author Derek Trumbo
 */
public class Parser
{

    ////////////
    // Fields //
    ////////////

    /**
     * A temporary boolean used for communicating between the parsing code
     * and the XML error handling object.  The error handler sets this
     * value to <CODE>true</CODE> if it encountered any error on the current file.
     *
     * @see vn.Parser.XMLErrorHandler
     */
    private static boolean tempError;

    /**
     * An array of XML {@link org.w3c.dom.Document} objects that correspond to the
     * parsed structure of the provided XML file array.  The array is the same size
     * and in the same order as the file array given to the {@link vn.Parser#parse(File[])}
     * method.
     *
     * @see vn.Parser#parse(File[])
     */
    private static Document[] documents;

    /**
     * An array of boolean values corresponding to whether there was an error
     * associated with the parsing of each XML file.  The array is the same size
     * and in the same order as the file array given to the {@link vn.Parser#parse(File[])}
     * method.
     *
     * @see vn.Parser#parse(File[])
     */
    private static boolean[] errors;
    public static MembershipMap<String, String> errorInfo = new MembershipMap<String, String>();

    /**
     * Whether or not there was at least one error encountered during the parsing of
     * the entire set of provided XML files.  This file will only be <CODE>false</CODE> if none
     * of the files had a parse error.  It is a convenience variable since this
     * information could be extracted from the {@link vn.Parser#errors} array.
     *
     * @see vn.Parser#parse(File[])
     */
    private static boolean overallError;

    /////////////
    // Methods //
    /////////////

    /**
     * Returns the XML {@link org.w3c.dom.Document} object corresponding to an
     * XML file that was parsed in the {@link vn.Parser#parse(File[])} method.
     *
     * @param index which file's XML {@link org.w3c.dom.Document} object to return
     */
    public static Document getDocument( int index )
    {
        return documents[ index ];
    }

    /**
     * Returns whether or not there was an error encountered parsing a given
     * XML file that was parsed in the {@link vn.Parser#parse(File[])} method.
     *
     * @param index which file's error status to return
     */
    public static boolean hasError( int index )
    {
        return errors[ index ];
    }

    /**
     * Returns whether or not there was at least one error with the entire
     * set of XML files parsed.
     */
    public static boolean isOverallError()
    {
        return overallError;
    }

    /**
     * Parses a single file.  This method simply packages the single object into an
     * array for use in the {@link vn.Parser#parse(File[])} method.
     *
     * @see vn.Parser#parse(File[])
     * @param xmlFile the XML file to parse
     * @return <CODE>true</CODE> if there were no errors with the file, and
     *         <CODE>false</CODE> if there was at least one error
     */
    public static boolean parse( File xmlFile )
    {
        return parse( new File[] { xmlFile } );
    }

    /**
     * Parses a set of XML files.  This method records the XML {@link org.w3c.dom.Document}
     * objects extracted during the parse and whether there was a parse error with
     * each file.  This method assumes the files it is given are XML files already.
     * The method returns true if no errors were encountered during the parse.  Warnings
     * are not errors and do not affect the return value of this method.  All output
     * from this method is sent to stderr.
     *
     * @param xmlFiles the XML files to parse.
     * @return <CODE>true</CODE> if there were no errors with any of the files, and
     *         <CODE>false</CODE> if there was at least one error
     */
    public static boolean parse( File[] xmlFiles )
    {
        String xmlFileName = null;

        // Handle a null reference.
        if( xmlFiles == null )
        {
            documents = null;
            errors = null;
            overallError = false;
            return true;               // No error.
        }

        // Initialize variables.
        documents = new Document[ xmlFiles.length ];
        errors = new boolean [ xmlFiles.length ];
        overallError = false;

        try
        {

            // Create essential JDOM objects.
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            // So XML files are checked against their DTD.
            dbf.setValidating( true );

            DocumentBuilder db = dbf.newDocumentBuilder();

            XMLErrorHandler xmlErrH = new XMLErrorHandler();

            // So custom error messages can be printed (see XMLErrorHandler).
            db.setErrorHandler( xmlErrH );

            System.out.print( "         ");
            ProgressManager.reset();
            for( int x = 0; x < xmlFiles.length; x++ )
            {
                ProgressManager.next(x, xmlFiles.length);
                xmlFileName = xmlFiles[ x ].getName();

                // Let the JDOM XML error handler know what input file is currently
                // being processed so it can report which file contained the error.
                xmlErrH.setActiveFile( xmlFileName );

                // Initialize the temporary error flag that may get updated by the
                // error handler if it encounters an error.
                tempError = false;

                try
                {

                    // Parse the XML source file.  This will print all the errors with validating
                    // the XML file against the DTD specified inside.  Save the document into the array.
                    /*documents[ x ] =*/ db.parse( xmlFiles[ x ] );

                    // Don't save documents for memory concerns.
                }

                // This exception could occur if the DTD file is not present in the
                // same directory as the XML file being parsed.
                catch( FileNotFoundException fnfe )
                {
                    tempError = true;
                    errorInfo.addMembership("ERROR: " + fnfe.getMessage() + ".\n   Most likely the file \"" + xmlFileName + "\" references the missing file above.", xmlFileName);
                }
                catch( SAXException se )
                {
                    // do nothing - should be caught by error handler's "fatalError" method.
                }

                // Save the error status for this file.
                errors[ x ] = tempError;

                // Update overall error.
                overallError = overallError || tempError;
            }
            ProgressManager.finish();
        }
        catch( ParserConfigurationException e )
        {
            overallError = true;
            errorInfo.addMembership("PARSER ERROR: Parser configuration error.", "GLOBAL");
        }
        catch( Exception e )
        {
            overallError = true;
            errorInfo.addMembership("PARSER ERROR: [" + xmlFileName + "] " + e.getMessage() + ".", "GLOBAL");
        }

        return !overallError;
    }

    //////////////////////////
    // Supplemental Classes //
    //////////////////////////

    /**
     * Contains the methods called by the {@link javax.xml.parsers.DocumentBuilder} object
     * in {@link vn.Parser#parse(File[])} when an error is encountered with an XML
     * file during the parsing thereof.  The DB object is given an instance of this class
     * and it calls the appropriate method if it encounters a problem.  This object is
     * constantly being given the name of the current XML file being processed so it
     * can provide that information along with the error text.  It also will set a
     * class-level boolean if an error was encountered.
     *
     * @see vn.Parser#parse(File[])
     * @see vn.Parser#tempError
     */
    private static class XMLErrorHandler implements ErrorHandler
    {

        /**
         * The current XML file being processed in the parser.
         */
        private static String activeFile = "";

        /**
         * Constructs a new error handler.
         */
        XMLErrorHandler() {}

        /**
         * Sets the new active file being processed.
         *
         * @param newFile the file currently being processed
         * @see vn.Parser#parse(File[])
         */
        void setActiveFile( String newFile )
        {
            activeFile = newFile;
        }

        /**
         * Shows any error, along with file name and line number, found by the parser.
         * Signals there was an error to the {@link vn.Parser} class.
         *
         * @param spe the exception generated by the parser
         * @see vn.Parser#tempError
         */
        public void error( SAXParseException spe )
        {
            tempError = true;
            errorInfo.addMembership("ERROR: [" + activeFile + "/Line " + spe.getLineNumber() + "] " + spe.getMessage(), activeFile);
        }

        /**
         * Shows any fatal error, along with file name and line number, found by the parser.
         * Signals there was an error to the {@link vn.Parser} class.
         *
         * @param spe the exception generated by the parser
         * @see vn.Parser#tempError
         */
        public void fatalError( SAXParseException spe )
        {
            tempError = true;
            errorInfo.addMembership("FATAL ERROR: [" + activeFile + "/Line " + spe.getLineNumber() + "] " + spe.getMessage(), activeFile);
        }

        /**
         * Shows any warning, along with file name and line number, found by the parser.
         *
         * @param spe the exception generated by the parser
         */
        public void warning( SAXParseException spe )
        {
            errorInfo.addMembership("WARNING: [" + activeFile + "/Line " + spe.getLineNumber() + "] " + spe.getMessage(), activeFile);
        }
    }
}

