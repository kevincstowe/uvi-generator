
package semlink.apps.wnu;

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

import semlink.apps.VerbNetAnalyzer;
import semlink.util.XMLElementEditor;

public class WordNetAnalyzer extends VerbNetAnalyzer {

    private static int vnTotal        = 0;  // Total VN WSK
    private static int vnUnique       = 0;  // Unique VN WSK
    private static int vnQuestion     = 0;  // VN WSK with preceding question mark (?)

    private static int wnMono         = 0;  // WSK translations in mono file (# lines in file)
    private static int wnMonoSame     = 0;  // WSK that didn't change in mono file
    private static int wnMonoDiff     = 0;  // WSK that changed in the mono file
    private static int wnPoly         = 0;  // WSK translations in poly file (# lines in file)
    private static int wnPolySame     = 0;  // WSK that didn't change in poly file
    private static int wnPolyDiff     = 0;  // WSK that changed in the poly file
    private static int wnPolyMultiple = 0;  // WSK that map to multiple keys in poly file

    private static int transMono      = 0;  // VN WSK found in mono file
    private static int transPoly      = 0;  // VN WSK found in poly file
    private static int transNotFound  = 0;  // VN WSK not found in either file
    private static int transMultiple  = 0;  // VN WSK that map to multiple keys (poly only)
    private static int transPolyHigh  = 0;  // VN WSK with scores > 80 (poly only)
    private static int transPolyLow   = 0;  // VN WSK with scores <= 80 & > 0 (poly only)
    private static int transPolyZero  = 0;  // VN WSK with zero scores (poly only)

    private static int []    wnPolyScores          = new int[ 11 ];
    private static ArrayList transNotFoundList     = new ArrayList();
    private static ArrayList transPolyZeroList     = new ArrayList();
    private static ArrayList transPolyLowList      = new ArrayList();
    private static ArrayList transPolyMultipleList = new ArrayList();
    private static ArrayList transWarnings         = new ArrayList();

    private static boolean gotScores     = false;
    private static boolean gotMonoTotals = false;
    private static boolean gotPolyTotals = false;

    private static double prevPct  = 0.0;
    private static int    pctCount = 0;

    private File wnMonoFile;
    private File wnPolyFile;

    public WordNetAnalyzer(File iDir, File oDir, File mono, File poly) {
        super(iDir, oDir);
        wnMonoFile = mono;
        wnPolyFile = poly;
    }

	@Override
	public void preAnalyze() {
		// Nothing.
	}

    @Override
    public void postAnalyze() {
        printReport();
    }

    public String analyzeLine(File file, String line, int lineNum) {

        super.analyzeLine(file, line, lineNum);

        // Look for member element.
        int memStart = line.indexOf("<MEMBER ");

        // If the line is a member line...
        if(memStart != -1) {
            String content = XMLElementEditor.getValueOfAttribute(line, "wn");
            if(!content.equals("")) {
            	try {
            		content = translateSenseKeys(content, wnMonoFile, wnPolyFile);
            	} catch(Exception e) {
            		e.printStackTrace();
            		System.exit(1);
            	}
                line = XMLElementEditor.replaceValueOfAttribute(line, "wn", content);
            }
        }
        
        return line;
    }
    
    public void handleException(File file, Exception e) {
        e.printStackTrace();
    }

    /**
     * xxx
     *
     * @param content xxx
     * @param wnMonoFile xxx
     * @param wnPolyFile xxx
     */
    private String translateSenseKeys( String content, File wnMonoFile, File wnPolyFile ) throws Exception
    {
       String[] keys = content.split( "  *" );
       String newContent = "";

       for( int k = 0; k < keys.length; k++ )
       {
          String thisKey = keys[ k ];
          String leftChar;

          if( thisKey.charAt( 0 ) == '?' )
          {
             thisKey = thisKey.substring( 1 );
             vnQuestion++;

             leftChar = "?";
          }
          else
             leftChar = "";

          String[] translatedKeys = translateSenseKey( thisKey, wnMonoFile, wnPolyFile ).split( " " );

          for( int t = 0; t < translatedKeys.length; t++ )
          {
             String thisTKey = translatedKeys[ t ];

             if( thisTKey.equals( "" ) )
                continue;

             thisTKey = leftChar + thisTKey;

             if( newContent.indexOf( " " + thisTKey ) == -1 )
                newContent += " " + thisTKey;

             if( thisTKey.startsWith( "?" ) && newContent.indexOf( " " + thisTKey.substring( 1 ) ) != -1 )
                transWarnings.add( "Both \"" + thisTKey + "\" and \"" + thisTKey.substring( 1 ) + "\" exist. [" + curClass + "]" );
          }
       }

       return newContent.trim();
    }

    /**
     * xxx
     *
     * @param key xxx
     * @param wnMonoFile xxx
     * @param wnPolyFile xxx
     */
    private String translateSenseKey( String key, File wnMonoFile, File wnPolyFile ) throws Exception
    {
       BufferedReader in;
       String line;
       String translatedKey = "";
       boolean foundm = false;
       boolean foundp = false;

       vnTotal++;

       if( !foundm )
       {
          in = new BufferedReader( new FileReader( wnPolyFile ) );

          while( ( line = in.readLine() ) != null )
          {
             int firstSpace  = line.indexOf( " " );
             int secondSpace = line.indexOf( " ", firstSpace + 1 );
             int lastSpace   = line.lastIndexOf( " " );
             boolean multiple = ( secondSpace != -1 && lastSpace != secondSpace );

             int sc = Integer.parseInt( line.substring( 0, firstSpace ) );

             if( !gotPolyTotals && !gotScores )
                wnPolyScores[ sc / 10 ]++;

             String k1 = line.substring( firstSpace + 1, line.indexOf( ";" ) - 2 );
             String k2 = line.substring( secondSpace + 1 );

             if( !gotPolyTotals )
             {
                if( multiple )
                   wnPolyMultiple++;

                wnPoly++;

                if( !multiple && k1.equals( k2.substring( 0, k2.indexOf( ";" ) - 2 ) ) )
                   wnPolySame++;
                else
                   wnPolyDiff++;
             }


             if( key.equals( k1 ) )
             {

                transPoly++;
                foundp = true;

                String[] k2all = k2.split( " " );
                String replace = "";

                if( sc != 0 )
                {
                   for( int a = 0; a < k2all.length; a++ )
                      replace += k2all[ a ].substring( 0, k2all[ a ].indexOf( ";" ) - 2 ) + " ";
                   replace = replace.trim();
                }

                translatedKey = replace;

                if( multiple )
                {
                   transMultiple++;
                   transPolyMultipleList.add( key + " -> " + translatedKey + " [" + curClass + "]" );
                }

                if( sc >= 90 )
                   transPolyHigh++;

                else if( sc >= 10 )
                {
                   transPolyLow++;
                   transPolyLowList.add( key + " -> " + translatedKey + " [" + curClass + "]" );
                }
                else
                {
                   transPolyZero++;
                   transPolyZeroList.add( key + " [" + curClass + "]" );
                }

                if( gotPolyTotals )
                   break;
             }
          }

          in.close();

          gotPolyTotals = true;
          gotScores = true;
       }

       if( !foundp )
       {
          in = new BufferedReader( new FileReader( wnMonoFile ) );

          while( ( line = in.readLine() ) != null )
          {
             int firstSpace  = line.indexOf( " " );
             int secondSpace = line.indexOf( " ", firstSpace + 1 );
             int lastSpace   = line.lastIndexOf( " " );

             String k1 = line.substring( 0, firstSpace - 2 );
             String k2 = line.substring( secondSpace + 1, lastSpace - 2 );

             if( !gotMonoTotals )
             {
                wnMono++;

                if( k1.equals( k2 ) )
                   wnMonoSame++;
                else
                   wnMonoDiff++;
             }

             if( key.equals( k1 ) )
             {
                foundm = true;
                translatedKey = k2;
                transMono++;
                if( gotMonoTotals )
                   break;
             }
          }

          gotMonoTotals = true;

          in.close();
       }

       if( !foundm && !foundp )
       {
          transNotFound++;
          transNotFoundList.add( key + " [" + curClass + "]" );

          translatedKey = key;
       }

       return translatedKey;
    }

    public void printReport()
    {
       out.println( "" );
       out.println( "<<< Report Begin >>>" );
       out.println( "" );
       out.println( "(WNSK = WordNet Sense Key)" );
       out.println( "" );
       out.println( "1. VerbNet Statistics Only" );
       out.println( "------------------------------------------------------------" );
       out.println( "| This section describes statistics about just VerbNet and |" );
       out.println( "| its WordNet sense keys.                                  |" );
       out.println( "------------------------------------------------------------" );
       out.printf( "1.A. Total VN WNSK                                    %5d%n", vnTotal );
       out.printf( "1.B. Unique VN WNSK                                   %5d%n", vnUnique );
       out.printf( "1.C. VN WNSK with uncertainty (preceded by ?)         %5d%n", vnQuestion );
       out.println( "" );
       out.println( "" );

       out.println( "2. WordNet Sense Mapping Statistics Only" );
       out.println( "------------------------------------------------------------" );
       out.println( "| This section describes statistics about just the         |" );
       out.println( "| *.verb.mono and *.verb.poly files.                       |" );
       out.println( "------------------------------------------------------------" );
       out.printf( "2.A. WNSK mappings in MONO file (lines in file)       %5d%n", wnMono );
       out.printf( "2.B. WNSK that did not change in MONO file            %5d%n", wnMonoSame );
       out.printf( "2.C. WNSK that did change in MONO file                %5d%n", wnMonoDiff );
       out.printf( "2.D. WNSK mappings in POLY file (lines in file)       %5d%n", wnPoly );
       out.printf( "2.E. WNSK that did not change in POLY file            %5d%n", wnPolySame );
       out.printf( "2.F. WNSK that did change in POLY file                %5d%n", wnPolyDiff );
       out.printf( "2.G. WNSK that map to multiple in POLY file           %5d%n", wnPolyMultiple );
       out.println( "2.H. Score distribution in POLY file" );

       int total = 0;

       out.printf( "       Score | Count | %% Total%n" );
       for( int s = 10; s >= 0; s-- )
       {

          double pct;

          if( wnPoly != 0 )
             pct = wnPolyScores[ s ] * 100.0 / wnPoly;
          else
             pct = 0.0;

          out.printf( "         %3d | %5d | %6.2f%%%n", s * 10, wnPolyScores[ s ], pct );
          total += wnPolyScores[ s ];
       }
       out.printf( "       Total | %5d  *same as D above%n", total );

       out.println( "" );
       out.println( "" );
       out.println( "3. Translation Statistics" );
       out.println( "------------------------------------------------------------" );
       out.println( "| This section describes statistics concerning the         |" );
       out.println( "| translation of VerbNet's WordNet keys from one version   |" );
       out.println( "| of WordNet to another.                                   |" );
       out.println( "------------------------------------------------------------" );
       out.printf( "3.A. VN WNSK found in MONO file                       %5d%n", transMono );
       out.printf( "3.B. VN WNSK found in POLY file                       %5d%n", transPoly );
       out.printf( "3.C. VN WNSK not found in either file                 %5d%n", transNotFound );
       out.printf( "3.D. VN WNSK that map to multiple in POLY file        %5d%n", transMultiple );
       out.printf( "3.E. VN WNSK mappings in POLY file with high score    %5d%n", transPolyHigh );
       out.printf( "        (score >= 90)%n" );
       out.printf( "3.F. VN WNSK mappings in POLY file with low score     %5d%n", transPolyLow );
       out.printf( "        (90 > score >= 10)%n" );
       out.printf( "3.G. VN WNSK mappings in POLY file with zero score    %5d%n", transPolyZero );
       out.printf( "        (10 > score = 0)%n" );
       out.println( "" );
       out.println( "" );
       out.println( "4. Translation Lists" );
       out.println( "-------------------------------------------------------------" );
       out.println( "| This section contains the specific mappings from the      |" );
       out.println( "| translation process that require inspection.              |" );
       out.println( "-------------------------------------------------------------" );

       printList( "4.A. Not Found (3.C from above)",  transNotFoundList );
       printList( "4.B. Multiple (3.D from above)",   transPolyMultipleList );
       printList( "4.C. Low Score (3.F from above)",  transPolyLowList );
       printList( "4.D. Zero Score (3.G from above)", transPolyZeroList );
       printList( "4.E. Other Warnings",              transWarnings );

       out.println( "" );
       out.println( "<<< Report End >>>" );
    }

    /**
     * xxx
     *
     * @param title xxx
     * @param list xxx
     */
    private void printList( String title, ArrayList list )
    {
       out.println( title + " [total items: " + list.size() + "]" );

       if( list.size() == 0 )
          out.println( " [this list empty]" );

       else
          for( int k = 0; k < list.size(); k++ )
             out.println( " - " + list.get( k ) );
    }
}
