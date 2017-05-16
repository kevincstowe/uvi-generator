
////////////////////////////
// VxC: A VN-Cyc Mapper   //
// Built on the Inspector //
// University of Colorado //
// Fall 2006              //
////////////////////////////

package semlink.apps.vxc;

/**
 * This class contains all the match constraints materalized into code.
 * All methods besides those for the null constraint and the naive constraint
 * accept a VerbNet syntax string and a Cyc <CODE>verbSemTrans</CODE> rule as their
 * arugments.  These methods don't need to know what the verb is
 * because it is assumed when they are called, the naive constraint
 * has already been applied, and the VerbNet syntax and Cyc rule arguments are for
 * the same verb.  The naive constraint will always be applied first.
 * If the naive constraint for a VN syntax-Cyc rule pair fails (i.e.
 * the syntax was for verb 'jump' and Cyc rule was for verb 'jest')
 * then there is no match and no further constrains are applied.
 * <BR><BR>
 * Each method returns a boolean value.  If a constraint method returns
 * <CODE>false</CODE>, that method has determined that the VerbNet syntax and the
 * Cyc rule are incompatible and there is no match.  However, if a constraint method
 * returns <CODE>true</CODE>, it is not saying that there exists a good match,
 * but rather that there is no reason to discard the match <I>as far as it
 * is concerned.</I>  There could exist other constraint methods that
 * find cause to discard it.
 * <BR><BR>
 * The word <U>match</U> means that a congruence was found between a
 * VerbNet verb-frame pair and a Cyc rule. The <CODE>defend-85</CODE>
 * VerbNet class has 4 members and 4 frames (as of VN Version 2.1).  Therefore,
 * there are 16 verb-frame pairs for this class.  Each verb-frame pair has the
 * possibility of matching with one or more Cyc <CODE>verbSemTrans</CODE> rules.
 * Each Cyc rule is basically a syntactic frame for a single verb.  A
 * <U>possible match</U> is any such (verb-frame pair, Cyc rule) combination.
 * The constraints applied by VxC (as specified by -A operator) determine if
 * there exists an actual match.
 * <BR><BR>The methods {@link vn.Matcher#bad_DBPBimpliesCause(String, String, String)} and
 * {@link vn.Matcher#bad_agentImpliesDBPB(String, String)} are only included in this Javadoc
 * for historical and informational purposes.  They represent constraints that
 * had poor performance on the VerbNet-Cyc mapping.  They are not available
 * for use when executing the VxC application (i.e. they are not invoked
 * when the -Aa option is provided).
 * <BR><BR>
 * Finally, usually the implementation of these methods can be understood
 * by examining the syntax of the VerbNet syntax statement and the
 * tokens that are generally used in Cyc rules.  Use the key option (-k)
 * to learn more about VerbNet syntax: <PRE>   java vn.Inspector -k</PRE>
 * Also, one can get a good feel for VerbNet syntax by just looking at lots
 * of examples.  If you download the original Inspector (instead of trying
 * to use VxC as a VerbNet viewer) you can quickly pull all the VerbNet
 * syntax strings: <PRE>   java vn.Inspector xml/ -Vxyz</PRE>
 * You can use VxC as a VerbNet viewer, you just have to still specify
 * all the mandatory operators (-C, -M and -F).
 * <BR><BR>
 * <I>NOTE: The VerbNet and Cyc data is given to these methods in the case
 * that it was in when it was extracted from the external files.
 * In other words, all token matches in either the VerbNet or Cyc
 * data must be done in a case-sensitive manner.  Luckily
 * the case format for both systems is standardized so you need not
 * worry about performing case-insensitive comparisons.</I>
 *
 * @see vn.Cyc#findCycMatches(String, String, String, int, String)
 *
 * @author Derek Trumbo
 * @version 1.0, 2006.10.25
 */
public class Matcher
{

   /////////////////
   // Constructor //
   /////////////////

   /**
    * This constructor is private because the class is not intended to ever
    * be instantiated.  The Inspector's job is very procedural and thus
    * all its members are static.
    */
   private Matcher() {}

   ////////////////////
   // Helper Methods //
   ////////////////////

   /**
    * Used as shorthand for <CODE>System.err.println</CODE>.
    *
    * @param s the string to print
    * @see     java.io.PrintStream#println(String)
    */
   private static void eprintln( String s ) { System.err.println( s ); }

   /**
    * Used as shorthand for <CODE>System.err.print</CODE>.
    *
    * @param s the string to print
    * @see     java.io.PrintStream#print(String)
    */
   private static void eprint( String s ) { System.err.print( s ); }

   //////////////////////////////
   // Match Constraint Methods //
   //////////////////////////////
   /**
    * Always returns <CODE>false</CODE>.  It discards every possible match. This method is for
    * debugging purposes only, but can still be invoked on the
    * command-line.  Its constraint symbol is <CODE>0</CODE> (zero).
    * Using this constraint (-A0 on command-line), one should expect to find the following
    * results produced (assuming there are X total manual mapping matches
    * and Y naive matches discarded by the manual mapping process):
    * <UL>
    *    <LI>Total Manual Mapping Matches: X</LI>
    *    <LI>Total Auto Mapping Matches: 0</LI>
    *    <LI>Correct Match: 0</LI>
    *    <LI>Correct Discard: Y</LI>
    *    <LI>Failed To Match: X</LI>
    *    <LI>Incorrect Match: 0</LI>
    *    <LI>% Coverage of OK-OK MM: 0%</LI>
    *    <LI>% Coverage of Discard-Discard MM: 100%</LI>
    * </UL>
    * If these are not the results produced then there is an error either
    * somewhere within the code or with the manual mapping file.  For
    * version 1.0 of VxC, X is 139 and Y is 264.
    *
    * @return <CODE>false</CODE>
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean nullMatch()
   {
      return false;
   }

   /**
    * Signals to discard a possible match between a VN frame's syntax and
    * a Cyc rule if the verbs (lemmas) are unequal.  Its constraint symbol is <CODE>n</CODE>.
    * This is a special constraint because it must always
    * be applied before the other constraints (besides the null constraint).  It makes no sense
    * to compare the syntax for the verb 'jump' in a verb-frame pair
    * and a Cyc rule for the verb 'jest'.  Using this constraint alone
    * can also be used for debugging purposes.
    * Using this constraint (-An on command-line), one should expect to find the following
    * results produced (assuming there are X total manual mapping matches
    * and Y naive matches discarded by the manual mapping process):
    * <UL>
    *    <LI>Total Manual Mapping Matches: X</LI>
    *    <LI>Total Auto Mapping Matches: X+Y</LI>
    *    <LI>Correct Match: X</LI>
    *    <LI>Correct Discard: 0</LI>
    *    <LI>Failed To Match: 0</LI>
    *    <LI>Incorrect Match: Y</LI>
    *    <LI>% Coverage of OK-OK MM: 100%</LI>
    *    <LI>% Coverage of Discard-Discard MM: 0%</LI>
    * </UL>
    * If these are not the results produced then there is an error either
    * somewhere within the code or with the manual mapping file.  For
    * version 1.0 of VxC, X is 139 and Y is 264.
    *
    * @param vnVerb the verb from the verb-frame pair
    * @param cycVerb the verb from the Cyc rule
    * @return <CODE>true</CODE> if the lemma for the verb-frame pair matches the
    *         lemma in the Cyc rule, <CODE>false</CODE> otherwise
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean naive( String vnVerb, String cycVerb )
   {
      return vnVerb.equals( cycVerb );
   }

   /**
    * Signals to discard a possible match if the prepositions in both
    * the VerbNet syntax and the Cyc rule do not correspond. Its constraint
    * symbol is <CODE>p</CODE>. If the Cyc rule requires a
    * specific preposition but the VerbNet frame syntax either does not
    * specify a verb at all after the verb, or it does specify one or more
    * prepositions but the prepositions that follow the verb do not include the preposition
    * in the Cyc rule, then the possible match is invalid.  If the Cyc rule
    * does not require a preposition then <CODE>true</CODE> is returned.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.Cyc#cycHasPreposition(String)
    * @see vn.Cyc#cycExtractPreposition(String)
    * @see vn.PrepositionManager#prepInClass(String, String)
    */
   static boolean cycPreposition( String vnSyntax, String cycRule )
   {

      // If the Cyc rule does not require a preposition, then
      // this constraint method does not have an opinion on
      // whether this possible match is good.
      if( !Cyc.cycHasPreposition( cycRule ) )
         return true;

      boolean isPrepClass;

      // Find location of verb in syntax string.
      int verbLoc = vnSyntax.indexOf( " V" );

      // Scan the string looking for prepositions and comparing
      // those prepositions found against the required preposition
      // in the Cyc rule.
      for( int s = verbLoc; s < vnSyntax.length(); s++ )
      {

         // If a preposition was found (signified by {...})...
         if( vnSyntax.charAt( s ) == '{' )
         {
            s++;

            // If this preposition is actually a preposition class
            // (signified by {{...}}), set appropriate flag.
            if( vnSyntax.charAt( s ) == '{' )
            {
               isPrepClass = true;
               s++;
            }
            else
               isPrepClass = false;

            // Grab the text between the { } or {{ }} and the required
            // preposition from the Cyc rule...
            String inside = vnSyntax.substring( s, vnSyntax.indexOf( '}', s ) );
            String prep = Cyc.cycExtractPreposition( cycRule );

            // If this is a preposition class...
            if( isPrepClass )
            {

               // If this is an or (|) situation, then just check
               // to see if the Cyc preposition is in any of the
               // preposition classes given.
               if( inside.indexOf( "&" ) == -1 )
               {
                  String[] allInside = inside.split( " \\| " );      // Break into array.

                  // Check to see if the Cyc preposition is inside any of the
                  // preposition classes in the or (|) list.
                  for( int p = 0; p < allInside.length; p++ )
                  {

                     // All of the classes in an or chain should have + signs.
                     if( allInside[ p ].charAt( 0 ) != '+' )
                     {
                        eprintln( "ERROR: Matcher.cycPreposition(String, String) - Or error" );
                        System.exit( 0 );
                     }

                     if( PrepositionManager.prepInClass( allInside[ p ].substring( 1 ), prep ) )
                        return true;
                  }
               }

               // Else if this is an and (&) situation, make sure
               // the Cyc preposition is in one class, but not the other.
               else
               {
                  String[] allInside = inside.split( " & " );        // Break into array.

                  // In VerbNet, for a preposition class that involves an and, always
                  // one is + and one is -.
                  if( allInside.length != 2 ||
                      ( allInside[ 0 ].charAt( 0 ) != '+' || allInside[ 1 ].charAt( 0 ) != '-' ) )
                  {
                     eprintln( "ERROR: Matcher.cycPreposition(String, String) - And error" );
                     System.exit( 0 );
                  }

                  // If the Cyc preposition is in the first class with the + and not in the
                  // second class with the -, then it is OK.
                  if( PrepositionManager.prepInClass( allInside[ 0 ].substring( 1 ), prep ) &&
                      !PrepositionManager.prepInClass( allInside[ 1 ].substring( 1 ), prep ) )

                     return true;
               }
            }

            // Else if there is just a list of prepositions supplied...
            else
            {
               String[] preps = inside.split( ", " );    // Break into array.

               // Return valid if any one of them matches the Cyc preposition.
               for( int p = 0; p < preps.length; p++ )
                  if( preps[ p ].equals( prep ) )
                     return true;
            }
         }
      }

      return false;
   }

   /**
    * Signals to discard a possible match if the transitivity in both
    * the VerbNet syntax and the Cyc rule do not correspond.  Its constraint
    * symbol is <CODE>t</CODE>.  One of the
    * arguments for the <CODE>verbSemTrans</CODE> predicate is a frame type
    * which describes the transitivity.  In the VerbNet syntax, if
    * there is a % sign following the verb (i.e. a noun phrase) then the frame is assumed to
    * be transitive.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    * @see vn.Cyc#cycExtractTrans(String)
    */
   static boolean transitivity( String vnSyntax, String cycRule )
   {
      char key;         // First non-whitespace character after V.

      // Find location of verb in syntax string.
      int verbLoc = vnSyntax.indexOf( " V" );

      // If the verb is at the end of the string, set the first non-whitespace
      // character after the V to anything but %.
      if( vnSyntax.charAt( vnSyntax.length() - 1 ) == 'V' )
         key = 'N';

      // Else grab the actual first non-whitespace character after the V.
      else
         key = vnSyntax.charAt( verbLoc + 3 );

      String trans = Cyc.cycExtractTrans( cycRule );

      // If the Cyc rule is basic transitive or DitransitivePPFrameType
      // (which is used in verb-preposition combos with an object), then return
      // true if the VerbNet syntax is also transitive, and false otherwise.
      if( trans.equals( "T" ) || trans.equals( "DPPFT" ) )
         return ( key == '%' );

      // Else if the Cyc rule is basic intransitive,
      // middle voice (which cannot take an object) or TransitivePPFrameType
      // (which is used with verb-preposition combos without an object) then
      // return true if the VerbNet syntax is also intransitive, and false
      // otherwise.
      else if( trans.equals( "I" ) || trans.equals( "M" ) || trans.equals( "TPPFT" ) )
         return ( key != '%' );

      // Else if the Cyc rule is some other type of frame, then this constraint
      // method has no opinion on the matter.
      else
         return true;
   }

   /**
    * Signals to discard a possible match if the infinitive or gerund nature in both
    * the VerbNet syntax and the Cyc rule do not correspond.  Its constraint symbol
    * is <CODE>i</CODE>.  This constraint
    * only operates on cases where either the VerbNet syntax or the Cyc rule
    * involves an infinitive or gerund (i.e. "He continued <I>painting</I>").
    * If neither involves an infinitive or gerund, then <CODE>true</CODE>
    * is returned.  If one of them involves an infinitive, then the other must as
    * well.  If one of them involves a gerund, then the other must as well.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean infinitiveGerund( String vnSyntax, String cycRule )
   {

      // Grab whether Cyc or VerbNet has an infinitive construction.
      boolean cinf = cycRule.indexOf( ":INF-COMP" ) != -1;
      boolean vinf = vnSyntax.indexOf( "inf" ) != -1;

      // Grab whether Cyc or VerbNet has a gerund construction.
      boolean cger = cycRule.indexOf( ":GERUND" ) != -1;
      boolean vger = vnSyntax.indexOf( "ing" ) != -1;

      // Grab whether VerbNet has specific 'ing' syntax restrictions
      // that don't actually function as gerunds.
      boolean bad  = vnSyntax.indexOf( "poss_ing" ) != -1 ||
                     vnSyntax.indexOf( "ac_ing" ) != -1;

      // If VerbNet has the non-gerund 'ing' syntax restrictions,
      // then it does not really have a gerund.
      if( bad )
         vger = false;

      // Return true if both VerbNet and Cyc behave the same for
      // infinitives and gerunds (they either both have one
      // or they both do not have one).
      return cinf == vinf && cger == vger;
   }

   /**
    * Signals to discard a possible match if the adjectives in both
    * the VerbNet syntax and the Cyc rule do not correspond.  Its constraint symbol
    * is <CODE>j</CODE>.  The VerbNet syntax and the Cyc rule have to either both
    * involve an adjective construction or both not involve an adjective construction.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean adjective( String vnSyntax, String cycRule )
   {

      // Grab whether Cyc or VerbNet has an adjective inside.
      boolean cadj = cycRule.indexOf( ":ADJ" ) != -1;
      boolean vadj = vnSyntax.indexOf( "ADJ" ) != -1;

      // Return true if both VerbNet and Cyc behave the same for
      // adjectives (they either both have one or they both do
      // not have one).
      return cadj == vadj;
   }

   /**
    * Signals to discard a possible match if the Cyc rule specifies
    * the predicate 'fromLocation' for the direct object and VerbNet does not
    * have 'Source' for the noun phrase immediately following the Verb.  Its constraint symbol
    * is <CODE>f</CODE>.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean fromLocationImpliesSource( String vnSyntax, String cycRule )
   {

      // Search for the object of the Cyc rule.
      int obj = cycRule.indexOf( ":OBJECT" );

      // If there is none, then this constraint will not apply so make no
      // judgement concerning this possible match.
      if( obj == -1 )
         return true;

      // Find the parenthesis of the predicate that contains the object token.
      int lp = cycRule.lastIndexOf( "(", obj );

      // Grab the predicate of the rule with the object token.
      String pred = cycRule.substring( lp + 1, cycRule.indexOf( " ", lp ) );

      // If it is 'fromLocation' then search for the Source thematic role.
      if( pred.equals( "fromLocation" ) )
      {

         // If the verb is at the end of the sentence then there is no match
         // (an object is required).
         if( vnSyntax.charAt( vnSyntax.length() - 1 ) == 'V' )
            return false;

         // Grab the location of the verb.
         int verbLoc = vnSyntax.indexOf( " V" );

         // If the token that follows the verb is not a noun phrase
         // then it does not have an object - there is no match.
         if( vnSyntax.charAt( verbLoc + 3 ) != '%' )
            return false;

         // Find the space after the noun phrase that follows the verb.
         int spc = vnSyntax.indexOf( " ", verbLoc + 3 );

         String nextNp;

         // If there was no space, then just extract all the letters
         // to the end of the string, else extract the letters to to the space.
         if( spc == -1 )
            nextNp = vnSyntax.substring( verbLoc + 4 );
         else
            nextNp = vnSyntax.substring( verbLoc + 4, spc );

         // There is a match if the noun phrase thematic role that follows
         // the verb is 'Source', else there is no match.
         return nextNp.equals( "Source" );
      }

      // Else if this Cyc rule does not have the correct predicate, make
      // no judgement concerning this possible match.
      else
         return true;
   }

   /**
    * Signals to discard a possible match if the Cyc rule specifies
    * the predicates 'doneBy' or 'performedBy' for the subject and VerbNet does not
    * have 'Agent' for the noun phrase immediately preceding the Verb.  Its constraint symbol
    * is <CODE>d</CODE>.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean DBPBimpliesAgent( String vnSyntax, String cycRule )
   {

      // Search for the subject of the Cyc rule.
      int subj = cycRule.indexOf( ":SUBJECT" );

      // If there is none, then this constraint will not apply so make no
      // judgement concerning this possible match.
      if( subj == -1 )
         return true;

      // Find the parenthesis of the predicate that contains the subject token.
      int lp = cycRule.lastIndexOf( "(", subj );

      // Grab the predicate of the rule with the subject token.
      String pred = cycRule.substring( lp + 1, cycRule.indexOf( " ", lp ) );

      // If it is 'doneBy' or 'performedBy' then look for the Agent
      // thematic role on the noun phrase preceding the verb.
      if( pred.equals( "doneBy" ) || pred.equals( "performedBy" ) )
      {

         // Grab the location of the verb.
         int verbLoc = vnSyntax.indexOf( " V" );

         // Grab the starting location of the noun phrase that
         // immediately precedes the verb.
         int prevNpLoc = vnSyntax.lastIndexOf( "%", verbLoc );

         // If there is no noun phrase before the verb, then
         // there is no match (it must be Agent).
         if( prevNpLoc == -1 )
            return false;

         // Extract the noun phrase before the verb.
         String prevNp = vnSyntax.substring( prevNpLoc + 1, vnSyntax.indexOf( " ", prevNpLoc ) );

         // There is a match if the noun phrase thematic role that follows
         // the verb is 'Agent', else there is no match.
         return prevNp.equals( "Agent" );
      }

      // Else if neither the 'doneBy' predicate nor the 'performedBy'
      // predicate contains the subject token, make no judgement concerning
      // this possible match.
      else
         return true;
   }

   /**
    * Signals to discard a possible match if the Cyc rule is of type
    * <CODE>MiddleVoiceFrame</CODE> and VerbNet specifies 'Agent' for the noun
    * phrase immediately preceding the verb.  With middle voice, there
    * should not be an 'Agent' as the subject of the verb, only 'Theme', 'Experiencer', etc.
    * Its constraint symbol is <CODE>m</CODE>.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean middleVoiceNoAgent( String vnSyntax, String cycRule )
   {

      // If the Cyc rule is MiddleVoiceFrame, make sure the noun phrase
      // thematic role immediately preceding the verb is not 'Agent'.
      if( Cyc.cycExtractTrans( cycRule ).equals( "M" ) )
      {

         // Grab the location of the verb.
         int verbLoc = vnSyntax.indexOf( " V" );

         // Grab the starting location of the noun phrase that
         // immediately precedes the verb.
         int prevNpLoc = vnSyntax.lastIndexOf( "%", verbLoc );

         // If there is no noun phrase preceding the verb then
         // 'Agent' is not preceding the verb so return true.
         if( prevNpLoc == -1 )
            return true;

         // Extract the noun phrase before the verb.
         String prevNp = vnSyntax.substring( prevNpLoc + 1, vnSyntax.indexOf( " ", prevNpLoc ) );

         // Return true if and only if the noun phrase preceding
         // the verb is not 'Agent'.
         return !prevNp.equals( "Agent" );
      }

      // Else if the Cyc rule is not MiddleVoiceFrame then make
      // no judgement concerning this possible match.
      else
         return true;
   }

   ////////////////////////////////////////////
   // Weak Constraint Methods (bad accuracy) //
   ////////////////////////////////////////////

   /**
    * Signals to discard a possible match if the Cyc rule specifies
    * the predicates 'doneBy' or 'performedBy' for the subject and VerbNet does not
    * have 'cause(Agent' appearing somewhere in the semantics for the verb-frame pair.  Its constraint symbol
    * is <CODE>c</CODE>.  NOTE: Although this method is made visible in
    * the Javadoc documentation - it is NOT activated.  Its constraint symbol
    * is not valid and it is not included when -Aa is provided. It produced less than
    * desirable results when applied to the possible matches.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param vnSem the semantics from the verb-frame pair for this possible match in one continuous string.
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean bad_DBPBimpliesCause( String vnSyntax, String vnSem, String cycRule )
   {

      // Search for the subject of the Cyc rule.
      int subj = cycRule.indexOf( ":SUBJECT" );

      // If there is none, then this constraint will not apply so make no
      // judgement concerning this possible match.
      if( subj == -1 )
         return true;

      // Find the parenthesis of the predicate that contains the subject token.
      int lp = cycRule.lastIndexOf( "(", subj );

      // Grab the predicate of the rule with the subject token.
      String pred = cycRule.substring( lp + 1, cycRule.indexOf( " ", lp ) );

      // If it is 'doneBy' or 'performedBy' then look for the cause token
      // in the VerbNet semantics.  There is a match if and only if it exists.
      if( pred.equals( "doneBy" ) || pred.equals( "performedBy" ) )
         return vnSem.indexOf( "cause(Agent" ) != -1;

      // Else if neither the 'doneBy' predicate nor the 'performedBy'
      // predicate contains the subject token, make no judgement concerning
      // this possible match.
      else
         return true;
   }

   /**
    * Signals to discard a possible match if the VerbNet syntax specifies
    * 'Agent' as the noun phrase that immediately precedes the verb and the Cyc rule
    * does not have the predicates 'doneBy' or 'performedBy' for the subject of the verb.
    * Its constraint symbol
    * is <CODE>e</CODE>.  NOTE: Although this method is made visible in
    * the Javadoc documentation - it is NOT activated.  Its constraint symbol
    * is not valid and it is not included when -Aa is provided. It produced less than
    * desirable results when applied to the possible matches.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   static boolean bad_agentImpliesDBPB( String vnSyntax, String cycRule )
   {

      // Grab the location of the verb.
      int verbLoc = vnSyntax.indexOf( " V" );

      // Grab the starting location of the noun phrase that
      // immediately precedes the verb.
      int prevNpLoc = vnSyntax.lastIndexOf( "%", verbLoc );

      // If there is none, then this constraint will not apply so make no
      // judgement concerning this possible match.
      if( prevNpLoc == -1 )
         return true;

      // Extract the noun phrase before the verb.
      String prevNp = vnSyntax.substring( prevNpLoc + 1, vnSyntax.indexOf( " ", prevNpLoc ) );

      // If the noun phrase thematic role is 'Agent', make sure the Cyc
      // rule contains 'doneBy' or 'performedBy'.
      if( prevNp.equals( "Agent" ) )
      {

         // Search for the subject of the Cyc rule.
         int subj = cycRule.indexOf( ":SUBJECT" );

         // If there is none, there is no match (because since
         // Agent is in the VerbNet syntax, there must be a subject).
         if( subj == -1 )
            return false;

         // Find the parenthesis of the predicate that contains the subject token.
         int lp = cycRule.lastIndexOf( "(", subj );

         // Grab the predicate of the rule with the subject token.
         String pred = cycRule.substring( lp + 1, cycRule.indexOf( " ", lp ) );

         // Return true if and only if the predicate is 'doneBy' or 'performedBy'
         // (otherwise there is no match).
         return pred.equals( "doneBy" ) || pred.equals( "performedBy" );
      }

      // Else if Agent is not the noun phrase thematic role that precedes
      // the verb, make no judgement concerning this possible match.
      else
         return true;
   }

   /* * >> TEMPLATE OF CONSTRAINT METHOD: Copy it, remove comments around body,
    *   >> modify this Javadoc comment and remove space from /* *.
    * Signals to discard a possible match if XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX in both
    * the VerbNet syntax and the Cyc rule do not correspond.  Its constraint symbol
    * is <CODE>?</CODE>.
    *
    * @param vnSyntax the syntax from the verb-frame pair for this possible match
    * @param cycRule the Cyc rule for this possible match
    * @return <CODE>false</CODE> if this possible match does not meet the constraint,
    *         <CODE>true</CODE> if it does meet the constraint or the constraint does not apply to the
    *         given possible match
    * @see vn.Cyc#findCycMatches(String, String, String, int, String)
    */
   /*
   static boolean constraintName( String vnSyntax, String cycRule )
   {
   }
   */
}

