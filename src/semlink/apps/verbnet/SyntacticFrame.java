package semlink.apps.verbnet;

import java.util.List;

import semlink.apps.verbnet.Role;

public class SyntacticFrame {
	private String syntaxString;
	private List<Role> roles;

	public SyntacticFrame(String syntaxString, List<Role> roles){
		this.syntaxString = syntaxString;
		this.roles = roles;
	}
	
	public String getSyntaxString(){
		return this.syntaxString;
	}
	
	public List<Role> getRoles(){
		return this.roles;
	}
	
	@Override
	public String toString(){
		return this.roles.toString();
	}
}
