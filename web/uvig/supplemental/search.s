<?php

////////////////////////
// Unified Verb Index //
////////////////////////


$search = trim( strtolower( $_POST[ "txtSearchRequest" ] ) );

$errStr = "";
	
//Checks POST for real search, then GET for HTML search	
if (strlen( $search ) == 0)
{	
    $errStr = "No search request (POST)";
    $search = trim( strtolower( $_GET[ "txtSearchRequest" ] ) );
    if (strlen( $search ) == 0)
	$errStr = "No search request (GET)." ;
    else
	//Everything's good
	$errStr = "";
}
	
if ($errStr != "")
    echo $errStr;
// Else validate the string.
else
{
   $goodChars = "abcdefghijklmnopqrstuvwxyz_-, ";

   // Make sure every character in the user's input is a valid
   // character, otherwise set an error string.
   for( $i = 0; $i < strlen( $search ); $i++ )
   {
      $ch = substr( $search, $i, 1 );

      if( strpos( $goodChars, $ch ) === false )
      {
         $errStr = "Invalid character in search request: '" . $ch . "'";
         break;
      }
   }
}

// Print the search request token and search again box, regardless
// of error state.
printSearchHeader( $search );

// If there was no error with the user's string, search for the token(s).
if( $errStr == "" )
{

   // Break the search request apart on the commas.
   $allWords = explode( ',', $search );

   // Print results for each of user's requested words.
   for( $w = 0; $w < count( $allWords ); $w++ )
   {
      $searchWord = trim( $allWords[ $w ], " " );

      // Replace all spaces with underscores (so the user can type
      // 'chicken out' instead of 'chicken_out').
      $searchWord = str_replace( " ", "_", $searchWord );

      // If there is more than one word requested, show word bars to delineate.
      if( count( $allWords ) > 1 )
      {

         // Print extra space before preceding word.
         if( $w != 0 )
            print "      <BR>\n\n";

         print "      <TABLE width='90%' align='center' cellpadding=0 cellspacing=0><TR><TD align='center' class='SearchWord'>" .
            $searchWord . "</TD></TR></TABLE>\n\n";
      }

      // Make sure it is at least two characters.
      if( strlen( $searchWord ) == 1 )
      {
         printError( "Please enter at least two characters.  Use index pages for a single letter." );

         continue;   // Jump to next word in list.
      }

      // Grab the first character of their input.
      $fl = substr( $searchWord, 0, 1 );

      // Construct appropriate file name.
      $fileName = 'search/search-index-' . strtoupper( $fl );

      // Initialize match counters (one for each of the sources).
      $vc = 0;   $pc = 0;
      $fc = 0;   $cc = 0;
      $gc = 0;   $rc = 0;
      $prc = 0;         /// TRUMBO ADDED 2007.10.8 ///

      // Open the searchable index file for read mode, without printing any error
      // messages associated with this opening (@).
      $iFile = @fopen( $fileName, 'r' );

      // If the file exists, check to see if there is at
      // least one line in the file.  This means that the
      // file has at least one line.
      if( $iFile )
      {

         // Read in first line (and strip CR/LF).
         $line = rtrim( fgets( $iFile ) );

         // If there is at least one line in the file...
         if( !feof( $iFile ) )
         {

            // Cycle through all lines in the file.
            while( !feof( $iFile ) )
            {

               // Break the line out into its constituent parts.
               list( $verbOrClass, $type, $text, $link ) = explode( " ", $line );

               // If the verb or class begins with the user's input,
               // then we have a match.  Store the line into the appropriate array.
               if( substr( $verbOrClass, 0, strlen( $searchWord ) ) == $searchWord )
               {
                  if( $type == "V" )      $vmatches[ $vc++ ] = $line;
                  elseif( $type == "P" )  $pmatches[ $pc++ ] = $line;
                  elseif( $type == "F" )  $fmatches[ $fc++ ] = $line;
                  elseif( $type == "G" )  $gmatches[ $gc++ ] = $line;  /// TRUMBO ADDED 2007.10.8 ///
                  elseif( $type == "C" )  $cmatches[ $cc++ ] = $line;
                  elseif( $type == "R" )  $rmatches[ $rc++ ] = $line;
                  elseif( $type == "PR" ) $prmatches[$prc++] = $line;
               }

               // Grab next line in file.
               $line = rtrim( fgets( $iFile ) );
            }
         }

         // Close searchable index file.
         fclose( $iFile );
      }

      // If there were no matches made, show a warning table.
      if( $vc == 0 && $pc == 0 && $fc == 0 && $gc == 0 && $cc == 0 )    /// TRUMBO CHANGED 2007.10.8 ///
      {
?>
      <TABLE cellpadding=3 align='center'><TR><TD>
         <TABLE class='WarningTable' cellpadding=10>
            <TR><TD>No Matches Found</TD></TR>
         </TABLE>
      </TD></TR></TABLE>

<?php
      }

      // Show the match tables (one for each of four sources).
      /// TRUMBO CHANGED 2007.10.8 ///

?>
      <TABLE width='90%' align='center' cellpadding=0 cellspacing=0>
         <TR valign='top'>
            <TD width='50%'>
               <TABLE width='100%' cellpadding=0 cellspacing=5>
                  <TR>
                     <TD>
                        <?php showResultTable( "VerbNet Members", "verbnet member", $vc, $vmatches ); ?>
                     </TD>
                  </TR>
                  <TR>
                     <TD>
                        <?php showResultTable( "VerbNet Classes", "verbnet class", $cc, $cmatches ); ?>
                     </TD>
                  </TR>
                  <TR>
                     <TD>
                        <?php showResultTable( "VerbNet ThemRoles", "themroles", $rc, $rmatches ); ?>
                     </TD>
                  </TR>
                  <TR>
                     <TD>
                        <?php showResultTable( "VerbNet Predicates", "predicates", $prc, $prmatches ); ?>
                     </TD>
                  </TR>
               </TABLE>
            </TD>
            <TD width='50%'>
               <TABLE width='100%' cellpadding=0 cellspacing=5>
                  <TR>
                     <TD>
                        <?php showResultTable( "PropBank", "propbank", $pc, $pmatches ); ?>
                     </TD>
                  </TR>
                  <TR>
                     <TD>
                        <?php showResultTable( "FrameNet", "framenet", $fc, $fmatches ); ?>
                     </TD>
                  </TR>
                  <TR>
                     <TD>
                        <?php showResultTable( "OntoNotes Sense Groupings", "sense grouping", $gc, $gmatches ); ?>
                     </TD>
                  </TR>
               </TABLE>
            </TD>
         <TR>
      </TABLE>

<?php
   }
}

// Else if there was an error, print the error message.
else
   printError( $errStr );

// Make sure the focus starts out in the search again box.

?>
      <BR>

      <SCRIPT language='JavaScript'>
         document.frmSearch2.txtSearchRequest.focus();
      </SCRIPT>

      </FORM>
<?php

// Print the search request token and the search again box.
function printSearchHeader( $search )
{
?>
      <FORM name='frmSearch2' method='post' action='search.php'>

      <BR>

      <CENTER>
         <FONT class='SearchRequest'>Search Request:</FONT>&nbsp;&nbsp;
         <FONT class='SearchRequestToken'>[<?php print $search; ?>]</FONT>
      </CENTER>

      <BR>

      <TABLE cellspacing=0 cellpadding=0 align='center'>
         <TR valign='bottom'>

            <TD width=15> </TD>

            <TD>

               <TABLE width=360 height=70 class='SearchTable' align='center'>
                  <TR valign='middle'>
                     <TD align='center'>
                        Search: &nbsp;<INPUT type='text' name='txtSearchRequest' class='SearchInput'>&nbsp;
                        <INPUT type='submit' name='cmdSearchGo2' value='Go!'>
                     </TD>
                  </TR>
               </TABLE>

            </TD>

            <TD align='right' width=15>
               <A href='javascript:showSearchInfo();'><IMG src='images/si.gif' width=13 height=15 alt='Search Info' border=0></A>
            </TD>

         </TR>
      </TABLE>

      <BR>

<?php
}

// Print a table with the match results for one source.
function showResultTable( $label, $noMatch, $matchCount, $data )
{
               ?><TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%' align='center'>
                           <TR class='ClassSectionHeadRow'>
                              <TD><?php print $label; ?></TD>
                           </TR>
<?php

   // Cycle through all matches for this source, printing a
   // row for each one.
   for( $x = 0; $x < $matchCount; $x++ )
   {

      // Break the line into its constituent parts.
      list( $verbOrClass, $type, $text, $link ) = explode( " ", $data[ $x ] );
?>
                           <TR>
                              <TD><NOBR><?php print $verbOrClass; ?>: <A href='<?php print $link; ?>'><?php print $text; ?></A></NOBR></TD>
                           </TR>
<?php
   }

   // If there were no matches for this source, show a message.
   if( $matchCount == 0 )
   {
?>
                           <TR>
                              <TD class='AbsenceOfItems'>no <?php print $noMatch; ?> matches</TD>
                           </TR>
<?php
   }
?>
                        </TABLE>
<?php
}

// Show the error message table.
function printError( $errStr )
{
?>
      <TABLE cellpadding=3 align='center'><TR><TD>
         <TABLE class='SearchError' cellpadding=10 align='center'>
            <TR><TD>Error: <?php print $errStr; ?></TD></TR>
         </TABLE>
      </TD></TR></TABLE>

<?php
}
?>
