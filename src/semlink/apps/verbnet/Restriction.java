package semlink.apps.verbnet;

public class Restriction implements Comparable{
	public String value = "";
	
	public String logic = "and";
	public boolean pos = true;
	
	/**
	 * Create a new Restriction
	 */
	public Restriction(String value, boolean pos){
		this.value = value;
		this.pos = pos;
	}
	
	public static Restriction parseRestriction(String value){
		if (value.startsWith("+"))
			return new Restriction(value.substring(1), true);
		else if (value.startsWith("-"))
			return new Restriction(value.substring(1), false);
		else{
			return null;
		}
	}
	@Override
	public String toString(){
		return this.pos + " " + this.value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (pos ? 1231 : 1237);
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
		Restriction other = (Restriction) obj;
		if (pos != other.pos)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public int compareTo(Object o) {
		try {
			Restriction other = (Restriction)o;
			if (other.value.equals(this.value))
				return (other.pos == this.pos ? 0 : -1);
			else
				return other.value.compareTo(this.value);
		}
		catch (Exception e){
			return 0;
		}
	}
	
	
}