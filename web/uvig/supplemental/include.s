<?php

////////////////////////
// Unified Verb Index //
////////////////////////

// If any PHP page has a value for the logout box specified,
// then remove the session variables.
if( $_POST[ "txtLogout" ] != "" )
{
   $_SESSION[ "USER"  ] = "";
   $_SESSOIN[ "ADMIN" ] = "";
}

// Returns an asterisk if the given class or subclass has
// comments associated with it.
function existCommentsStar( $class )
{
   $star = "";

   // Construct comments file name.
   $fileName = '../comments/';
   $fileName .= $class;
   $fileName .= '.c';

   // Open the file for read mode, without printing any error
   // messages associated with this opening (@).
   $cFile = @fopen( $fileName, 'r' );

   // If the file exists, check to see if there is at
   // least one line in the file.  This means that the
   // class has at least one comment.
   if( $cFile )
   {
      $line = fgets( $cFile );

      // If the file is not at its end, then there are
      // comments in the file.
      if( !feof( $cFile ) )
         $star = "*";

      fclose( $cFile );
   }

   return $star;
}

// Print the comments associated with a given class or subclass
// (or the 'generic-comments' group).  Return whether or not
// any output was generated for this comment class/group.
function printCommentBox( $class, $forViewManage, $canDelete, $showClassLink, $relDir )
{
   $wasOutput = false;           // No output yet.

   // Construct comments file name.
   $fileName = $relDir . '/comments/';
   $fileName .= $class;
   $fileName .= '.c';

   // Attempt to open the comments file.
   $cFile = @fopen( $fileName, 'r' );

   // Decide if an additional message should go in the header
   // of the comments box.  Comment boxes on the VerbNet classes
   // do not get extra header text.
   if( $forViewManage )
   {
      if( $class == "generic-comments" )
         $extra = "(GENERIC)";

      else
         $extra = "for class or subclass [" . $class . "]";
   }
   else
      $extra = "";         // VerbNet class box

   // If the comments file exists for this class/subclass/generic-group,
   // read each comment out and print it to an HTML table.
   if( $cFile )
   {
      $line = rtrim( fgets( $cFile ) );      // Take off the CR/LF

      // If we're not already at the EOF, then there are actual
      // comments in the file to be processed.
      if( !feof( $cFile ) )
         $areComments = 1;        // There are comments to process.

      else
         $areComments = 0;        // There are no comments to process.

      // Initialize count of how many comments are in the file. This
      // will be used to uniquely identify the comments in the file.
      $count = 0;

      // This data counter represents which line of information is currently
      // being read in and output (1=File, 2=Date, 3=User, 4=Body).
      $data = 1;

      // Signal that this is the first line of the file.
      $firstLine = true;

      while( !feof( $cFile ) )
      {

         // Save the file name into a variable.
         if( $data == 1 )
         {
            $curFile = $line;

            // If this is the first line of the file...
            if( $firstLine )
            {

               // Decide if a link to the actual class should be added.
               if( $showClassLink == 1 && $class != "generic-comments" )
               {
                  $extra .= " <A href='$curFile#$class' class='GoClassLink'>Go To This Class</A>";
               }

      ?>
      <BR>
      <A name='<?php print $class . "-comments"; ?>'></A>
      <TABLE class='CommentsBox' cellspacing=0 cellpadding=4 width='100%'>
         <TR class='CommentsHeadRow'>
            <TD>
               Comments <?php print $extra; ?>
            </TD>
         </TR><?php
            }

            $firstLine = false;
         }

         // Save the date information into a variable.
         elseif( $data == 2 )
            $curDate = $line;

         // Save the user information into a variable.
         elseif( $data == 3 )
            $curFrom = $line;

         // Print out the date, user, and comment body information
         // to the table.
         elseif( $data == 4 )
         {
            $curMsg = $line;

            $count++;         // Increment the comment count.

            // Print the information for this comment (along with
            // a delete link if the user is in manage mode and
            // they have the appropriate rights, which are determined
            // by logging in).
            ?>
         <TR>
            <TD colspan=2>
               <FONT class='CommentInfo'><?php print 'From: ' . $curFrom . ', Date: ' . $curDate; ?></FONT><?php
            if( $forViewManage == 1 && $canDelete == 1 ) {?>
               <A href='javascript:deleteComment("<?php print $class; ?>", <?php print $count; ?> );'><IMG src='images/delete.gif' width=12 height=12 alt='Delete' border=0></A><?php } ?>

               <BR>
               <FONT class='CommentText'><?php print $curMsg; ?></FONT>
            </TD>
         </TR><?php

            $data = 0;

            $wasOutput = true;                     // Signal at least one comment was printed.
         }

         $line = rtrim( fgets( $cFile ) );         // Read in the next line (and kill CR/LF).
         $data = $data + 1;                        // Increment the type of data being read in.
      }   // while( !feof( $cFile ) )

      // If there were comments printed, end the table.
      if( $areComments == 1 )
      {
         ?>
      </TABLE>
         <?php
      }

      // Close the file.
      fclose( $cFile );
   }   // if( $cFile )

   return $wasOutput;
}

?>
