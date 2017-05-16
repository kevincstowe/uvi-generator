<?php

////////////////////////
// Unified Verb Index //
////////////////////////

// Construct the current date in the format "YYYY.[M]M.[D]D".
function getMyDate()
{
   date_default_timezone_set('America/Denver');
   $yy = date( "Y" );
   $mm = date( "m" );
   $dd = date( "d" );

   if( substr( $mm, 0, 1 ) == "0" )  $mm = substr( $mm, 1, 1 );
   if( substr( $dd, 0, 1 ) == "0" )  $dd = substr( $dd, 1, 1 );

   return $yy . "." . $mm . "." . $dd;
}

// Grab all relevant post and session data.
$file  = $_POST[ "txtFileName"  ];
$class = $_POST[ "txtClassName" ];
$uHTML = $_POST[ "txtUseHTML"   ];
$suser = $_SESSION[ "USER" ];

if( get_magic_quotes_gpc() == 1 )
   $comment = trim( stripslashes( $_POST[ "txtComment" ] ) );
else
   $comment = trim( $_POST[ "txtComment" ] );

// If the user has not been logged in yet, eject them to the login page.
if( $suser == "" )
{
   ?>
      <HTML><BODY>
         <FORM name='frmLogin' method='post' action='login.php'>
            <INPUT type='hidden' name='txtFileNameFrom' value='<?php print "postcomment.php"; ?>'>
            <INPUT type='hidden' name='txtFileNameTo' value='<?php print $file; ?>'>
            <INPUT type='hidden' name='txtClassName' value='<?php print $class; ?>'>
         </FORM>
      </BODY></HTML>
      <SCRIPT language='JavaScript'>document.frmLogin.submit();</SCRIPT>
   <?php

   exit;
}

// If we have a good user, and a valid comment has been provided, write
// the comment to the correct file.
if( $comment != "" && $file != "" && $class != "" )
{
   $useHTML = ( $uHTML != "" );
   $newComment = "";

   // Transform a string (the comment) with carriage returns and
   // line feeds into one with <BR> tags.
   for( $i = 0; $i < strlen( $comment ); $i++ )
   {
      $ch = substr( $comment, $i, 1 );

      // If the current character is a CR or LF...
      if( $ch == "\n" || $ch == "\r" )
      {

         // If the user wants HTML, turn every CRLF into
         // a space (just like the interpretation of basic HTML).
         if( $useHTML )
            $newComment .= " ";

         // Else if HTML is not desired, turn every CRLF into
         // an HTML break so it looks right when displayed in the
         // browser.
         else
            $newComment .= "<BR>";

         // If we're not at the end of the string, check the
         // next character for the carriage return (ASCII 13).
         // This would happen if the browser puts both on the
         // TEXTAREA value.
         if( $i != strlen( $comment ) - 1 )
         {
            $nch = substr( $comment, $i + 1, 1 );

            // Just ignore following CR or LF (for Windows this is
            // important in the \r\n pair).
            if( $nch == "\r" || $nch == "\n" )
               $i++;
         }
      }

      // Else if the current character is an open caret and
      // the user doesn't want HTML, then turn the character
      // into an HTML entity.
      elseif( $ch == "<" && !$useHTML )
         $newComment .= "&lt;";

      // Else just add all other characters straight away.
      else
         $newComment .= $ch;
   }

   $comment = $newComment;

   // Construct comments file name.
   $fileName = 'comments/';
   $fileName .= $class;
   $fileName .= '.c';

   // Set the all comments file name.
   // This file holds all comments ever created on the system,
   // just in case a malicious user or hacker does get in and delete
   // all the comments in the normal files.  This is a backup.
   // The PHP front end does not provide for deletion from this
   // file on any screen.
   $allFileName = 'comments/all-comments-copy.backup';

   // Open the file for append mode, without printing any error
   // messages associated with this opening (@).
   $cFile = @fopen( $fileName, "a+" );

   if( $cFile != "" )
   {

      // Write the info for this comment.
      fwrite( $cFile, $file       . "\n" );
      fwrite( $cFile, getMyDate() . "\n" );
      fwrite( $cFile, $suser      . "\n" );
      fwrite( $cFile, $comment    . "\n" );

      fclose( $cFile );

      // Open the all file for append mode, without printing any error
      // messages associated with this opening (@).
      $aFile = @fopen( $allFileName, "a+" );

      // Write the info for this comment.
      fwrite( $aFile, "--> Instructions: Place the following 4 lines into file '" . $class . ".c' in the comments/ directory.\n" );
      fwrite( $aFile, $file       . "\n" );
      fwrite( $aFile, getMyDate() . "\n" );
      fwrite( $aFile, $suser      . "\n" );
      fwrite( $aFile, $comment    . "\n" );

      fclose( $aFile );

      // Return to the file and class for which they added the comment.
      // This will either be a VerbNet class page, or an index page.
      print "<SCRIPT language='JavaScript'>document.location='$file#$class" . "-comments';</SCRIPT>";

      exit;
   }
   else
   {
      ?>
      <TABLE cellpadding=3 align='center'><TR><TD>
         <TABLE class='SearchError' cellpadding=10 align='center'>
            <TR><TD>Error: Could not create comment file(s).<TD></TR>
         </TABLE>
      </TD></TR></TABLE>
      <?php
   }
}

// Else if they haven't yet submitted a comment, but they
// did come to this page via the correct method (by clicking
// a 'Post Comment' link).
elseif( $file != "" && $class != "" )
{

   // Decide which label to put next to the class/subclass/generic name.
   if( $class == "generic-comments" )
   {
      $group = "Group";
      $show = "generic comments";
   }
   else
   {
      $group = "Class/Subclass";
      $show = $class;
   }
?>
      <BR>
      <BR>

      <FORM name='frmPostComment' method='post' onSubmit='return checkCommentAndConfirm();'>

         <TABLE align='center' cellspacing=0 cellpadding=7 class='ClassSectionBox'>
            <TR class='ClassSectionHeadRow'>
               <TD><?php print $group; ?></TD><TD><?php print $show; ?></TD>
            </TR>
            <TR>
               <TD>Name</TD><TD><?php print $suser ?></TD>
            </TR>
            <TR>
               <TD>Date</TD><TD><?php print getMyDate(); ?></TD>
            </TR>
            <TR valign='top'>
               <TD width='130px'>
                  Comment<BR><BR><FONT class='Small'>You may use HTML markup.  Just make sure it is <NOBR>well-formed!</NOBR>
                  By checking this box you are also responsible to add &lt;BR> tags for all the line breaks you want or your
                  comment may not appear as you desire.</FONT><BR><BR>
                  <INPUT type='checkbox' name='txtUseHTML'> <FONT class='Small'>Interpret my <NOBR>comment as HTML</NOBR></FONT>
               </TD>
               <TD width='400px'><TEXTAREA name='txtComment' rows=20 cols=60></TEXTAREA></TD>
            </TR>
            <TR class='ClassSectionHeadRow'>
               <TD colspan=2 align='center'>
                  <INPUT type='submit' value='Post!'>&nbsp;
                  <INPUT type='button' value='Cancel' onClick='document.location="<?php print $file ?>";'>
               </TD>
            </TR>
         </TABLE>

         <INPUT type='hidden' name='txtFileName' value='<?php print $file; ?>'>
         <INPUT type='hidden' name='txtClassName' value='<?php print $class; ?>'>
      </FORM>

      <!-- Start the focus in the comment box. -->
      <SCRIPT language='JavaScript'>document.frmPostComment.txtComment.focus();</SCRIPT>

      <BR>
<?php
}

// Else if this page is being visited, but the proper post data
// was not supplied (like if they visited via a favorite link),
// print a warning message.
else
{
?>
      <BR>
      <TABLE align='center' class='WarningTable' cellspacing=0 cellpadding=4>
         <TR><TD>Invalid post parameters.  Please visit this page from a VerbNet class page.</TD></TR>
      </TABLE>
      <BR>
<?php
}
?>
