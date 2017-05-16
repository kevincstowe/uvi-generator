
/**********************/
/* Unified Verb Index */
/**********************/


/**********************/
/*** GENERAL STYLES ***/
/**********************/

BODY                                   /* BACKGROUND ON ALL PAGES */
{
   background: aliceblue;
}

A                                      /* ALL LINKS EXCEPT FOR UNIVERSITY LINKS & FN/WN LINKS */
{
   font-family:     Tahoma;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: none;
}

A:link    { color: red;     }
A:visited { color: red;     }
A:hover   { color: #BB2345; }          /* MAROON-LIKE COLOR */

UL                                     /* SO LISTS DON'T HAVE PADDING BEFORE AND AFTER */
{
   margin-top:    0em;
   margin-bottom: 0em;
}

/****************************/
/*** INDEX-RELATED STYLES ***/
/****************************/

.IndexMainTitle                        /* MAIN INDEX PAGE BIG TITLE */
{
   color:       green;
   font-family: Arial;
   font-size:   20pt;
   font-weight: bold;
}

.IndexLetterBox                        /* MAIN & LETTER INDEX LETTER BOXES */
{
   background:   white;
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
}

.IndexBannerBox                        /* INDEX PAGE BANNER BOX */
{
   background:   #FFFFCC;
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
}

.IndexImageCenter                      /* LETTER INDEX IMAGE */
{
   text-align: center;
}

.IndexUniversityLink                   /* CU LINK ON MAIN INDEX PAGE */
{
   color:           black;
   font-family:     Tahoma;
   font-size:       14pt;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: none;
}

.IndexUniversityLink:link    { color: black;   }
.IndexUniversityLink:visited { color: black;   }
.IndexUniversityLink:hover   { color: #FFD700; }   /* GOLD/YELLOW COLOR */

.EntryColor1 { background: #FAEBD7; }  /* PEACH COLOR */
.EntryColor2 { background: white;   }

TR.EntryColor1:hover, TR.EntryColor1-over { background: #BBFFBB; }
TR.EntryColor2:hover, TR.EntryColor2-over { background: #BBFFBB; }

.TotalRow                              /* THE TOTAL ROW AT THE END OF EACH INDEX (BOTH VERBS & CLASSES) */
{
   background:    lavender;
   border-left:   1px black solid;
   border-right:  1px black solid;
   border-bottom: 1px black solid;
   font-variant:  small-caps;
   font-weight:   bold;
}

.NoClassNames                          /* FOR WHEN THERE ARE NO CLASS NAMES FOR THE GIVEN LETTER */
{
   background: #FAEBD7;                /* PEACH COLOR */
   border:     1px black solid;
}

.HasSubclasses                         /* FOR CLASS-NAMES-WITH-LETTER SECTION */
{
   font-size:  10pt;
   font-style: italic;
}

.TotalsTable                           /* FOR THE TABLE WITH THE VERB/LINK TOTALS ON MAIN INDEX PAGE */
{
   background: thistle;                /* LAVENDER COLOR */
   border:     1px black solid;
}

.TotalsTableTitle                      /* FOR LABEL "The index has...." */
{
   font-style: italic;
}

.TotalsTableNum                        /* FOR VERB/LINK COUNT NUMBERS */
{
   font-weight: bold;
   margin-left: 1em;
}

.Download                              /* VERBNET AND PROPBANK DOWNLOAD LINKS */
{
   font-size:  10pt;
   font-style: italic;
}

/***************************/
/*** CONTACT PAGE STYLES ***/
/***************************/

.Obfus                                 /* CONTACT PAGE EMAILS */
{
   font-size: 16pt;
   font-variant: small-caps;
   font-weight: bold;
}

.ContactBox                            /* CONTACT PAGE EMAILS */
{
   background:   white;
   border:       1px black solid;
   font-size:    12pt;
}

/************************/
/*** ALL PAGES STYLES ***/
/************************/

.YesCaps { font-variant: small-caps; } /* VARIOUS PLACES */
.NoCaps  { font-variant: normal;     } /* VARIOUS PLACES */
.Small   { font-size:    10pt;       } /* VARIOUS PLACES */

.HBar                                  /* HEADER AND FOOTER HORIZONTAL RULES */
{
   background: dodgerblue;
   border:     none;
   color:      dodgerblue;
   height:     10px;
}

.SmallUniversityLink                   /* CU LINK ON ALL PAGES EXCEPT MAIN INDEX PAGE */
{
   color:           black;
   font-family:     Tahoma;
   font-size:       10pt;
   font-variant:    small-caps;
   font-weight:     normal;
   text-decoration: none;
}

.SmallUniversityLink:link    { color: black;   }
.SmallUniversityLink:visited { color: black;   }
.SmallUniversityLink:hover   { color: #FFD700; }   /* GOLD/YELLOW COLOR */

.PageTitle                             /* IN HEADER BAR, CENTER, ALL PAGES EXCEPT MAIN INDEX PAGE */
{
   color:       green;
   font-family: Arial;
   font-weight: bold;
}

.DateTimeTable                         /* PAGE GENERATED DATE AT BOTTOM OF EVERY PAGE */
{
   background:  darkblue;
   border:      1px black solid;
   color:       white;
   font-family: Tahoma;
   font-size:   10pt;
}

.WarningTable                          /* FOR COMMENT MANAGEMENT PAGE, POST COMMENT PAGE, SEARCH PAGE */
{
   background:   orange;
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
}

/*****************************/
/*** REFERENCE PAGE STYLES ***/
/*****************************/

.InfoTable                             /* FOR INFORMATION/LINK */
{
   background:   white;
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
}

.RefTable                              /* LIST TABLE */
{
   background: #DDFFDD;
   border: 1px black solid;
}

.RefTable2                              /* LIST TABLE (LIGHTER) */
{
   background: #EEFFEE;
   border: 1px black solid;
}

.RefGroup                              /* TITLE FOR EACH LIST */
{
   background:      #FFFFCC;
   text-align:      center;
   font-size:       14pt;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: underline;
}

.RefCounts                             /* TITLE FOR EACH LIST */
{
   background:      #FFFFCC;
   text-align:      center;
   font-size:       12pt;
   font-variant:    small-caps;
   font-weight:     bold;
}

/***********************************/
/*** CLASS HIERARCHY PAGE STYLES ***/
/***********************************/

.ClasHCol                              /* FOR COLUMN HEADER */
{
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: underline;
}

/*************************/
/*** LOGIN PAGE STYLES ***/
/*************************/

.AdminOnly                             /* FOR LOGIN PAGE */
{
   background: palegreen;
}

.LoginFailed                           /* FOR LOGIN PAGE */
{
   background: red;
   color:      white;
}

/****************************/
/*** COMMENTS PAGE STYLES ***/
/****************************/

.CommentList                           /* FOR LIST OF COMMENT LINKS ON comments.php */
{
   background: white;
   border: 1px black solid;
   font-variant: small-caps;
}

.GoClassLink                           /* FOR A GO TO CLASS LINKS ON comments.php */
{
   color:           palegreen;
   font-family:     Tahoma;
   font-size:       12pt;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: none;
}

.GoClassLink:link    { color: palegreen; }
.GoClassLink:visited { color: palegreen; }
.GoClassLink:hover   { color: white;     }

.CommentCount                          /* FOR THE COMMENT COUNTS NEXT TO EACH CLASS NAME */
{
   font-weight: bold;
}

/*********************/
/*** SEARCH STYLES ***/
/*********************/

.SearchPanel                           /* PANEL THAT APPEARS AND DISAPPEARS */
{
   left:       20px;
   position:   absolute;
   top:        33px;
   visibility: hidden;
}

.SearchTable                           /* TABLE INSIDE PANEL */
{
   background:   white;
   border:       1px black solid;
   font-variant: small-caps;
}

.SearchInput                           /* WIDTH OF SEARCH INPUT BOX */
{
   width: 100px;
}

.SearchError                           /* FOR SEARCH SCREEN */
{
   background: red;
   color:      white;
}

.SearchRequest                         /* FOR TITLE OF SEARCH SCREEN */
{
   font-size:    16pt;
   font-variant: small-caps;
}

.SearchRequestToken                    /* FOR TITLE OF SEARCH SCREEN */
{
   font-size:    18pt;
   font-variant: small-caps;
   font-weight:  bold;
}

.SearchWord
{
   background:   palegreen;
   font-size:    14pt;
   font-variant: small-caps;
   font-weight:  bold;
}

/****************************/
/*** VerbNet CLASS STYLES ***/
/****************************/

.ClassSectionBox                       /* FOR CLASS HIERARCHY, MEMBERS, THEM ROLES, AND FRAME BOXES */
{
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
   background:   #E8FFFF;
}

.ClassSectionHeadRow                   /* FOR CLASS HIERARCHY, MEMBERS, THEM ROLES, AND FRAME BOXES */
{
   background: skyblue;
}

.GoCommentsLink                        /* FOR A GO TO COMMENTS LINK */
{
   color:           green;
   font-family:     Tahoma;
   font-size:       12pt;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: none;
}

.GoCommentsLink:link    { color: green;     }
.GoCommentsLink:visited { color: green;     }
.GoCommentsLink:hover   { color: palegreen; }

.CommentsBox                           /* FOR COMMENT BOX */
{
   background:   palegreen;
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
}

.CommentsHeadRow                       /* FOR COMMENT BOX */
{
   background: #55BE55;                /* MEDIUM GREEN */
}

.CommentInfo                           /* FOR A SINGLE COMMENT'S AUTHOR AND DATE/TIME */
{
   font-variant:    normal;
   font-weight:     bold;
   text-decoration: underline;
}

.CommentText                           /* FOR A SINGLE COMMENT'S TEXT */
{
   font-variant: normal;
   font-weight:  normal;
}

.ClassTitleBox                         /* TITLE OF MAIN VERBNET CLASS */
{
   background:  white;
   border:      1px black solid;
   font-family: Tahoma;
   font-size:   20pt;
   font-weight: bold;
}

.SubclassTitleBox                      /* TITLE OF ALL VERBNET SUBCLASSES */
{
   background:  white;
   border:      1px black solid;
   font-family: Tahoma;
   font-size:   16pt;
   font-weight: bold;
}

.SubclassTopLink                       /* LINK IN SUBCLASS TITLE FOR TOP */
{
   font-size: 12pt;
}

.SubtleText                            /* FOR CLASS/SUBCLASS NUMBERS AND LABEL 'Subclass' */
{
   color:       silver;
   font-family: "Times New Roman";
   font-size:   12pt;
   font-style:  italic;
}

.AbsenceOfItems                        /* FOR 'NO SUBCLASSES', 'NO MEMBERS', 'NO ROLES', 'NO FRAMES' AND 'NO CLASS NAMES...' */
{
   font-size:  10pt;
   font-style: italic;
}

.VerbLinks
{
    color: black;
}

.VerbLinks:link { color:black; }  /* FOR VERB LINKS THAT SEARCH FOR THAT VERB */
.VerbLinks:visited { color:black; }
.VerbLinks:hover { color:red; }

.PredLinks:link { color:darkred; }
.PredLinks:visited { color:darkred; }
.PredLinks:hover { color:red; }
	
.MemberLinks                           /* FOR FN AND WN LINKS IN MEMBERS BOX */
{
   color: orangered;
}

.MemberLinkNumbers                     /* FOR ACTUAL <A> TAGS IN FN AND WN LINKS (SMALLER THAN NORMAL LINKS) */
{
   font-family:     Tahoma;
   font-size:       8pt;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: none;
}

.MemberLinkNumbers:link    { color: red;     }
.MemberLinkNumbers:visited { color: red;     }
.MemberLinkNumbers:hover   { color: #BB2345; }   /* MAROON-LIKE COLOR */

.SelRestrs                             /* FOR SELECTIONAL RESTRICTIONS IN THEM ROLE BOX */
{
   color:       orangered;
   font-weight: bold;
}

/* THE REST ARE FOR DIFFERENT PARTS OF THE FRAME SYNTAX */

.FrameDescription
{
   background: white;
}

.ThemRole
{
   text-decoration: underline;
}

.Predicate
{
   color:       darkred;
   font-weight: bold;
}

.Negated
{
   color:       red;
   font-weight: bold;
}

.SynRestrs
{
   color:       orangered;
   font-weight: bold;
}

.VerbV
{
   color:       darkgreen;
   font-size:   14pt;
   font-weight: bold;
}

.Preposition
{
   color:       darkblue;
   font-weight: bold;
}

.LexicalItem
{
   color:       indigo;
   font-weight: bold;
}

TD.MemberCell:hover, TR.MemberCell-over { background: #BBFFBB; }
