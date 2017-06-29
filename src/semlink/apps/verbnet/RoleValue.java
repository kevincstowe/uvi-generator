package semlink.apps.verbnet;

import tools.Log;

/**
 * Enum encompassing possible Roles for VerbNet classes. 
 * @author kcstowe
 *
 */
public enum RoleValue {
	Actor, Affector, Agent, Asset, Attribute, Axis, Beneficiary, Causer, Context, Co_Agent, Co_Patient, Co_Theme, Destination, Duration, Experiencer,
	Extent, Final_Time, Goal, Initial_Location, Initial_State, Instrument, Location, Manner, Material, Path, Patient, Pivot, Precondition, Predicate, Product,
	Recipient, Reflexive, Result, Source, Stimulus, Theme, Time, Topic, Trajectory, Value;
	
	/**
	 * Returns the enum value of a given string representation of a VerbNet Role.  Allows for 
	 * cases like "Co-Agent", which it converts to "Co_Agent".
	 * @param s - String to conver to Role
	 * @return - Role value of converted string, null if none found.
	 */
	public static RoleValue parseValue(String s){
		RoleValue v = null;
		if (s == null)
			return null;
		if (s.length() < 1)
			return null;
		try {
			v = RoleValue.valueOf(s);
		}
		catch(NullPointerException e){
			return null;
		}
		catch(IllegalArgumentException e){
			try {
				Log.warn("Trying a capitalized version...");
				if (s.contains("-")){
					String pre = s.split("-")[0];
					String post = s.split("-")[1];
					pre = pre.substring(0, 1).toUpperCase() + pre.substring(1);
					post = post.substring(0, 1).toUpperCase() + post.substring(1);
					s = pre + "-" + post;
					
					v = RoleValue.valueOf(pre + "-" + post);
					
				}
				else if (s.contains("_")){
					String pre = s.split("_")[0];
					String post = s.split("_")[1];
					pre = pre.substring(0, 1).toUpperCase() + pre.substring(1);
					post = post.substring(0, 1).toUpperCase() + post.substring(1);
					s = pre + "_" + post;
					
					v = RoleValue.valueOf(pre + "_" + post);
				}
				else {
					v = RoleValue.valueOf(s.substring(0, 1).toUpperCase() + s.substring(1));
				}
			}
			catch (IllegalArgumentException e2){
				Log.warn ("Argument not a valid role, attempting to replace \'-\'");
				try {
					v = RoleValue.valueOf(s.replace("-", "_"));
				}
				catch (IllegalArgumentException e3){
					Log.warn("That didn't work, no role found");
				}
			}
		}
		return v;
	}
}
