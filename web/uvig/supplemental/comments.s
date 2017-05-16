<?php

////////////////////////
// Unified Verb Index //
////////////////////////

// Grab whether or not this page is to be used to delete
// comments as well as view them (a.k.a. management mode).
$manage = $_POST[ "txtForManage" ];

// If they are attempting to view this page for management mode...
if( $manage == 1 )
{

   // If either the user is not set, or the fact that they are an admin
   // is not set, redirect them to the login page.
   if( $_SESSION[ "USER" ] == "" || $_SESSION[ "ADMIN" ] != 1 )
   {
      print "<SCRIPT language='JavaScript'>document.location='login.php';</SCRIPT>";
      exit;
   }
}

// Grab the delete information.
$class = $_POST[ "txtDeleteCls" ];
$num   = $_POST[ "txtDeleteNum" ];

// If the user has requested to delete a comment...
if( $class != "" && $num != "" )
{

   // Construct comments file name.
   $fileName = 'comments/';
   $fileName .= $class;
   $fileName .= '.c';

   // Use the 'file' function to grab all the lines from the file.
   $lines = file( $fileName );

   // Open the file for append mode.
   $cFile = fopen( $fileName, "w" );

   // Initialize a counter representing the type of data currently
   // being processed (1=File, 2=Date, 3=User, 4=Body).
   $data  = 1;

   // Initialize the comment counter.
   $cm = 1;

   // Initialize a flag saying whether or not any lines
   // were written back to the comments file.
   $linesWritten = 0;

   // Write the non-deleted comments back to the file.
   foreach( $lines as $line )
   {

      // Write this line if it's not associated with the comment
      // that will be deleted (by not writing the lines associated
      // with the comment to-be-deleted back to the file, you are
      // in essence deleting it).
      if( $cm != $num )
      {
         $linesWritten = 1;
         fwrite( $cFile, $line );
      }

      $data++;

      // If all pieces of information for this comment have been
      // processed, reset and increment comment counter.
      if( $data == 5 )
      {
         $data = 1;
         $cm++;
      }
   }

   // If there were no lines written back to the file,
   // then delete the file so as not to leave any
   // "orphan" files.
   if( $linesWritten == 0 )
      unlink( $fileName );

   fclose( $cFile );
}

// Look at the comments directory and print all the comments.
if( $dh = opendir( "comments/" ) )
{
   $count = 0;          // Init counter.

   // Load up all comment file names (without '.c') into an array.
   while( ( $file = readdir( $dh ) ) !== false )
      if( substr( $file, strlen( $file ) - 2 ) == ".c" )
      {

         $cFiles[ $count++ ] = substr( $file, 0, strlen( $file ) - 2 );

         // Make sure the generic
         if( $file == "generic-comments.c" && $count > 1 )
         {
            $temp = $cFiles[ 0 ];
            $cFiles[ 0 ] = $cFiles[ $count - 1 ];
            $cFiles[ $count - 1 ] = $temp;
         }
      }

   // Close the directory.
   closedir( $dh );

   // Assume no output will be generated.
   $someOutput = false;

   // If there are files to process...
   if( $count != 0 )
   {

      // Sort file names (because the OS will return them in an arbitrary
      // order.
      for( $a = 0; $a < $count - 1; $a++ )
         for( $b = $a + 1; $b < $count; $b++ )
            if( strcmp( $cFiles[ $a ], $cFiles[ $b ] ) > 0 && $cFiles[ $a ] != "generic-comments" )
            {
               $temp = $cFiles[ $a ];
               $cFiles[ $a ] = $cFiles[ $b ];
               $cFiles[ $b ] = $temp;
            }

      // Display handy jump links to comment box.
      ?>
      <BR>

      <TABLE align='center' class='CommentList' cellpadding=5>
         <TR><TD>The following classes/subclasses/groups have comments:
<?php
      print "            <UL>\n";

      $totalComments = 0;

      for( $c = 0; $c < $count; $c++ )
      {

         // Construct the path to the comment file.
         $fileName = 'comments/';
         $fileName .= $cFiles[ $c ];
         $fileName .= '.c';

         // Count the number of lines in the file and divide by the number
         // of lines per comment to get the number of comments in the file.
         $numComments = count( file( $fileName ) ) / 4;

         // Accumulate the total number of comments.
         $totalComments = $totalComments + $numComments;

         $outp = $cFiles[ $c ] . "</A> &nbsp;<FONT class='CommentCount'>(" . $numComments . ")</FONT>";

      ?>
               <LI><A href='#<?php print $cFiles[ $c ] ?>-comments'><?php print $outp ?></LI>
<?php
      }
?>
            </UL>
         </TD></TR>
         <TR>
            <TD align='center' bgcolor='lightblue'><?php print "Total Comments: " . $totalComments; ?></TD>
         </TR>
      </TABLE>

      <BR>
<?php
      // Print the comment boxes.
      for( $c = 0; $c < $count; $c++ )
      {
         $wasOutput = printCommentBox( $cFiles[ $c ], 1, $manage, 1, '.' );
         $someOutput = $someOutput || $wasOutput;
      }
   }

   // If there was no output generated (there were no comments in the system)...
   if( !$someOutput )
   {
   ?>
      <BR>
      <TABLE align='center' class='WarningTable' cellspacing=0 cellpadding=4>
         <TR><TD>No comments to display at this time.</TD></TR>
      </TABLE>
      <BR>
   <?php
   }

   // Else if there were comments and the user is in manage mode, print the
   // form required to delete comments.
   elseif( $manage == 1 )
   {
   ?>

      <FORM name='frmComments' method='post'>
         <INPUT type='hidden' name='txtForManage' value='1'>
         <INPUT type='hidden' name='txtDeleteCls'>
         <INPUT type='hidden' name='txtDeleteNum'>
      </FORM>
   <?php
   }
}
else
{
?>
      <BR>
      <TABLE align='center' class='WarningTable' cellspacing=0 cellpadding=4>
         <TR><TD>An error occurred while attempting to access the comment directory.</TD></TR>
      </TABLE>
      <BR>
<?php
}
?>
