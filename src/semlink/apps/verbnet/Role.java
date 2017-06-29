package semlink.apps.verbnet;

import java.util.Collection;
import java.util.Set;

import semlink.apps.verbnet.RoleValue;
import semlink.apps.verbnet.SelectionalRestrictionValue;

public class Role {
	/**
	 * 
	 */
	private static final long serialVersionUID = -971403906379405193L;
	
	private RoleValue value;
	private Set<Restriction> selRestriction;
	private Set<Restriction> synRestriction;
	private String prepValue;
	
	/**
	 * Constructor for Role, takes a RoleValue and SelectionalRestriction
	 * @param rv - RoleValue of this Role
	 * @param sr - SelectioanlRestriction object for this Role
	 */
	public Role(RoleValue rv, Set<Restriction> selRestr, Set<Restriction> synRestr){
		this.value = rv;
		this.synRestriction = synRestr;
		this.selRestriction = selRestr;
	}
	
	public Role(String s, Set<Restriction> selRestr, Set<Restriction> synRestr){
		if (RoleValue.parseValue(s) != null)
			this.value = RoleValue.parseValue(s);
		else{
			//NEED TO PARSE ACCURATE PREP VALUES HERE!!!
			this.prepValue = s;
		}
		this.selRestriction = selRestr;
		this.synRestriction = synRestr;
	}
	/**
	 * Returns this Role's RoleValue
	 * @return - RoleValue of this Role
	 */
	public RoleValue getRoleValue(){
		return this.value;
	}
	
	/**
	 * Returns the SelectionalRestriction object for this Role
	 * @return - SelectionalRestriction of this Role
	 */
	public Set<Restriction> getSynRestrictions(){
		return this.synRestriction;
	}
	
	/**
	 * Returns the SelectionalRestriction object for this Role
	 * @return - SelectionalRestriction of this Role
	 */
	public Set<Restriction> getSelRestrictions(){
		return this.selRestriction;
	}
	
	/**
	 * Given a Collection of selectional restrictions, determine whether those
	 * <br>restrictions satisfy the restrictions on this role.
	 * @param restrictions
	 * @return - True if the given restrictions satisfy this role's restrictions
	 */
	public boolean isValid(Collection<SelectionalRestrictionValue> restrictions){
		boolean result = false;
		
		return result;
	}
	
	@Override
	public String toString(){
		String result = "";
		if (this.value == null){
			if (this.prepValue == null)
				return null;
			result = this.prepValue.toString();

		}
		else
			result = this.value.toString();
		
		result += this.selRestriction.toString() + " " + this.synRestriction.toString();
		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((prepValue == null) ? 0 : prepValue.hashCode());
		result = prime * result
				+ ((selRestriction == null) ? 0 : selRestriction.hashCode());
		result = prime * result
				+ ((synRestriction == null) ? 0 : synRestriction.hashCode());
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		Role other = (Role) obj;
		if (prepValue == null) {
			if (other.prepValue != null)
				return false;
		} else if (!prepValue.equals(other.prepValue))
			return false;
		if (selRestriction == null) {
			if (other.selRestriction != null)
				return false;
		} else if (!selRestriction.equals(other.selRestriction))
			return false;
		if (synRestriction == null) {
			if (other.synRestriction != null)
				return false;
		} else if (!synRestriction.equals(other.synRestriction))
			return false;
		if (value != other.value)
			return false;
		return true;
	}
}
