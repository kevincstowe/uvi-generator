package semlink.apps.verbnet;

public class Arg {
	public String type;
	public String value;
	
	public Arg(String type, String value){
		this.type = type;
		this.value = value;
	}
	
	@Override
	public String toString(){
		return this.type + " " + this.value;
	}
}
