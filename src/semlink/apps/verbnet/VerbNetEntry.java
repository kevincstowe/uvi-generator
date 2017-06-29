package semlink.apps.verbnet;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import tools.Log;
import semlink.apps.verbnet.VerbNet;
import enums.POS;

/**
 * VerbNetEntry
 * 
 * A class that holds the relevant information about a given verb entry in VerbNet.
 * This information includes the lemma of the verb and the class it belongs to.
 * @author kcstowe
 *
 */
public class VerbNetEntry implements Serializable, Comparable<VerbNetEntry>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -65478475606541542L;
	
	private String verb;
	private String[] features;
	private int hashValue = 0;
	
	VerbNetClass verbClass;
	
	/**
	 * <br>Constructor takes a name, VerbNet class, and a list of wordnet groupings.
	 * <br> Hashcode is generated upon constructor as VerbNetEntries are frequency used as a keys.
	 * @param verb - Verb name
	 * @param vnc - Class the verb is in
	 */
	public VerbNetEntry(String verb, String features, VerbNetClass vnc){
		this.verb = verb;
		this.verbClass = vnc;
		if (features.trim().length() > 1)
			this.features = features.split(" ");
		else
			this.features = null;
		hashValue = hashCode();
	}

	/**
	 * ParseVerbNetEntry takes a string and a VerbNetClass object and returns
	 * the VerbNetEntry object that corresponds to these.  
	 * <br><br>
	 * If the information provided doesn't match a verb in the correct class, 
	 * or is invalid, a warning is written to the log and null is returned.
	 * @param lemma - Verb to search for
	 * @param _class - VerbNetClass that verb is found in
	 * @return VerbNetEntry instance based on the lemma and class
	 */
	public static VerbNetEntry parseVerbNetEntry(String lemma, VerbNetClass _class){
		VerbNetEntry result = null;
		
		if (lemma == "" || lemma == null || _class == null){
			Log.warn("VerbNetEntry couldn't be parsed : requires non-null lemma and class");
		}
		else {
			for (VerbNetEntry vne : _class.getMembers()){
				if (vne.getVerb().equals(lemma)){
					result = vne;
				}
			}
			if (result == null){
				Log.warn("VerbNetEntry couldn't be parsed from : " + lemma + " - " + _class);
			}
		}
		return result;
	}
	
	/**
	 * ParseVerbNetEntries takes a single argument, the verb lemma, and returns
	 * each possible VerbNetEntry object for that lemma.  This function is useful
	 * if you want to find all of the occurrences of a given verb in VerbNet.
	 * <br>
	 * <br>Null is returned with a warning if the verb isn't found.
	 * @param lemma - Verb to search for
	 * @return Set of all possible VerbNetEntries that share the lemma
	 */
	public static Set<VerbNetEntry> parseVerbNetEntries(String lemma){
		Set<VerbNetEntry> result = new HashSet<VerbNetEntry>();
		for (VerbNetEntry vne : VerbNet.getVerbEntries()){
			if (vne.getVerb().equalsIgnoreCase(lemma)){
				result.add(vne);
			}
		}
		if (result.size() == 0){
			Log.warn("WARNING : \'" + lemma + "\' was not parsed into a valid VN entry");
			result = null;
		}
		return result;		
	}
	
	/**
	 * Determines if the given VerbNetEntry has a lemma that contains more than one word.
	 * @return true if VerbNetEntry is MWE, false otherwise
	 */
	public boolean isMultiWord(){
		boolean result = false;
		if (verb.contains(" ") || verb.contains("_") || verb.contains("-")){
			result = true;
		}
		return result;
	}
	
	/**
	 * Returns VerbNet class that this entry belongs to.
	 * @return VerbNetClass instance that this verb belongs to.
	 */
	public VerbNetClass getVerbClass(){
		return this.verbClass;		
	}
	
	/**
	 * Returns the verb string
	 * @return - this.verb
	 */
	public String getVerb(){
		return this.verb;
	}

	/**
	 * Returns the POS value for this entry - because it's a VerbNetEntry it is necessarily POS.V
	 * @return - POS.V
	 */
	public POS getPos(){
		return POS.V;
	}

	/**
	 * Return the features, as an array of strings
	 */
	public String[] getFeatures(){
		return this.features;
	}
	
	/*
	 * Comparable Implementation
	 */

	@Override
	public int compareTo (VerbNetEntry o) {
		return (o.getVerbClass().getId() + " " + o.getVerb()).compareTo(this.getVerbClass().getId() + " " + this.verb);
	}
	
	/*
	 * Object overrides
	 */
	
	@Override
	public String toString(){
		if (this.verbClass != null){
			return this.verb + " " + this.verbClass.getId();
		}
		else {
			Log.error("VerbNetEntry requires VerbClass : " + this.verb);
		}
		return null;
	}

	@Override
	public int hashCode() {
		int result = hashValue;
		if (result == 0){
			final int prime = 31;
			result = 17;
			result = prime * result + verb.hashCode();
			result = prime * result + (verbClass == null ? 0 : verbClass.getName().hashCode() + verbClass.getId().hashCode());
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
		VerbNetEntry other = (VerbNetEntry) obj;
		if (verb == null) {
			if (other.verb != null)
				return false;
		} else if (!verb.equals(other.verb))
			return false;
		if (verbClass == null) {
			if (other.verbClass != null)
				return false;
		} else if (!verbClass.equals(other.verbClass))
			return false;
		return true;
	}
	
}
