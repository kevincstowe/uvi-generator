
/**********************/
/* Unified Verb Index */
/**********************/

/**********************/
/*** GENERAL STYLES ***/
/**********************/

BODY                                   /* BACKGROUND ON ALL PAGES */
{
   background: #FFFCEE;
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

/************************/
/*** ALL PAGES STYLES ***/
/************************/

.YesCaps { font-variant: small-caps; } /* VARIOUS PLACES */
.NoCaps  { font-variant: normal;   font-weight: normal;  } /* VARIOUS PLACES */
.Small   { font-size:    10pt;       } /* VARIOUS PLACES */

.HBar                                  /* HEADER AND FOOTER HORIZONTAL RULES */
{
   background: yellow;
   border:     none;
   color:      yellow;
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
   color:       orangered;
   font-family: Arial;
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
}

.ClassSectionHeadRow                   /* FOR CLASS HIERARCHY, MEMBERS, THEM ROLES, AND FRAME BOXES */
{
   background: yellow;
   font-size: 14pt;
}

.ClassSectionBoxHighlight              /* FOR CLASS HIERARCHY, MEMBERS, THEM ROLES, AND FRAME BOXES */
{
   border:       3px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
   background: #FFFACD;
}

.ClassSectionHeadRowHighlight          /* FOR CLASS HIERARCHY, MEMBERS, THEM ROLES, AND FRAME BOXES */
{
   background: red;
   color:      yellow;
   font-size: 14pt;
}

.ClassTitleBox                         /* TITLE OF MAIN VERBNET CLASS */
{
   background:  white;
   border:      1px black solid;
   font-family: Tahoma;
   font-size:   20pt;
   font-weight: bold;
}

.MemberLinkNumbers                     /* FOR ACTUAL <A> TAGS IN FN AND WN LINKS (SMALLER THAN NORMAL LINKS) */
{
   font-family:     Tahoma;
   font-size:       12pt;
   font-variant:    small-caps;
   font-weight:     bold;
   text-decoration: none;
}

.MemberLinkNumbers:link    { color: red;     }
.MemberLinkNumbers:visited { color: red;     }
.MemberLinkNumbers:hover   { color: #BB2345; }   /* MAROON-LIKE COLOR */

.WarningTable                          /* FOR COMMENT MANAGEMENT PAGE, POST COMMENT PAGE, SEARCH PAGE */
{
   background:   orange;
   border:       1px black solid;
   font-size:    12pt;
   font-variant: small-caps;
   font-weight:  bold;
}
