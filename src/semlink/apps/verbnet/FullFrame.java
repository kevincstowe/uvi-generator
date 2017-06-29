package semlink.apps.verbnet;

import java.util.List;
import java.util.Set;

public class FullFrame {
	private String descriptionNumber;
	private String primary;
	private String secondary;
	private Set<String> examples;
	private SyntacticFrame syntax;
	private SemanticFrame semantics;
	
	public FullFrame(String descNumber, String primary, String secondary, Set<String> examples, SyntacticFrame syntax, SemanticFrame semantics){
		this.descriptionNumber = descNumber;
		this.primary = primary;
		this.secondary = secondary;
		this.examples = examples;
		this.syntax = syntax;
		this.semantics = semantics;
	}
	
	public Set<String> getExamples(){
		return this.examples;
	}
	
	public String getDescriptionNumber(){
		return this.descriptionNumber;
	}
	
	public String getPrimary(){
		return this.primary;
	}
	
	public String getSecondary(){
		return this.secondary;
	}
	
	public SyntacticFrame getSyntax(){
		return this.syntax;
	}
	
	public SemanticFrame getSemantics(){
		return this.semantics;
	}

	@Override
	public String toString() {
		return "FullFrame [descriptionNumber=" + descriptionNumber
				+ ", primary=" + primary + ", secondary=" + secondary
				+ ", examples=" + examples + ", semantics=" + semantics + "]";
	}
}
