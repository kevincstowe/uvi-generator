package semlink.grouping;

import java.util.LinkedHashMap;

import slflixer.cc.util.mm.MembershipMap;

public class GroupingMap extends LinkedHashMap<String, GroupingFile> {

	public MembershipMap<String, GroupingFile> uniqueVnClasses = new MembershipMap<String, GroupingFile>();
	public MembershipMap<String, GroupingFile> uniqueWnNums = new MembershipMap<String, GroupingFile>();
	
	@Override
	public GroupingFile put(String key, GroupingFile gf) {
		for(Grouping g : gf.groupings) {
			if(g.vnClasses != null) {
				for(String cls : g.vnClasses) {
					uniqueVnClasses.addMembership(cls, gf);
				}
			}
			if(g.wnSenseNums != null) {
				for(String num : g.wnSenseNums) {
					uniqueWnNums.addMembership(num, gf);
				}
			}
		}
		return super.put(key, gf);
	}
	
	@Override
	public boolean equals(Object o) {
		if(o == null) {
			return false;
		}
		GroupingMap map = (GroupingMap) o;

		if(size() != map.size()) {
			return false;
		}
		
		if(!uniqueVnClasses.equals(map.uniqueVnClasses)) {
			return false;
		}
		if(!uniqueWnNums.equals(map.uniqueWnNums)) {
			return false;
		}
		
		String[] meKeys = keySet().toArray(new String[0]);
		String[] otherKeys = map.keySet().toArray(new String[0]);

		for(int k = 0; k < meKeys.length; k++) {
			if(!meKeys[k].equals(otherKeys[k])) {
				System.out.println(meKeys[k] + " " + otherKeys[k]);
				return false;
			}
			if(!get(meKeys[k]).equals(map.get(otherKeys[k]))) {
				System.out.println(meKeys[k] + " " + otherKeys[k]);
				return false;
			}
		}

		return true;
	}
	
}
