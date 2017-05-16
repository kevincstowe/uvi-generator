package semlink.apps.uvig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import storage.Storage;
import syntax.Role;
import syntax.Restriction;
import tools.Tools;
import verbnet.Predicate;
import verbnet.SemanticFrame;
import verbnet.SyntacticFrame;
import verbnet.VerbNet;
import verbnet.VerbNetClass;
import verbnet.FullFrame;
import verbnet.VerbNetEntry;

public class ReferencePages {
	private static final int INIT_MEM_ROW = 5;
	private static final int NUM_MEM_COL = 4;
	
	private static int memPerCol = 4;
	private static int curMem;
	
	//All static methods, no instantiation
	private ReferencePages(){}
	
	private static Map<Restriction, Set<String>> getRestrictions(String category, String type){
		Map<Restriction, Set<String>> restrictionMap = new HashMap<Restriction, Set<String>>();
		
		for (VerbNetClass vnc : VerbNet.getClasses()){
			String value = vnc.toString();
			
			Collection<Role> roles = null;
			if (category.equals("THEMROLES")){
				roles = vnc.getExtendedRoles();
			}
			else if (category.equals("SYNTAX")){
				roles = new ArrayList<Role>();
				for (SyntacticFrame sf : vnc.getSyntacticFrames()){
					roles.addAll(sf.getRoles());
				}
			}
			
			for (Role r : roles){
				Collection<Restriction> restrictions = null;
				if (type.equals("SEL")){
					restrictions = r.getSelRestrictions();
				}
				else if (type.equals("SYN")){
					restrictions = r.getSynRestrictions();
				}
				for (Restriction s : restrictions){
					if (restrictionMap.get(s) != null){
						restrictionMap.get(s).add(value);
					}
					else{
						Set<String> l = new TreeSet<String>();
						l.add(value);
						if (s != null)
							restrictionMap.put(s, l);
					}
				}
			}
		}
		return restrictionMap;
			
	}
	
	
	private static Map<String, Set<String>> getPredMap(){
		Map<String, Set<String>> predMap = new HashMap<String, Set<String>>();
		for (VerbNetClass vnc : VerbNet.getClasses()){
			String className = vnc.toString();
			for (SemanticFrame sf : vnc.getSemanticFrames()){
				for (Predicate p : sf.predicates){
					String predName = p.value.toString();
					if (predMap.get(predName) != null){
						predMap.get(predName).add(className);
					}
					else{
						Set<String> l = new TreeSet<String>();
						l.add(className);
						if (predName != null)
							predMap.put(predName, l);
					}
				}
			}
		}
		return predMap;
	}
	
	private static Map<String, Set<String>> getRoleMap(){
		Map<String, Set<String>> roleMap = new HashMap<String, Set<String>>();
		for (VerbNetClass vnc : VerbNet.getClasses()){
			System.out.println(vnc);
			String value = vnc.toString();
			for (Role r : vnc.getExtendedRoles()){
				String roleName = r.getRoleValue().toString();
				if (roleMap.get(roleName) != null){
					roleMap.get(roleName).add(value);
				}
				else{
					Set<String> l = new TreeSet<String>();
					l.add(value);
					if (roleName != null)
						roleMap.put(roleName, l);
				}
			}
		}
		return roleMap;
	}
	
	
	
	private static void printSelectionalRestrictionBox(Set<String> members, String title){
		Q.oh( 2, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
        Q.oh( 3, "<TR class='ClassSectionHeadRow'>" );
        Q.oh( 4, "<TD>" + title + "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 3, "<TR>" );
        Q.oh( 4, "<TD colspan=2>" );
        
        if (members != null)
        	printClasses(members.size(), members);
        else
        	printClasses(0, members);
      
        Q.oh( 4, "</TD>");
        Q.oh( 3, "</TR>");
        Q.oh( 2, "</TABLE>");
	}
	
	static void generateSelectionalReference(File directory){
		VerbNet.buildVerbNet(Storage.VERBNET_LOCATION);
		
		Map<Restriction, Set<String>> themSelectionalMap = getRestrictions("THEMROLES", "SEL");
		Map<Restriction, Set<String>> syntaxSelectionalMap = getRestrictions("SYNTAX", "SEL");
		
		Set<String> stringKeys = new TreeSet<String>();
		for (Restriction key : themSelectionalMap.keySet()){
			stringKeys.add(key.value);
		}
		for (Restriction key : syntaxSelectionalMap.keySet()){
			stringKeys.add(key.value);
		}
		
		for (String s : stringKeys){
			if (s != null){
				PrintWriter pw = Generator.createHTMLOutStream("/selrestrs/" + s + ".php");
				Q.setWriter(pw);
				
				Q.oh(2, "<h2>Selectional Restriction : " + s + "</h2>");
				//Print all the +selectional_restriction from thematic roles
				Restriction sr = new Restriction(s, true);				

				printSelectionalRestrictionBox(themSelectionalMap.get(sr), "Classes containing +" + sr.value.toUpperCase() + " (in ThemRoles) : " + ((themSelectionalMap.get(sr) != null) ? themSelectionalMap.get(sr).size() : 0));
				//Now find the -selectional_restrictions
				sr.pos = false;
				printSelectionalRestrictionBox(themSelectionalMap.get(sr), "Classes containing -" + sr.value.toUpperCase() + " (in ThemRoles) : " + ((themSelectionalMap.get(sr) != null) ? themSelectionalMap.get(sr).size() : 0));
				
				//Same for syntactic frame selrestrs
				sr.pos = true;
				printSelectionalRestrictionBox(syntaxSelectionalMap.get(sr), "Classes containing +" + sr.value.toUpperCase() + " (in Frames) : " + ((syntaxSelectionalMap.get(sr) != null) ? syntaxSelectionalMap.get(sr).size() : 0));
				sr.pos = false;
				printSelectionalRestrictionBox(syntaxSelectionalMap.get(sr), "Classes containing -" + sr.value.toUpperCase() + " (in Frames) : " + ((syntaxSelectionalMap.get(sr) != null) ? syntaxSelectionalMap.get(sr).size() : 0));
		
		        Generator.closeHTMLOutStream( pw, "/selrestrs/" + s + ".php" );
			}
		}		
	}

	static void generateVerbFeatureReference(File directory){
		VerbNet.buildVerbNet(Storage.VERBNET_LOCATION);
		
		Map<Restriction, Set<String>> featuresToVerbsMap = new HashMap<Restriction, Set<String>>();
		
		for (VerbNetEntry vne : VerbNet.getVerbEntries()){
			if (vne.getFeatures() != null){
				System.out.println(vne);
				for (String f : vne.getFeatures()){
					Restriction r = Restriction.parseRestriction(f);
					if (featuresToVerbsMap.keySet().contains(r)){
						featuresToVerbsMap.get(r).add(vne.getVerb() + " " + vne.getVerbClass());
					}
					else{
						Set<String> verbs = new HashSet<String>();
						verbs.add(vne.getVerb() + " " + vne.getVerbClass());
						featuresToVerbsMap.put(r, verbs);
					}
				}
			}
		}
		
		List<String> stringKeys = new ArrayList<String>();
		
		for (Restriction r : featuresToVerbsMap.keySet()){
			stringKeys.add(r.value);
		}
		
		for (String s : stringKeys){
			if (s != null){
				PrintWriter pw = Generator.createHTMLOutStream("/verbfeatures/" + s + ".php");
				Q.setWriter(pw);
				
				Q.oh(2, "<h2>Verb-Specific Features : " + s + "</h2>");
				Restriction sr = new Restriction(s, true);				
	
				sr.pos = true;
				printSelectionalRestrictionBox(featuresToVerbsMap.get(sr), "Classes containing +" + sr.value.toUpperCase() + " : " + ((featuresToVerbsMap.get(sr) != null) ? featuresToVerbsMap.get(sr).size() : 0));
				sr.pos = false;
				printSelectionalRestrictionBox(featuresToVerbsMap.get(sr), "Classes containing -" + sr.value.toUpperCase() + " : " + ((featuresToVerbsMap.get(sr) != null) ? featuresToVerbsMap.get(sr).size() : 0));
		
		        Generator.closeHTMLOutStream( pw, "/verbfeatures/" + s + ".php" );
			}
		}		
	}

	
	static void generateSyntacticReference(File directory){
		VerbNet.buildVerbNet(Storage.VERBNET_LOCATION);
		
		Map<Restriction, Set<String>> syntaxSelectionalMap = getRestrictions("SYNTAX", "SYN");
		
		Set<String> stringKeys = new TreeSet<String>();
		for (Restriction key : syntaxSelectionalMap.keySet()){
			stringKeys.add(key.value);
		}
		
		for (String s : stringKeys){
			if (s != null){
				PrintWriter pw = Generator.createHTMLOutStream("/synrestrs/" + s + ".php");
				Q.setWriter(pw);
				
				Q.oh(2, "<h2>Syntactic Restriction : " + s + "</h2>");
				//Print all the +selectional_restriction from thematic roles
				Restriction sr = new Restriction(s, true);				
	
				//Same for syntactic frame selrestrs
				sr.pos = true;
				printSelectionalRestrictionBox(syntaxSelectionalMap.get(sr), "Classes containing +" + sr.value.toUpperCase() + " : " + ((syntaxSelectionalMap.get(sr) != null) ? syntaxSelectionalMap.get(sr).size() : 0));
				sr.pos = false;
				printSelectionalRestrictionBox(syntaxSelectionalMap.get(sr), "Classes containing -" + sr.value.toUpperCase() + " : " + ((syntaxSelectionalMap.get(sr) != null) ? syntaxSelectionalMap.get(sr).size() : 0));
		
		        Generator.closeHTMLOutStream( pw, "/synrestrs/" + s + ".php" );
			}
		}		
	}
	
	static void generateThemRolesReference(File directory){	
		VerbNet.buildVerbNet(Storage.VERBNET_LOCATION);
		//This needs to come from the supplemental files, or somewhere else - Kevin	
		Map<String, String> roleDescriptions = generateDescriptions(new File("/home/kevin/UVIG/trunk/web/uvig/supplemental/them_role_descriptions.s"));
		Map<String, Set<String>> roleMap = getRoleMap();
		
		for (String r : roleMap.keySet()){
			if (r != null){	
				Set<String> classes = roleMap.get(r);
				
				r = r.replace("-", "_");
				PrintWriter pw = Generator.createHTMLOutStream("/themroles/" + r + ".php");
				Q.setWriter(pw);
				Q.oh(2, "<h2>Thematic Role : " + r + "</h2>");
				if (roleDescriptions.keySet().contains(r)){
					Q.oh(2, "<h3>Description : " + roleDescriptions.get(r).split("\n")[0] + "</h3>");				
					if (roleDescriptions.get(r).contains("Example"))
						Q.oh(2, "<h3>" + roleDescriptions.get(r).split("\n")[1] + "</h3>");
				}
				Q.oh( 2, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
		        Q.oh( 3, "<TR class='ClassSectionHeadRow'>" );
		        Q.oh( 4, "<TD>Classes containing " + r.toUpperCase() + " : " + classes.size() + "</TD>" );
		        Q.oh( 3, "</TR>" );
		        Q.oh( 3, "<TR>" );
		        Q.oh( 4, "<TD colspan=2>" );

		        printClasses(classes.size(), classes);

		        Generator.closeHTMLOutStream( pw, "/themroles/" + r + ".php" );
			}
		}
	}
	
	private static Map<String, String> generateDescriptions(File f){
		Map<String, String> descriptions = new HashMap<String, String>();
		String data = "";
		for (String s : Tools.dumbFileRead(f)){
			data += s;
		}
		String[] dataPoints = data.split("//");
		for (String s : dataPoints){
			if (s.contains("Example:")){
				String lineOne = s.split(":")[1].split("Example")[0] + "\n";
				String lineTwo = "Example : " + s.split("Example:")[1];
				descriptions.put(s.split(":")[0], lineOne + lineTwo);
			}
			else{
				descriptions.put(s.split(":")[0], s.split(":")[1]);
			}
		}
		return descriptions;
	}
	
	public static void generatePredicateReference(File directory){
		VerbNet.buildVerbNet(Storage.VERBNET_LOCATION);
		Map<String, Set<String>> predMap = getPredMap();
		
		for (String p : predMap.keySet()){
			if (p != null){	
				Set<String> classes = predMap.get(p);
				
				PrintWriter pw = Generator.createHTMLOutStream("/predicates/" + p + ".php");
				Q.setWriter(pw);
				Q.oh(2, "<h2>Predicate : " + p + "</h2>");
				
		        Q.oh( 2, "<TABLE class='ClassSectionBox' cellspacing=0 cellpadding=4 width='100%'>" );
		        Q.oh( 3, "<TR class='ClassSectionHeadRow'>" );
		        Q.oh( 4, "<TD>Classes containing " + p.toUpperCase() + " : " + classes.size() + "</TD>" );
		        Q.oh( 3, "</TR>" );
		        Q.oh( 3, "<TR>" );
		        Q.oh( 4, "<TD colspan=2>" );

		        printClasses(classes.size(), classes);

		        Generator.closeHTMLOutStream( pw, "/predicates/" + p + ".php" );
			}
		}
		
	}

	private static void printClasses(int totalMem, Collection<String> classes){
        if( totalMem == 0 ) {
            Q.oh( 5, "&nbsp;&nbsp;&nbsp;&nbsp;<FONT class='AbsenceOfItems'>no members</FONT>" );
        } else
        {
            Q.oh( 5, "<TABLE width='100%'>" );
            Q.oh( 6, "<TR valign='top'>" );

            
            // Calculate the number of members per column.
            if( totalMem < INIT_MEM_ROW ) {
                memPerCol = totalMem;
            } else if( totalMem <= NUM_MEM_COL * INIT_MEM_ROW ) {
                memPerCol = INIT_MEM_ROW;
            } else
            {
                int extra = ( totalMem % NUM_MEM_COL == 0 ) ? 0 : 1;
                memPerCol = ( int ) ( ( ( double ) totalMem / NUM_MEM_COL ) + extra );
            }

            curMem = 0;
        }
        
        if( totalMem != 0 )
        {
        	printMembers(classes);
            // Print all the HTML for the class members whose information was collected
            // and stored during the <MEMBER> tag scanning.

            if( curMem % memPerCol != 0 )
            {
                while( curMem % memPerCol != 0 )
                {
                    Q.oh( 9, "<TR><TD>&nbsp;</TD></TR>" );
                    curMem++;
                }

                Q.oh( 8, "</TABLE>" );
                Q.oh( 7, "</TD>" );
            }

            // Fill the rest of the table with empty cells.
            if( totalMem >= INIT_MEM_ROW ) {
                while( curMem < NUM_MEM_COL * INIT_MEM_ROW )
                {
                    Q.oh( 7, "<TD width='25%' align='left'>" );
                    Q.oh( 8, "<TABLE class='YesCaps'>" );
                    Q.oh( 9, "<TR><TD>&nbsp;</TD></TR>" );
                    curMem++;

                    while( curMem % memPerCol != 0 )
                    {
                        Q.oh( 9, "<TR><TD>&nbsp;</TD></TR>" );
                        curMem++;
                    }

                    Q.oh( 8, "</TABLE>" );
                    Q.oh( 7, "</TD>" );
                }
            }

            Q.oh( 6, "</TR>" );
            Q.oh( 5, "</TABLE>" );
        }

        Q.oh( 4, "</TD>" );
        Q.oh( 3, "</TR>" );
        Q.oh( 2, "</TABLE>" );
        Q.oh( 2, "" );
        Q.oh( 2, "<BR>" );
        Q.oh( 2, "" );
	}
	
	private static void printMembers(Collection<String> members)
    {
		List<String> membersList = new ArrayList<String>(members);
		Collections.sort(membersList);
		Iterator<String> i = membersList.iterator();
        while(i.hasNext())
        {

            // Extract parts of member information.
            String className = i.next();
            
            // Start the current column.
            if( curMem % memPerCol == 0 )
            {
                Q.oh( 7, "<TD width='25%' align='left'>" );
                Q.oh( 8, "<TABLE class='YesCaps'>" );
            }

            String linkPath = className;
            String verbName = "";
            //Change verb-specific to remove the verb and just link the class
            if (className.contains(" ")){
            	verbName = className.split(" ")[0] + " ";
            	className = className.substring(className.indexOf(" ")+1);
            }
            
            //Change link to appropriate subclass, if it's a subclass
            if (className.subSequence(className.indexOf("-"), className.length()).toString().contains("-")){
            	linkPath = className.split("-")[0] + "-" + className.split("-")[1] + ".php#" + className;
            }
            else{
            	linkPath = className;
            }
            
            linkPath = "<a href=\"../vn/" + linkPath.trim() + ".php\" class=verbLinks>";

            
            
            Q.oh( 9, "<TR><TD class='MemberCell'>" + linkPath + verbName + className + "</TD></TR>" );


            // If this is the last member in the column, close the column.
            if( curMem % memPerCol == memPerCol - 1 )
            {
                Q.oh( 8, "</TABLE>" );
                Q.oh( 7, "</TD>" );
            }

            // Increment the member counter.
            curMem++;
        }
    }

}
