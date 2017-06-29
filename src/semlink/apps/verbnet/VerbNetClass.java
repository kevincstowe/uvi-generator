package semlink.apps.verbnet;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import tools.Log;
import tools.Tools;

import semlink.apps.verbnet.RoleValue;
import semlink.apps.verbnet.Role;
import semlink.apps.verbnet.VerbNet;

public class VerbNetClass implements Serializable, Comparable<VerbNetClass> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -84703448447539560L;
	
	VerbNetClass parent;
//	Set<VerbNetClass> children = new HashSet<VerbNetClass>();
//	Set<VerbNetClass> siblings = new HashSet<VerbNetClass>(); 
	private Set<Role> roles = new HashSet<Role>();
	private Set<FullFrame> frames = new HashSet<FullFrame>();
	private Set<VerbNetEntry> members = new HashSet<VerbNetEntry>();
	private File sourceFile;
	private int hashValue = 0;
	
	private String name = "";
	private String ID = "";
	
	/**
	 * public static class Builder
	 * <br>
	 * <br>Builder pattern for VerbNetClass 
	 * <br>VerbNetClasses can take a large number of parameters, many of them optional.
	 * <br>This makes it ideal for the Builder pattern described by Joshua Bloch (Effective Java, 2nd edition)
	 * <br>
	 * <br>Fields :
	 * <br>{@code String name} (required)
	 * <br>{@code String ID} (required)
	 * <br>{@code VerbNetClass parent}
	 * <br>{@code File sourceFile}
	 * <br>{@code Set<VerbNetClass> children}
	 * <br>{@code Set<VerbNetClass> siblings}
	 * <br>{@code Set<String> roles}
	 * <br>{@code Set<SyntacticFrame> frames}
	 * <br>{@code Set<VerbNetEntry> members}

	 * @author kcstowe
	 *
	 */
	public static class Builder {
		private String name;
		private String ID;
//		private VerbNetClass parent;
		private File sourceFile;
//		private Set<VerbNetClass> children = new HashSet<VerbNetClass>();
//		private Set<VerbNetClass> siblings = new HashSet<VerbNetClass>();
		private Set<Role> roles = new HashSet<Role>();
		private Set<FullFrame> frames = new HashSet<FullFrame>();
		private Set<VerbNetEntry> members = new HashSet<VerbNetEntry>();
		
		public Builder(String name, String ID){
			this.name = name;
			this.ID = ID;
		}
		
		public Builder sourceFile(File f){
			this.sourceFile = f;
			return this;
		}
/*		public Builder parent(VerbNetClass p){
			this.parent = p;
			return this;
		}
		public Builder children(Set<VerbNetClass> c){
			this.children = c;
			return this;
		}
		public Builder siblings(Set<VerbNetClass> s){
			this.siblings = s;
			return this;
		}
		*/
		public Builder roles(Set<Role> roles){
			this.roles = roles;
			return this;
		}
		public Builder frames(Set<FullFrame> f){
			this.frames = f;
			return this;
		}
		public Builder members(Set<VerbNetEntry> e){
			this.members = e;
			return this;
		}
		
		public VerbNetClass build(){
			return new VerbNetClass(this);
		}
	}

	/**
	 * Constructor takes a builder object and returns the newly built
	 * VerbNetClass instance.
	 * @param b - Builder to make class from
	 */
	public VerbNetClass(Builder b) {
		this.name = b.name;
		this.ID = b.ID;
		//this.parent = b.parent;
		//this.children = b.children;
		//this.siblings = b.siblings;
		this.roles = b.roles;
		this.frames = b.frames;
		this.members = b.members;
		this.sourceFile = b.sourceFile;
	}

	/** 
	 * Returns whether or not this class is the root class of it's family.
	 * @return True if this class is root class, otherwise false
	 */
	public boolean isRoot(){
		//Silly, but seems to be necessary and sufficient
		return !this.ID.contains("-");
	}

	/**
	 * Returns how far into the hierarchy this class is.  The root class
	 * will have a depth of 0.
	 * @return Depth into hierarchy.
	 */
	public int getDepth(){
		int result = 0;
		for (char c : this.ID.toCharArray()){
			//Again, silly but effective?
			if (c == '-'){
				result++;
			}
		}
		return result;
	}
	
	/**
	 * Returns a list of Strings of each verb present in this class.
	 * 
	 * @return Verb strings present in this class.
	 */
	public List<String> getVerbStrings(){
		List<String> result = new ArrayList<String>();
		
		for (VerbNetEntry vne : this.members){
			result.add(vne.getVerb());
		}
		
		return result;
	}
	/**
	 * Returns the entire extended family of this verb class
	 * This includes parents, grandparents, all children, all siblings, all siblings' children
	 * DOES include the verb class itself
	 * This doesn't work quite right!
	 * @return All members of this verb class's family
	 */
	public Set<VerbNetClass> getFamily(){
		Set<VerbNetClass> result = new TreeSet<VerbNetClass>();

		for (VerbNetClass vnc : VerbNet.getClasses()){
			if (vnc.getId().split("-")[0].equals(this.getId().split("-")[0])){
				result.add(vnc);
			}
					
		}
		return result;
	}
	
	/**
	 * public boolean familyContains(VerbNetEntry vne)
	 * <br>
	 * <br>Given a VerbNetEntry instance, determine whether or not
	 * <br>that VerbNetEntry exists in the family of this class.
	 * 
	 * @param vne - VerbNetEntry to check
	 * @return True if this VerbNetClass's family contains the VerbNetEntry.
	 */
	public boolean familyContains(VerbNetEntry vne){
		boolean result = false;
		for (VerbNetClass vnc : this.getFamily()){
			if (vnc.getMembers().contains(vne)){
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * public VerbNetClass getRootClass()
	 * <br>
	 * <br>Return the class that is the root of this class's family.  For example,
	 * <br>for the class learn-14-2-1, will return learn-14. 
	 * <br>Logs a warning if no root is found.
	 * 
	 * @return
	 * VerbNetClass that is the root of this class
	 */
	public VerbNetClass getRootClass(){
		VerbNetClass result = null;
		for (VerbNetClass vnc : this.getFamily()){
			System.out.println(vnc);
			if (vnc.isRoot()){
				result = vnc;
			}
		}
		if (result == null){
			Log.warn("No root class found for " + this + ".");
		}
		return result;
	}
	
	/**
	 * public static VerbNetClass parseClass(String s)
	 * <br>
	 * <br>Given a String representation of a class, this function attempts
	 * <br>to return a valid VerbNetClass object.  It checks  if the string is a sole
	 * <br>VerbNet ID, a sole class name, and strings containing both.
	 * <br>
	 * <br>If a sole class name is passed, it only matches the highest class of that name
	 * <br>in the heirarchy.
	 * 
	 * @param s - Representation to be parsed
	 * @return
	 * VerbNetClass object found from the String passed, or null with a log message
	 * <br>if none could be found.
	 */
	public static VerbNetClass parseClass(String s){
		VerbNetClass result = null;
		String errorMessage = "";
		String[] data = s.split(" ");
		boolean found = false;
		if (data.length <= 1){
			//If one word passed (either ID or name)
			for (VerbNetClass vnc : VerbNet.getClasses()){

				//If it's an ID number that matches
				if (vnc.getId().equalsIgnoreCase(s)){
					result = vnc;
					found = true;
				}
				//If it's a name that matches, it needs to match the highest class in the hierarchy
				//This is done by checking if it's a subclass (contains '-'), and disallowing those
				else if  (vnc.getName().equalsIgnoreCase(s) && !(vnc.getId().contains("-"))){
					result = vnc;
					found = true;
				}
			}
			if (!found){
				errorMessage = "Single value passed (" + s + "), but not found as ID or verb";
			}
		}
		else {
			for (VerbNetClass vnc : VerbNet.getClasses()){
				//If two words passed
				String id = "";
				String verb = "";
				if (Pattern.matches(".*[0-9].*", data[0])){
					id = data[0];
					verb = data[1];
				}
				else if (Pattern.matches(".*[0-9].*", data[1])) {
					id = data[1];
					verb = data[0];
				}
				else {
					errorMessage = "String contained two elements, but a VerbNet class ID wasn't found";
				}
				
				if (vnc.getId().equals(id) ^ vnc.getName().equalsIgnoreCase(verb)){
					errorMessage = "Mismatch between ID and verb name";
				}
				else if (vnc.getId().equals(id) && vnc.getName().equalsIgnoreCase(verb)){
					result = vnc;
				}
			}
		}
		if (result == null){
			Log.warn(s + " couldn't be parsed : " + errorMessage);
		}
		return result;
	}
	

	public String getName() {
		return this.name;
	}

	public Set<Role> getThematicRoles(){
		/*Set<String> result = new HashSet<String>();
		for (Role r : roles){
			if (r.getRoleValue() != null){
				if (!r.getRoleValue().equals(RoleValue.Initial_Location)){
					result.add(r.getRoleValue().toString().toUpperCase().replace("_", "-"));
				}
				else {
					result.add(r.getRoleValue().toString().toUpperCase());
				}
			}
		}*/
		
		return this.roles;
	}
	
	public Set<VerbNetEntry> getMembers() {
		return Tools.copySet(this.members);
	}
	
	public Set<String> getMemberStrings(){
		Set<String> result = new HashSet<String>();
		
		for (VerbNetEntry vne : this.members){
			result.add(vne.getVerb());
		}
		
		return result;
	}
	
	public Set<FullFrame> getFullFrames(){
		return Tools.copySet(this.frames);
	}
	
	public Set<SyntacticFrame> getSyntacticFrames(){
		Set<SyntacticFrame> result = new HashSet<SyntacticFrame>();
		for (FullFrame ff : this.frames){
			result.add(ff.getSyntax());
		}
		return result;
	}

	public Set<SemanticFrame> getSemanticFrames(){
		Set<SemanticFrame> result = new HashSet<SemanticFrame>();
		for (FullFrame ff : this.frames){
			result.add(ff.getSemantics());
		}
		return result;
	}

	
	public String getId(){
		return this.ID;
	}
	
	public Set<Role> getRoles(){
		return this.roles;
	}
	
	public Collection<Role> getExtendedRoles(){
		Set<Role> roles = new HashSet<Role>();
		for (VerbNetClass vnc : this.getFamily()){
			roles.addAll(vnc.getRoles());
		}
		return roles;
	}
	
	public VerbNetClass getParent(){
		return this.parent;
	}
	
	public String getSourceLocation(){
		return sourceFile.getName();
	}
	
/*	public Set<VerbNetClass> getSiblings(){
		return Tools.copySet(this.siblings);
	}

	public Set<VerbNetClass> getChildren(){
		return Tools.copySet(this.children);
	}
	*/
	
	/*
	 * Comparable Implementation
	 */
	
	@Override
	public int compareTo(VerbNetClass vnc){
		int result = 0;
		
		if (this == vnc || this.equals(vnc)){
			result = 0;
		}

		if (vnc.getId().equals(this.ID)){
			result = 0;
		}
		else if (vnc.getId().length() < this.ID.length()){
			result = 1;
		}
		else {
			result = this.ID.compareTo(vnc.getId());
		}
		
		return result;
	}
	
	/*
	 * Object Overrides
	 */
	@Override
	public String toString(){
		return name + "-" + ID;

	}

	@Override
	public int hashCode() {
		int result = hashValue;
		if (result == 0){
			final int prime = 31;
			result = 17;
			result = prime * result + ((ID == null) ? 0 : ID.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			hashValue = result;
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VerbNetClass other = (VerbNetClass) obj;
		if (other.getName().equals(this.getName()) && other.getId().equals(this.getId())){
			return true;
		}
		else {
			return false;
		}
	}

}
