package semlink.apps.verbnet;

import java.util.ArrayList;
import java.util.List;

public class SemanticFrame {
	public List<Predicate> predicates;
	
	public SemanticFrame(){
		this.predicates = new ArrayList<Predicate>();
	}
	
	@Override
	public String toString(){
		String res = "";
		for (Predicate p : this.predicates){
			res += p.value + " ";
		}
		return res;
	}
}
