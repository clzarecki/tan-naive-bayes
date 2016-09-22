import java.util.ArrayList;
import java.util.List;


public class Attribute {
	
	public String name;
	public List<String> values;
	
	public Attribute(String name) {
		super();
		this.name = name;
		this.values = new ArrayList<String>();
	}
	
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof Attribute)) return false;
		else {
			Attribute attr = (Attribute) o;
			return name.equals(attr.name);
		}
	}
	
	@Override
	public String toString() {
		return name;
	}

}
