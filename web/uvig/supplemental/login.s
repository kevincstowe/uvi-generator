<?php

////////////////////////
// Unified Verb Index //
////////////////////////

// Grab the posted login info and the currently set login info.
$user   = trim( $_POST[ "txtUserName" ] );
$passwd = $_POST[ "txtPassword" ];
$fileS  = $_POST[ "txtFileNameFrom" ];
$fileD  = $_POST[ "txtFileNameTo" ];
$class  = $_POST[ "txtClassName" ];
$suser  = $_SESSION[ "USER" ];
$admin  = $_SESSOIN[ "ADMIN" ];

if( $user != "" && $passwd != "" )
{
   $allUsers = file( "users/user.list" );

   $goodUser = 0;

   foreach( $allUsers as $curUser )
   {
      $allInfo = explode( ",", $curUser );

      $iUser = trim( $allInfo[ 0 ] );
      $iPwd  = trim( $allInfo[ 1 ] );
      $iAdm  = trim( $allInfo[ 2 ] );

      if( strtoupper( $user ) == strtoupper( $iUser ) &&
          $passwd == trim( $iPwd ) )
      {
         $goodUser = 1;
         break;
      }
   }

   $errMsg = "";

   // If they are
   if( $goodUser == 1 )
   {
      $suser = $iUser;
      $admin = $iAdm;
      $_SESSION[ "USER"  ] = $iUser;
      $_SESSION[ "ADMIN" ] = $iAdm;


      if( $class == "" )
         $toPage = "comments.php";
      else
         $toPage = $fileS;

      ?>
      <HTML><BODY>
         <FORM name='frmRedirect' method='post' action='<?php print $toPage; ?>'>
            <INPUT type='hidden' name='txtForManage' value='1'>
            <INPUT type='hidden' name='txtFileName' value='<?php print $fileD; ?>'>
            <INPUT type='hidden' name='txtClassName' value='<?php print $class; ?>'>
         </FORM>
      </BODY></HTML>
      <SCRIPT language='JavaScript'>document.frmRedirect.submit();</SCRIPT>
      <?php
      exit;
   }

   // Else set a message that will be displayed later on.
   else
      $errMsg = "Login Failed";
}

// Decide which message to display.  Show the user name
// the user is currently logged in under.
if( $suser == "" )
{
   $msg = "You are not currently logged in";
}
else
{
   $msg = "You are logged in as <U>" . $suser . "</U>";

   if( $admin == 1 )
      $msg .= " <FONT color='green'>[admin]</FONT>";
}

// If the manage comments page is being requested ($class==""), and they
// are successfully logged in ($suser!=""), and they are not an admin
// ($admin!=1) show a message explaining why they are not changing pages.
if( $class == "" && $suser != "" && $admin != 1 )
   $admMsg = "Only an administrator can access this resource";

?>
      <BR>
      <BR>

      <FORM name='frmLogin' method='post'>
         <TABLE align='center' width='270px' cellspacing=0 cellpadding=7 class='ClassSectionBox'>
            <TR class='ClassSectionHeadRow'>
               <TD colspan=2 align='center'><?php print $msg; ?></TD>
            </TR>
<?php if( $admMsg != "" ) { ?>
            <TR class='AdminOnly'>
               <TD colspan=2 align='center'><?php print $admMsg; ?></TD>
            </TR>
<?php } ?>
<?php if( $errMsg != "" ) { ?>
            <TR class='LoginFailed'>
               <TD colspan=2 align='center'><?php print $errMsg; ?></TD>
            </TR>
<?php } ?>
            <TR>
               <TD>Name</TD><TD><INPUT type='text' name='txtUserName'></TD>
            </TR>
            <TR>
               <TD>Password</TD><TD><INPUT type='password' name='txtPassword'></TD>
            </TR>
            <TR class='ClassSectionHeadRow'>
               <TD colspan=2 align='center'><INPUT type='submit' name='cmdSubmit' value='Login'></TD>
            </TR>
         </TABLE>

         <INPUT type='hidden' name='txtForManage' value='1'>
         <INPUT type='hidden' name='txtFileNameFrom' value='<?php print $fileS; ?>'>
         <INPUT type='hidden' name='txtFileNameTo' value='<?php print $fileD; ?>'>
         <INPUT type='hidden' name='txtClassName' value='<?php print $class; ?>'>
      </FORM>

      <!-- Start the focus in the comment box. -->
      <SCRIPT language='JavaScript'>document.frmLogin.txtUserName.focus();</SCRIPT>

      <BR>
