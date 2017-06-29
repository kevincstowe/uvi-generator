package semlink.apps.verbnet;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.*;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.input.sax.SAXBuilderEngine;

import tools.Constants;
import tools.Log;
import tools.Relations;
import tools.Tools;

import semlink.apps.verbnet.RoleValue;
import semlink.apps.verbnet.Role;
import semlink.apps.verbnet.Restriction;
import semlink.apps.verbnet.SyntacticFrame;


/** 
 * VerbNet
 * <br>
 * <br>Currently functions as static class
 * <br>
 * <br>VerbNet is meant to be immutable and therefore thread-safe.
 * <br>
 * <br>Constructor:
 *  <br>Does all heavy lifting - loads all VerbNet classes from their files,
 *  <br>and all relevant information into those classes.  Uses 'buildClass' helper
 *  <br>method extensively.
 *  <br>
 * <br>Public:
 *  <br>getEntries : Shallow copy of entries
 *  <br>getClasses : Shallow copy of classes
 * 
 * @author Kevin Stowe
 *
 */

public enum VerbNet{
	//Empty enum : static class
	;
	private static Set<VerbNetEntry> entries = new HashSet<VerbNetEntry>();
	private static Set<VerbNetClass> classes = new HashSet<VerbNetClass>();
	
	private static Set<String> res = new TreeSet<String>();
	private static boolean exists = false;
	
	/**
	 * Constructs VerbNet with an optional logging parameter.  If logging is set to true,
	 * full error and information log will be kept.  Logging defaults to false.
	 * 
	 * @param location : the directory containing the VerbNet class files
	 * @param logging : Whether or not to log information about VerbNet construction
	 */
	public static final void buildVerbNet(String location, boolean logging){
		Log.setEnabled(logging);
		buildVerbNet(location);
	}
	
	/**
	 * <br>VerbNet is a static class - it has no instances but it needs
	 * <br>to be initialized by calling this function.
	 * <br>
	 * 
	 * @param location : the directory containing the VerbNet class files
	 */
	public static final void buildVerbNet(String location){
		entries = new HashSet<VerbNetEntry>();
		classes = new HashSet<VerbNetClass>();
		
		/* want multiple possible vns now */
//		if (exists){
			//System.out.println("VerbNet already built.");
//			return;
//		}
		Log.info("VerbNet Construction Underway...");
		long time = System.currentTimeMillis();
		File root = new File(location);

		if (!root.exists() || !root.isDirectory()){
			System.err.println("VerbNet load unsuccessful : Directory not found.\n" + location);
		}
		else {
			//Add a new class for every file in the root directory
			for (File fileEntry : root.listFiles()){
				//Avoid directories and SVN files
				if (!fileEntry.isDirectory() && !fileEntry.getName().contains(".svn") && fileEntry.getName().contains(".xml")){
					classes.addAll(buildClassesJDOM(fileEntry));
				}
			}
			
			//Add all of the entries from the classes to the entries data
			for (VerbNetClass vnc : classes){
				for (VerbNetEntry vne : vnc.getMembers()){
					//vne.verbClass = vnc;
					entries.add(vne);
				}
			}
			time = System.currentTimeMillis()-time;
			exists = true;
			Log.info("VerbNet loaded in " + time + " ms");
		}
	}
	
	private static final Set<VerbNetClass> buildClassesJDOM(File fileEntry){
		//System.out.println(fileEntry);
		SAXBuilder builder = new SAXBuilder();
		Set<VerbNetClass> classes = new HashSet<VerbNetClass>();

		try {
			Document doc = builder.build(fileEntry);
			Element rootNode = doc.getRootElement();
			classes.add(buildClass(rootNode, fileEntry));

			//Recursively find classes
			List<Element> nodesToCheck = new ArrayList<Element>();
			nodesToCheck.add(rootNode);
			Element node;
			while (!nodesToCheck.isEmpty()){
				node = nodesToCheck.remove(0);
				List<Element> subclasses = node.getChild("SUBCLASSES").getChildren("VNSUBCLASS");
				
				for (Element e : subclasses){
					classes.add(buildClass(e, fileEntry));
					nodesToCheck.add(e);
				}
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return classes;
	}
	
		
	private static final VerbNetClass buildClass(Element rootNode, File fileEntry){
		String verbAndId = rootNode.getAttributeValue("ID");
		String verb = verbAndId.split("-")[0];
		String id = verbAndId.substring(verbAndId.indexOf("-")+1, verbAndId.length());
		
		Set<VerbNetEntry> members = new HashSet<VerbNetEntry>();
		
		for (Element e : rootNode.getChildren("MEMBERS").get(0).getChildren()){
			if (e.getName().equals("MEMBER")){
				if (e.getAttributeValue("features") != null)
					members.add(new VerbNetEntry(e.getAttributeValue("name"), e.getAttributeValue("features"), null));
				else
					members.add(new VerbNetEntry(e.getAttributeValue("name"), "", null));
			}
		}
		Set<Role> roles = new HashSet<Role>();
		Element themrolesElement = rootNode.getChild("THEMROLES");
		roles.addAll(getSyntacticRoles(themrolesElement, "THEMROLE"));
		Set<FullFrame> frames = new HashSet<FullFrame>();
		
		for (Element e : rootNode.getChild("FRAMES").getChildren("FRAME")){
			Element description = e.getChild("DESCRIPTION");
			String descNo = description.getAttributeValue("descriptionNumber");
			String primary = description.getAttributeValue("primary");
			String secondary = description.getAttributeValue("secondary");
			Set<String> examples = new HashSet<String>();
			for (Element e2 : e.getChild("EXAMPLES").getChildren("EXAMPLE")){
				examples.add(e2.getText());
			}
			
			Element syntax = e.getChild("SYNTAX");
			
			List<Role> allRoles = getSyntacticRoles(syntax, "NP");
			allRoles.addAll(getSyntacticRoles(syntax, "PREP"));
			
			SyntacticFrame synFrame = new SyntacticFrame(syntax.getAttributeValue("value"), allRoles);

			SemanticFrame sf = new SemanticFrame();
			Element semantics = e.getChild("SEMANTICS");
			for (Element e2 : semantics.getChildren("PRED")){
				Predicate p = new Predicate();
				p.value = e2.getAttributeValue("value");
				Element args = e2.getChild("ARGS");
				for (Element e3 : args.getChildren("ARG")){
					p.args.add(new Arg(e3.getAttributeValue("type"), e3.getAttributeValue("value")));
				}
				sf.predicates.add(p);
			}
			frames.add(new FullFrame(descNo, primary, secondary, examples, synFrame, sf));
		}

		VerbNetClass vnc = new VerbNetClass.Builder(verb, id).sourceFile(fileEntry).members(members).frames(frames).roles(roles).build();

		for (VerbNetEntry vne : members){
			vne.verbClass = vnc;
		}
		return vnc;
	}
	
	private static List<Role> getSyntacticRoles(Element head, String subHead){
		List<Role> synRoles = new ArrayList<Role>();
				
		for (Element synRole : head.getChildren(subHead)){
			Set<Restriction> selRestrictions = new HashSet<Restriction>();
			Set<Restriction> synRestrictions = new HashSet<Restriction>();
			String logic = "and";
			for (Element selRestr : synRole.getChildren("SELRESTRS")){
				if (selRestr.hasAttributes() && selRestr.getAttributeValue("logic").equals("or")){
					logic = "or";
				}
				for (Element item : selRestr.getChildren("SELRESTR")){
					boolean srPos;
					String srType;
					String value = item.getAttributeValue("Value");
					String type = item.getAttributeValue("type");
					if (value != null){
						srPos = (value.equals("+") ? true : false);
					}
					else
						srPos = true;
					srType = type;
					Restriction newRestr = new Restriction(srType, srPos);
					newRestr.logic = logic;
					selRestrictions.add(newRestr);
				}

			}
			
			for (Element selRestr : synRole.getChildren("SYNRESTRS")){
				for (Element item : selRestr.getChildren("SYNRESTR")){
					boolean srPos;
					String srType;
					String value = item.getAttributeValue("Value");
					String type = item.getAttributeValue("type");
					if (value != null){
						srPos = (value.equals("+") ? true : false);
					}
					else
						srPos = true;
					srType = type;
					synRestrictions.add(new Restriction(srType, srPos));
				}
			}
			String roleValue = synRole.getAttributeValue("value");

			if (subHead.equals("THEMROLE")){
				roleValue = synRole.getAttributeValue("type");
			}
			Role r = new Role(roleValue, selRestrictions, synRestrictions);
			synRoles.add(r);
		}
		return synRoles;
	}
	
	/**
	 * public static boolean exists()
	 * 
	 * @return
	 * true if VerbNet has been built, otherwise false
	 */
	public static boolean exists(){
		return exists;
	}

	/**
	 * <br>Checks all of the verbs in VerbNet for the specified word - if the lemma and
	 * <br>POS match a verb in verbnet, returns true.
	 * @param lemma - Word lemma to search for
	 * @param _class - Class that lemma is in
	 * @return
	 * true if VerbNet data contains a word matching the lemma and pos of the given word
	 */
	public static boolean containsEntry(String lemma, String _class){
		if (exists){
			for (VerbNetEntry vne : entries){
				if (vne.getVerb().equals(lemma) && vne.getVerbClass().equals(VerbNetClass.parseClass(_class))){
					return true;
				}
			}
		}
		Log.error("containsEntry(String lemma, String _class) called but VerbNet not built.");
		return false;
	}
	
	/**
	 * Returns a shallow copy of the HashSet of Classes present in VerbNet
	 * 
	 * @return Class objects in VerbNet
	 */
	public static Set<VerbNetClass> getClasses(){
		if (exists){
			return Tools.copySet(classes);
		}

		Log.error("\'getClasses()\' called but VerbNet has not been built.");
		return null;
	}
	
	/**
	 * Returns a shallow copy of the Set of VerbNetEntries present in VerbNet
	 * 
	 * @return VerbNetEntries in VerbNet
	 */
	public static Set<VerbNetEntry> getVerbEntries(){
		if (exists){
			return Tools.copySet(entries);
		}

		Log.error("\'getVerbEntries()\' called but VerbNet has not been built.");
		return null;
	}
	
	/**
	 * Returns a Set of all of the VerbNetClass objects that contain the lemma given.
	 * 
	 * @param lemma - Verb lemma
	 * @return VerbNetClasses containing this lemma
	 */
	public static Set<VerbNetClass> getClassesOfVerb(String lemma){
		return getClassesOfVerb(lemma, false);
	}
	
	/**
	 * 
	 * @param lemma
	 * @param mwes
	 * @return
	 */
	public static Set<VerbNetClass> getClassesOfVerb(String lemma, boolean mwes){
		if (exists){
			Set<VerbNetClass> result = new HashSet<VerbNetClass>();
			
			for (VerbNetEntry vne : entries){
				if (vne.getVerb().equals(lemma) || vne.getVerb().split("_")[0].equals(lemma) || vne.getVerb().split("-")[0].equals(lemma)){
					result.add(vne.getVerbClass());
				}
			}
			if (result.isEmpty()){
				Log.warn("No classes found by getClassesOfVerb for " + lemma);
			}
			return result;
		}
		else {
			Log.error("getClassesOfVerb(String lemma) called, VerbNet not built");
			return null;
		}
	}
	
	/**
	 * Gives a list of Strings composed of the class name and VerbNet Id for that class
	 * <br> This is a more user friendly, alphabetical listing of each VerbNet class
	 * 
	 * @return Classes as String by name
	 */
	public static Collection<String> getSortedClassNames(){
		if (exists){
			Collection<String> result = new ArrayList<String>();
			
			for (VerbNetClass vnc : classes){
				result.add(vnc.getName() + "-" + vnc.getId());
			}
			
			return result;
		}

		Log.error("\'getSortedClassNames()\' called but VerbNet has not been built.");
		return null;
	}
	
	/**
	 * Gives a list of Strings of verb entries
	 * <br> This is a more user friendly, alphabetical listing of each VerbNet verb
	 * 
	 * @return Verbs by name
	 */
	public static Collection<String> getSortedVerbNames(){
		if (exists){
			Collection<String> result = new HashSet<String>();
			
			for (VerbNetEntry we : entries){
				result.add(we.getVerb());
			}
			
			return result;
		}

		Log.error("\'getSortedVerbNames()\' called but VerbNet has not been built.");
		return null;
	}


	static class Relations {
		/**
		 * Returns all VerbNetClasses that are children of the given class and present in a specific set
		 * of VerbNetClasses.  Used by VerbNet builder to compile a given VerbNetClass's family.
		 * @param v - Class to find children of
		 * @param classes - Classes to look for children in
		 * @return - Children of V that are in classes
		 */
		public static Set<VerbNetClass> findChildren(VerbNetClass v, Set<VerbNetClass> classes){
			Set<VerbNetClass> result = new HashSet<VerbNetClass>();
			
			for (VerbNetClass vnc : classes){
				if (isChild(vnc.getId(), v.getId())){
					result.add(vnc);
				}
			}
			
			return result;
		}
	
		/**
		 * Finds the parent of the given VerbNetClass in a given Set of VerbNetClasses.  Used by VerbNet builder
		 * to compile a given VerbNetClass's family.
		 * @param v - Class to find parent of
		 * @param classes - Set to look for parent in
		 * @return - Parent VerbNetClass of v
		 */
		private static VerbNetClass findParent(VerbNetClass v, Set<VerbNetClass> classes){
			VerbNetClass result = null;
			
			for (VerbNetClass vnc : classes){
				if (isParent(vnc.getId(), v.getId())){
					result = vnc;
				}
			}
			
			return result;
		}
	
		/**
		 * Finds all siblings of a given VerbNetClass in a given Set of VerbNetClasses.  Used by VerbNet builder
		 * to compile a given VerbNetClass's family.
		 * @param v - VerbNetClass to find siblings of
		 * @param classes - Set of VerbNetClasses to look in
		 * @return - Set of VerbNetClasses that are siblings to v
		 */
		private static Set<VerbNetClass> findSiblings(VerbNetClass v, Set<VerbNetClass> classes){
			Set<VerbNetClass> result = new HashSet<VerbNetClass>();
			
			for (VerbNetClass vnc : classes){
				if (areSiblings(vnc.getId(), v.getId())){
					result.add(vnc);
				}
			}
			
			return result;
		}
		
		/**
		 * Determines if a given String is a child Verb Net Class of another String
		 * @param child - VNC representation
		 * @param parent - VNC represantation
		 * @return - True if child string represents a child class of parent string
		 */
		public static boolean isChild(String child, String parent){
			boolean result = false;
			
			if (child.contains(parent) && child.length() > parent.length()){
				result = true;
			}
			
			return result;
		}
		
		/**
		 * Determines if a given String is the parent of another
		 * @param a
		 * @param b
		 * @return
		 */
		public static boolean isParent(String a, String b){
			String[] one = a.split("-");
			String[] two = b.split("-");
			boolean result = true;
			if (!(two.length == one.length+1)){	
				result = false;
			}
			else {
				int i = 0;
				while (i < one.length){
					if (!one[i].equals(two[i])){
						result = false;
					}
					i++;
				}
			}
			return result;
		}
		
		private static boolean areSiblings(String a, String b){
			String[] one = a.split("-");
			String[] two = b.split("-");
			if (a.equals(b)){
				return false;
			}
			else if (one.length != two.length){
				return false;
			}
			else {
				int i = 0;
				while ( i < (one.length-1) ){
					if (!one[i].equals(two[i])){
						return false;
					}
					i++;
				}
			}
			return true;
		}
	}
}

