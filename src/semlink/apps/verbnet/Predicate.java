package semlink.apps.verbnet;

import java.util.ArrayList;
import java.util.List;

public class Predicate {
	public String value;
	public List<Arg> args;
	
	public Predicate(){
		this.value = "";
		this.args = new ArrayList<Arg>();
	}
	
	@Override 
	public String toString(){
		return this.value + " " + this.args;
	}
}
