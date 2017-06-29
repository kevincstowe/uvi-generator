package semlink.apps.verbnet;

/**
 * Enum encompassing possible selectional restrictions on VerbNet roles.  "abstract" is a Java
 * keyword, converted to _abstract.
 * @author kcstowe
 *
 */
public enum SelectionalRestrictionValue {
	_abstract, not_abstract, 
	animal, not_animal,
	animate, not_animate,
	body_part, not_body_part,
	comestible, not_comestible,
	communication, not_communication,
	concrete, not_concrete,
	currency, not_currency,
	dest, not_dest,
	dest_conf, not_dest_conf,
	dest_dir, not_dest_dir,
	dir, not_dir,
	elongated, not_elongated,
	force, not_force,
	garment, not_garment,
	human, not_human,
	int_control, not_int_control,
	loc, not_loc,
	location, not_location,
	machine, not_machine,
	nonrigid, not_nonrigid,
	organization, not_organization,
	path, not_path,
	plural, not_plural,
	pointy, not_pointy,
	refl, not_refl,
	region, not_region,
	scalar, not_scalar,
	solid, not_solid,
	sound, not_sound,
	spatial, not_spatial,
	src, not_src,
	state, not_state,
	substance, not_substance,
	time, not_time,
	vehicle, not_vehicle;
	
	/**
	 * Parses a value from a two strings, one being "+" or "-", the other being the 
	 * name of the selectional restriction.
	 * @param p - "+" or "-"
	 * @param s - String to parsed
	 * @return - Selectional restriction of "(string)" or "not_(string)", null if not found.
	 */
	public static SelectionalRestrictionValue parseValue(String p, String s){
		if (p.equals("+")){
			if (s.equals("abstract")){
				return SelectionalRestrictionValue._abstract;
			}
			else {
				return SelectionalRestrictionValue.valueOf(s);
			}
		}
		else if (p.equals("-")){
			return SelectionalRestrictionValue.valueOf("not_" + s);
		}
		assert false : "Selectional Restriction Value parse failed!";
		return null;
	}
}
