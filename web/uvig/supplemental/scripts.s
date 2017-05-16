
////////////////////////
// Unified Verb Index //
////////////////////////


/////////////////////
// VerbNet Classes //
/////////////////////

// Show the WordNet page.
function wordNetMessage( wnVersion, verb, senseNum, senseKey )
{
   document.location = "../wn/wordnet.cgi?" + wnVersion + "." + verb + "." + senseNum + "." + senseKey + "#" + senseNum;
}

// Show the key for the members box.
function showMembersKey()
{
   alert( 'Key For Members\n  ' +
          'FN = VN-FN mapping\n    ' +
          '(FN link numbers have no particular meaning)\n  ' +
          'WN = WordNet Sense Numbers\n  ' +
          'G = OntoNotes Sense Grouping Group Numbers' );
}

// Show the key for the grouping and coloring in the frame boxes' syntax section.
function showFrameKey()
{
   alert( 'Key For Frames\n  ' +
          'Underlined = Thematic Role\n  ' +
          'Green V = Verb\n  ' +
          'Orange [...] = Selectional Restrictions\n  ' +
          'Orange <...> = Syntax Restrictions\n  ' +
          'Purple (...) = Lexical Requirements\n  ' +
          'Blue {...} = Preposition(s)\n  ' +
          'Blue {{...}} = Preposition Class\n     ' +
          '(via Selectional Restrictions)\n  ' +
          'Dark Red = Predicate' );
}

//////////////////
// Post Comment //
//////////////////

// Move to the add comment page.  This is called by each index page's
// 'Post Generic Comment' link and each VerbNet class's 'Post Comment'
// links.
function postComment( fil, cls )
{
   document.frmPostComment.txtFileName.value  = fil;        // File name for return redirection.
   document.frmPostComment.txtClassName.value = cls;        // Name of class comment is for (or 'generic-comments')
   document.frmPostComment.submit();
}

// Make sure the comment field on the post comment page is not blank.
// This is called by the form's 'onSubmit' event.  If it's a good
// comment, confirm the user's desire to post the comment.
function checkCommentAndConfirm()
{
   if( trim( document.frmPostComment.txtComment.value ) == '' )
   {
      alert( "Please enter a comment." );
      return false;
   }
   else
   {
      return confirm( "Submit this comment?" );
   }
}

///////////////////
// View Comments //
///////////////////

// Move to the view comments page.  This is called by each page's
// 'View or Manage comments' links.  The argument 'forManage' is
// a 1 if the delete capability is desired (a.k.a. they clicked
// 'Manage' instead of 'View').  It is a 0 if they clicked 'View'.
function viewComments( forManage )
{
   document.frmViewComments.txtForManage.value = forManage;
   document.frmViewComments.submit();
}

// Confirms that the user does want to delete a comment from the
// manage comments screen.
function deleteComment( cls, num )
{
   if( confirm( "Are you sure you want to delete this comment?" ) )
   {
      document.frmComments.txtDeleteCls.value = cls;     // File from which to delete comment.
      document.frmComments.txtDeleteNum.value = num;     // Comment # inside file (1 for first comment).
      document.frmComments.submit();
   }
}

// Take leading and trailing spaces off of a string.
function trim( s )
{
   while( s.substring( 0, 1 ) == ' ' || s.substring( 0, 1 ) == '\n' || s.substring( 0, 1 ) == '\r' )
      s = s.substring( 1, s.length );

   while( s.substring( s.length - 1, s.length ) == ' '  ||
          s.substring( s.length - 1, s.length ) == '\n' ||
          s.substring( s.length - 1, s.length ) == '\r')

      s = s.substring( 0, s.length - 1 );

   return s;
}

////////////
// Logout //
////////////

function logout()
{
   document.frmLogout.txtLogout.value = 'LOGOUT';
   document.frmLogout.submit();
}

////////////
// Search //
////////////

// Show the search box.
function showSearch()
{
   visi( "divSearch", true );

   // Put the focus in the input box.
   document.frmSearch.txtSearchRequest.focus();
}

// Hide the search box.
function clearSearch()
{
   visi( "divSearch", false );
}

// Change the visibility of a DIV.
function visi( nr, turnOn )
{
   if( document.layers )
      document.layers[ nr ].visibility = ( turnOn ? 'show' : 'hide' );

   else if( document.all )
      document.all[ nr ].style.visibility = ( turnOn ? 'visible' : 'hidden' );

   else if( document.getElementById )
      document.getElementById( nr ).style.visibility = ( turnOn ? 'visible' : 'hidden' );
}

// Show a little note about searching.
function showSearchInfo()
{
   alert( 'In this search box you can enter as many letters from the ' +
          'beginning of the verb(s) as you would like.  A match is returned ' +
          'if a verb from the index begins with any of the tokens supplied in the search request.  ' +
          'You can supply multiple search tokens using a comma-delimited ' +
          'list.  You do not have to type underscores (_) for particle constructions.  ' +
          'You must enter at least two letters per token.  Hyphens are accepted ' +
          'but numbers are not.  The search is case-insensitive.\n\nExamples:\n' +
          '  1) ask\n' +
          '  2) ask, question\n' +
          '  3) chase, pursue, follow\n' +
          '  4) ch, pur, foll\n' +
          '  5) sub, sup\n' +
          '  6) chicken_out\n' +
          '  7) chicken out, run away\n' +
          '  8) sound_\n' +
          '  9) spray-  (quick way to return just the VN class)' );
}
