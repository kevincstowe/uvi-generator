#!/usr/bin/perl

print "Content-type: text/html\n\n";

##########################################
## THIS SECTION NICELY DISPLAYS WN INFO ##
## Written By Derek Trumbo              ##
##########################################

($wnVersion, $word, $sensenum, $sensekey) = split(/\./, $ENV{QUERY_STRING});
$pos = "v";
if( $sensekey eq "" )
{
   $sensekey = "not supplied";
}
else
{
   $sensekey =~ s/-/%/;
}
$wnVersion =~ s/-/./;

&PrepareWNFiles;
@s = reverse &SynsetNumbersNew( $word, $pos );

if( $sensenum < 1 || $sensenum > @s )
{
   $sensenum = "?";
}

print<<"HEADER";
<?xml version='1.0'?>
<!DOCTYPE HTML PUBLIC '-//W3C//DTD HTML 4.01 Transitional//EN' 'http://www.w3.org/TR/html4/loose.dtd'>
<HTML>
   <HEAD>
      <TITLE>Unified Verb Index</TITLE>
      <LINK rel='stylesheet' type='text/css' href='styles-wn.css'>
      <LINK rel='shortcut icon' href='../images/favicon.ico'>
      <SCRIPT type='text/javascript' src='../scripts.js'></SCRIPT>
   </HEAD>

   <BODY><A name='top'></A>

      <TABLE width='100%'>
         <TR>
            <TD align='left' width='40%'>
               <A href='../index.php'>Return Home</A> |
               <A href='javascript:history.back();'>Back</A> |
               <A href='javascript:showSearch();'>Search</A>
            </TD>
            <TD align='center' width='20%' class='PageTitle'><NOBR>WordNet $wnVersion Viewer - Unified Verb Index</NOBR></TD>
            <TD align='right' width='40%'>
               <NOBR><A href='javascript:viewComments(0);'>View</A><FONT class='YesCaps'> or </FONT>
               <A href='javascript:viewComments(1);'>Manage</A><FONT class='YesCaps'> all comments</FONT> |
               <A href='http://www.colorado.edu/' class='SmallUniversityLink'>University of Colorado</A></NOBR>
            </TD>
         </TR>
      </TABLE>

      <HR class='HBar'>

      <BR>

      <TABLE class='ClassTitleBox' cellspacing=0 cellpadding=7 width='30%' align='center'>
         <TR>
            <TD align='center'><NOBR>$word/$sensenum</NOBR><BR>
            <A href="javascript:document.frmWordNet.s.value='$word';document.frmWordNet.submit();"
               class='MemberLinkNumbers'>show in wordnet</A>
            </TD>
         </TR>
      </TABLE>

      <BR>

HEADER

if( @s != 0 )
{
   $n = 0;

   for $s ( @s )
   {
      $n++;
      $d = &GetDefn( $s, $pos );
      chomp( $d );
      $d =~ s/^ *//;
      $d =~ s/ *$//;

      if( $d =~ m/"/ )
      {
         $d =~ s/; *"/\) "/;
         $d =~ s/"/<I>"/;
         $d .= "</I>";
      }
      else
      {
         $d .= ")";
      }

      $d = "($d";

      $ss = join( ', ', &SynsetWordList( $s, $pos ) );

      $hh = &printHyps( $s, $pos, 0, "" );
      chomp( $hh );
      $hh =~ s/<BR>$//;

      if( $sensenum == $n )
      {
         $cls1 = "ClassSectionBoxHighlight";
         $cls2 = "ClassSectionHeadRowHighlight";
         $extra = "&nbsp;&nbsp;&nbsp;&nbsp;[sense key: $sensekey]";
      }
      else
      {
         $cls1 = "ClassSectionBox";
         $cls2 = "ClassSectionHeadRow";
         $extra = "";
      }

      if( $ss =~ m/$word/ )
      {
         $ss =~ s/$word/<B><U>$word<\/U><\/B>/;
      }

      print<<"BODY";
      <A name='$n'></A>
      <TABLE class='$cls1' cellspacing=0 cellpadding=4 width='70%' align='center'>
         <TR class='$cls2'>
            <TD width='100px'>Sense Num</TD>
            <TD>$n$extra</TD>
         </TR>
         <TR valign='top'>
            <TD width='100px'><B>gloss</B></TD>
            <TD class='NoCaps'>$d</TD>
         </TR>
         <TR valign='top'>
            <TD width='100px'><B>synset</B></TD>
            <TD class='NoCaps'>$ss</TD>
         </TR>
         <TR valign='top'>
            <TD width='100px'><B>hypernyms</B></TD>
            <TD class='NoCaps'>$hh</TD>
         </TR>
      </TABLE>

      <BR>

BODY
   }
}
else
{
print<<"ERRTABLE";
      <TABLE class='WarningTable' cellpadding=10 align='center'>
         <TR><TD>This word not found in WordNet</TD></TR>
      </TABLE>
ERRTABLE
}
print<<"FOOTER";

      <!-- The form used to switch to View or Manage comments -->
      <FORM name='frmViewComments' method='post' action='../comments.php'>
         <INPUT type='hidden' name='txtForManage'>
      </FORM>

      <!-- The form used to log out on any page -->
      <FORM name='frmLogout' method='post'>
         <INPUT type='hidden' name='txtLogout'>
      </FORM>

      <!-- The form used to enter a search request from any page -->
      <FORM name='frmSearch' method='post' action='../search.php'>
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

      <FORM method="get" action="http://wordnetweb.princeton.edu/perl/webwn" enctype="multipart/form-data" name="frmWordNet">
         <INPUT type="hidden" name="s" value="send" maxlength="500" />
         <INPUT type="hidden" name="o2" value="" />
         <INPUT type="hidden" name="o0" value="1" />
         <INPUT type="hidden" name="o7" value="1" />
         <INPUT type="hidden" name="o5" value="" />
         <INPUT type="hidden" name="o1" value="1" />
         <INPUT type="hidden" name="o6" value="1" />
         <INPUT type="hidden" name="o4" value="" />
         <INPUT type="hidden" name="o3" value="" />
         <INPUT type="hidden" name="h" value="" />
      </FORM>

      <HR class='HBar'>

      <TABLE width='100%'>
         <TR>
            <TD align='right'><A href='../contact.php'>Contact</A></TD>
         </TR>
      </TABLE>
   </BODY>
</HTML>
FOOTER

sub printHyps
{
   my( $s, $pos, $level, $output ) = @_;

   @h = &GetHypNumbers( $s, $pos );

   for $h ( @h )
   {
      $hh = join( ', ', &SynsetWordList( $h, $pos ) );
      if( $level == 0 )
      {
         $output .= "<I>Direct:</I> $hh<BR>\n";
      }
      else
      {
         $output .= "            ";
         for( $aa = 0; $aa < $level; $aa++ )
         {
            $output .= "&nbsp;&nbsp;&nbsp;";
         }
         $output .= "<I>Inherited:</I> $hh<BR>\n";
      }
      $output = &printHyps( $h, $pos, $level + 1, $output );
   }

   return $output;
}





###############################################
## EVERYTHING BELOW HERE SUPPORTS ABOVE CODE ##
## Written By Dan Gildea                     ##
###############################################

# This library includes functions for handling WordNet
# See WNDB files in the WN manual for detailed info on the database file structures

#Exactly one of these 2 subroutines should be commented out

#Opens all the files - must be called before accessing Wordnet
#Sets global file handles
#No parameters, no return

sub PrepareWNFiles

{

#my ($Path) = "/n/jolt/da/aicorpus/wordnet/dict/";
#my ($Path) = "/u/gildea/wordnet-1.6/dict/";
#my ($Path) = "/mnt/unagi/nldb2/wordnet/dict/";
#my ($Path) = "./WordNet-3.0/dict/";
my ($Path) = "/usr/local/WordNet-3.0/dict/";



my ($Verbindex) = $Path."index.verb";
my ($Advindex) = $Path."index.adv";
my ($Nounindex) = $Path."index.noun";
my ($Adjindex) = $Path."index.adj";

my ($Verbdata) = $Path."data.verb";
my ($Advdata) = $Path."data.adv";
my ($Noundata) = $Path."data.noun";
my ($Adjdata) = $Path."data.adj";

open(VERBINDEXFILE,"$Verbindex") || die "Can't open $Verbindex\n";
open(VERBDATAFILE,"$Verbdata") || die "Can't open $Verbdata\n";
open(NOUNINDEXFILE,"$Nounindex") || die "Can't open $Nounindex\n";
open(NOUNDATAFILE,"$Noundata") || die "Can't open $Noundata\n";
open(ADJINDEXFILE,"$Adjindex") || die "Can't open $Adjindex\n";
open(ADJDATAFILE,"$Adjdata") || die "Can't open $Adjdata\n";
open(ADVINDEXFILE,"$Advindex") || die "Can't open $Advindex\n";
open(ADVDATAFILE,"$Advdata") || die "Can't open $Advdata\n";

}

sub GetDefn {
my ($Synset, $POS) = @_;
my ($Retval);
my (@tokens);
my ($ListLength);

@tokens = split (/\|/, &SynsetLookup($Synset, $POS));

#you should never have "|" inside a line, but just in case ...take last

$ListLength = @tokens;
$Retval = $tokens[$ListLength-1];

}

sub IndexerString {

#This routine makes a one digit integer into a 2-digit string
#Used to identify sense numbers within WN

my ($i) = @_;
my ($Retval) = $i;

	if ($i < 10) {
		$Retval = "0".$i;
	}
	$Retval;
}

sub SynsetLookup {

#returns a whole line as a string, given a synset offset number and a POS

my ($SeekSynset, $POS) = @_; #(input is a synsetoffsetnumber)
my ($line);
	if ($POS eq "v"){
		seek(VERBDATAFILE, $SeekSynset, 0);
		$line = <VERBDATAFILE>;
	}
	elsif ($POS eq "n"){
		seek(NOUNDATAFILE, $SeekSynset, 0);
		$line = <NOUNDATAFILE>;
	}
	elsif ($POS eq "adj"){
		seek(ADJDATAFILE, $SeekSynset, 0);
		$line = <ADJDATAFILE>;
	}
	elsif ($POS eq "adv"){
		seek(ADVDATAFILE, $SeekSynset, 0);
		$line = <ADVDATAFILE>;
	}

}


sub SynsetNumbersNew
{

# input: Lemma, POS
#Returns: list of synset numbers that contain this lemma in the synset

my ($SeekLemma, $POS) = @_;
my (@Retval);
my ($RangeStart);
my ($RangeEnd);
my ($IndexFile);
my (@tokens);
my ($FoundLine) = "";
my ($NumberOfSenses);
my ($NumberOfTokens);
my ($i);
my ($StoppingPoint);

#This constant could change with a new version of Wordnet
$RangeStart = 1740;

if ($POS eq "v") {
	$IndexFile = "VERBINDEXFILE";
}
elsif ($POS eq "n") {
	$IndexFile = "NOUNINDEXFILE";
}
elsif ($POS eq "adj") {
	$IndexFile = "ADJINDEXFILE";
}
elsif ($POS eq "adv") {
	$IndexFile = "ADVINDEXFILE";
}

#Find the end of the file
seek ($IndexFile, 0, 2);
$RangeEnd = tell($IndexFile);

$FoundLine  = &RecursiveIndexSearch($SeekLemma, $IndexFile, $RangeStart, $RangeEnd);
chop ($FoundLine);

@tokens = split (/ /, $FoundLine);
$NumberOfSenses = $tokens[2];
$NumberOfTokens = @tokens;

#Start at the last token (number of tokens - 1 is subscript array)
#Once for each sense, push the token onto the return value array

#StoppingPoint is the leftmost array subscript which contains a synset
$StoppingPoint = $NumberOfTokens - $NumberOfSenses ;

#It's -2 because of the array subscript starts at 0, and because the last one is a newline char
for ($i = $NumberOfTokens - 1 ; $i >= $StoppingPoint; $i--) {
	push (@Retval, $tokens[$i]);
}

@Retval;

}

sub RecursiveIndexSearch

# Looks through the index file given for the lemma.  Returns the whole line once found.
#Called from SynsetNumbers - when first calling, give the end of the disclaimer (1740)
#as range start, and eof offset as range end.

{
my ($SeekLemma, $IndexFile, $RangeStart, $RangeEnd) = @_;
my ($dumpline); #garbage partial line
my ($Position);
my ($myline);
my ($HalfwayPoint);
my ($Retval);
my ($CompareLemmas);

#Note that the longest index file length is 602 (break, v)
#The longest index file length for an n is 301 (head, n)

#A clean start on the old RecursiveIndexSearch
#If the range is less than 700 chars (for WN, for a lex file, can be less)
#	Start at start of range
#		Hop through til the end, checking along the way
#			If you find it, return the line
#	Stop when "tell" of the next line is greater than range end
#	Then do one more
#		If you find it, return the line
#		If nothing, return empty
#Otherwise
	#Set Halfway point to the middle of the range
	#Seek the halfway point and throw away the current line.
	#Go to the next line and check
	#If it's what you want, return the line - done
	#If it's less that what you want, RangeStart = HalfwayPoint
	#If it's more than what you want, RangeEnd = HalfwayPoint
		#And search again, with the new Range start and end

if ($RangeEnd - $RangeStart <= 700) {
	seek ($IndexFile, $RangeStart - 1 , 0);
	$dumpline = (<$IndexFile>); #dump the garbage line
	while ($Position < $RangeEnd){
		$Position = tell $IndexFile;
		$myline = (<$IndexFile>);
		if (&CompareLemmas($myline, $SeekLemma) == 2){
			return $myline;
		}
	}
	#and then check the next one, too.  This may not be necessary, but it doesn't hurt
	$myline = (<$IndexFile>);
	if (&CompareLemmas($myline, $SeekLemma) == 2){
		return $myline;
	}
	else {
		return 0; #if it wasn't found
	}
}
else {
	#set the halfway point in the range

	$HalfwayPoint = int(($RangeEnd-$RangeStart)/2) + $RangeStart;

	#Get the line you want
	seek ($IndexFile, $HalfwayPoint, 0);
	$dumpline = (<$IndexFile>); #get rid of the garbage line
	$myline = (<$IndexFile>);

	# check to see if the lemma is there.
	$CompareLemmas = &CompareLemmas($myline, $SeekLemma);
	if ($CompareLemmas == 2){
		return $myline;
	}
	#if the lemma we are looking for is later in the file
	elsif ($CompareLemmas == 1){
		$RangeStart = $HalfwayPoint;
	}
	#if the lemma we are looking for is earlier in the file
	elsif ($CompareLemmas == -1){
		$RangeEnd = $HalfwayPoint;
	}

	#call the function again with the new range set.
	$Retval = &RecursiveIndexSearch($SeekLemma, $IndexFile, $RangeStart, $RangeEnd);
}
}

sub CompareLemmas
#input is a line and a lemma
#output is 0 if the lemma is not the first word of the line
{
my ($line, $Lemma) = @_;
my (@tokens);
my ($Retval);

@tokens = split(/ /, $line);
if ($tokens[0] eq $Lemma){
	$Retval = 2;
}
#if the seeklemma is later than the line - you need to go forwards
elsif ($tokens[0] lt $Lemma) {
	$Retval = 1;
}
#if the seek lemma is earlier than the line - you need to go backwards
elsif ($tokens[0] gt $Lemma) {
	$Retval = -1;
}
$Retval;
}



sub GetDataLines
{
my ($Lemma, $POS) = @_;
my ($Synset);
my (@WholeLines);

#Starting with the lemma, return ALL of data lines with synsets it is in.

foreach $Synset(&SynsetNumbersNew($Lemma, $POS)){
	push (@WholeLines, &SynsetLookup($Synset, $POS));
}
@WholeLines;

}


sub Syns
{

#Starting with the lemma, return ALL of the synonyms in all of the synsets.
#return an unordered list from WordNet of all syns (from the synsets)
#for right now, we'll leave this case-sensitive

my ($Lemma, $POS) = @_;
my ($Synset);
my (@Synsets);
my ($Syn);
my ($Line);
my ($Word);
my (@WholeList);
my (%SynHash);

#Get all the data lines for this lemma
@Synsets = &SynsetNumbersNew($Lemma, $POS);

#Then parse the whole lines:
#Start at tokens[4].  Look at every other token - if it's 3 digits and the first is 0, stop
#Else, push it onto the stack

#You could rewrite this, since the 4th field in the datafile indicates how many words are in the synset

foreach $Synset(@Synsets){
	foreach $Word(&SynsetWordList($Synset, $POS)){
		if ($SynHash{$Word} ne "1") {
			push (@Retval, $Word);
			$SynHash{$Word} = "1";
		}
	}
}
@Retval;
}


sub GetAllHypsRecursive

{
#OK, you can now get the hyps for a synset.
#What you need is ALL of the hyps of the synset, and all its hyps, etc.
#So:
#Start with your synset, get its hyps, if any
#if there are any hyps, for each hyp it returns, push it onto the array
#				  Call the function again with that hyp
#If there aren't any hyps, return 0;
#Return a list of hyps, which can then be added to the hash in both the main and the subroutine;

#Synset 1 has hyps 2 and 3.
#For each of 2 and 3
#	push 2, 3 onto @Retval
#	push onto @Retval the results of GetAllHyps of 2,3
#return @Retval


my ($Synset, $POS, $RelationType) = @_;
my (@Retval);
my ($Hyp);

#print $RelationType;
if ($RelationType eq "") {
	$RelationType = "hyper";  #this is the default
}

foreach $Hyp(&GetHypNumbers($Synset, $POS, $RelationType)){

#        print $Synset, ' ', $Hyp, ' ', join(' ', &SynsetWordList($Hyp, $POS)), "\n";

	push (@Retval, $Hyp);
	push (@Retval, &GetAllHypsRecursive($Hyp, $POS, $RelationType));
}
@Retval;
}

sub GetAllHyps
{
#An outer shell to wrap around the recursive version
#to return a unique list.

my ($Synset, $POS, $RelationType) = @_;
my (%HypHash);
my (@HypArray);
my ($Hyp);

#print $RelationType; print "\n";
if ($RelationType eq "") {
	$RelationType = "hyper";  #this is the default
}

foreach $Hyp (&GetAllHypsRecursive($Synset, $POS, $RelationType)) {
	if ($HypHash{$Hyp} ne "1") {
		push (@HypArray, $Hyp);
		$HypHash{$Hyp} = "1";
	}
}

@HypArray;
}


sub GetHypNumbers

{
#Given a synset, return the synset(s) of the hyps of that synset
#Changed 2/5/99 to allow different relations.  The switch is the relations type:
#Possible values are hyper, hypo

my ($Synset, $POS, $RelationType) = @_;
my (@Retval);
my ($Line);
my ($i) = 0;
my (@tokens);
my ($NumberOfPointers);
my ($NumberOfWords);
my ($NOPPosition);
my ($Position);

if ($RelationType eq "") {
	$RelationType = "hyper";  #this is the default
}

$Line = &SynsetLookup($Synset, $POS);
#print $Line; print "\n";

@tokens = split (/ /, $Line);
$NumberOfWords = int($tokens[3]);

#NOW = 1 : Words = 4, 5, $NOP = 6
#NOW = 2 : Words = 4-7, $NOP = 8
#NumberofPointers is right after the words.
#So, $NOP is at the field number 4 + 2*NOW

$NOPPosition = 4 + (2 * $NumberOfWords);
$NumberOfPointers = int($tokens[$NOPPosition]);

#Starting at NOPPosition + 1, check for "@".
#If you find it, stuff the next element into the array.
#Increment by 2, and loop for number of pointers

$Position = $NOPPosition + 1;

#print $RelationType;
for ($i = 1; $i <= $NumberOfPointers; $i++) {
	if ($tokens[$Position] eq "@" && $RelationType eq "hyper") {
		push (@Retval, $tokens[$Position + 1]);
	}
	elsif ($tokens[$Position] eq "~" && $RelationType eq "hypo") {
		push (@Retval, $tokens[$Position + 1]);
	}
	$Position = $Position + 4;
}

@Retval;

}

sub SynsetWordList
{
#given a synset and POS, return a list of the lemmas of the syns in it.

my ($Synset, $POS) = @_;
my ($Syn);
my ($NumberOfWords);
my ($Position);
my (@Retval);
my ($Line);
my ($i);
my (@tokens);
my ($mytoken);

#Get the data line
#This is sort of inefficient - in some cases when this is called, the line will already be available.
#If that's always true, you might pass in the whole line instead of looking it up.

	$Line = &SynsetLookup($Synset, $POS);

#Then parse the whole lines:

#Added 3/31/99 to synch up with FrameLibrary3 on home PC
#Check for data quality
	if (substr ($Line, 0, 8) !~ /\d{8}/) {
		return @Retval;
	}


#The 4th field in the datafile indicates how many words are in the synset

	@tokens = split (/ /, $Line);

#Changed 3/31/99 to synch up with FrameLibrary3 on home PC
	$NumberOfWords = hex(@tokens[3]);

	$Position = 4;

	for ($i = 1; $i<=$NumberOfWords; $i++) {
		$mytoken = $tokens[$Position];
		if ($POS eq "adj"){
		#special part to handle (a), (p), (ip)
			$mytoken =~ s/\([ap]\)//;
			$mytoken =~ s/\(ip\)//;
		}
		push (@Retval, $mytoken);
		$Position = $Position + 2;
	}
	@Retval;
}

sub IsLeafOfHypTree
# input: Synset, POS
# return 1 if it has hyponyms, else return 0
{
my ($Synset, $POS) = @_;
if (&SynsetLookup($Synset,$POS) !~ /\~/) {
	return 1;
}
else {
	return 0;
}
}
