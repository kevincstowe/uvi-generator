
      <!-- The form used to switch to View or Manage comments -->
      <FORM name='frmViewComments' method='post' action='@@rel-dir@@/comments.php'>
         <INPUT type='hidden' name='txtForManage'>
      </FORM>

      <!-- The form used to log out on any page -->
      <FORM name='frmLogout' method='post'>
         <INPUT type='hidden' name='txtLogout'>
      </FORM>

      <!-- The form used to enter a search request from any page -->
      <FORM name='frmSearch' method='post' action='@@rel-dir@@/search.php'>
         <DIV id='divSearch' class='SearchPanel'>
            <TABLE width=360 class='SearchTable'>
               <TR valign='middle'>
                  <TD align='center'>
                     <BR>
                     Search: &nbsp;<INPUT type='text' name='txtSearchRequest' class='SearchInput'>&nbsp;
                     <INPUT type='submit' name='cmdSearchGo' value='Go!'>
                     <INPUT type='button' name='cmdSearchClose' value='Close' onClick='clearSearch();'>
                     <BR>&nbsp;
                  </TD>
               </TR>
            </TABLE>
         </DIV>
      </FORM>

      <HR class='HBar'>

      <TABLE width='100%'>
         <TR>
            <TD align='left' width='40%'>
               <TABLE class='DateTimeTable' cellpadding=2 cellspacing=0>
                  <TR><TD>This page generated on @@date@@ at @@time@@.</TD></TR>
               </TABLE>
            </TD>
            <TD align='center' width='20%'><NOBR>@@footer-links@@</NOBR></TD>
            <TD align='right' width='40%'>@@license@@</TD>
         </TR>
      </TABLE>

   </BODY>
</HTML>
