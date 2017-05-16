<?php

////////////////////////
// Unified Verb Index //
////////////////////////

// Allow each person who vists the UVI to keep their login
// information around with them during their visit.
session_start();

header( "Cache-Control: max-age=3600" );

// Allow each page access to a joint group of PHP functions.
include "@@rel-dir@@/include.php";

// Need to print the XML header tag to certain PHP versions
// don't croak.
print "<?xml version='1.0'?>\n";
?><!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>
<HTML>
   <HEAD>
      <TITLE>@@browser-title@@</TITLE>
      <LINK rel='stylesheet' type='text/css' href='@@rel-dir@@/styles.css'>
      <LINK rel='shortcut icon' href='@@rel-dir@@/images/favicon.ico'>
      <SCRIPT type='text/javascript' src='@@rel-dir@@/scripts.js'></SCRIPT>
   </HEAD>

   <BODY><A name='top'></A>

      <TABLE width='100%'>
         <TR>
            <TD align='left' width='40%'>
               <A href='@@rel-dir@@/index.php'>Return Home</A> |
               <A href='javascript:history.back();'>Back</A> |
               <A href='javascript:showSearch();'>Search</A>
               <!-- | <A href='javascript:logout();'>Logout</A>-->
            </TD>
            <TD align='center' width='20%' class='PageTitle'><NOBR>@@page-title@@</NOBR></TD>
            <TD align='right' width='40%'>
               <NOBR><A href='javascript:viewComments(0);'>View</A><FONT class='YesCaps'> or </FONT>
               <A href='javascript:viewComments(1);'>Manage</A><FONT class='YesCaps'> all comments</FONT> |
               <A href='http://www.colorado.edu/' class='SmallUniversityLink'>University of Colorado</A></NOBR>
            </TD>
         </TR>
      </TABLE>

      <HR class='HBar'>

