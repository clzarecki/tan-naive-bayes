
public class CondProb {
	
	public boolean twoPars;
	
	public Attribute attr = null;
	public Attribute parAttr1 = null;
	public Attribute parAttr2 = null;
	
	/**
	 * first dimension is on different main Attribute values, 
	 * second dimension is on different parent Attribute values
	 */
	public int[][] counts1 = null;
	/**
	 * first dimension is on different main Attribute values, 
	 * second dimension is on different parent1 Attribute values, 
	 * third dimension is on different parent2 Attribute values
	 */
	public int[][][] counts2 = null;
	
	public CondProb(Attribute attr, Attribute parentAttr) {
		this.attr = attr;
		this.parAttr1 = parentAttr;
		this.counts1 = new int[attr.values.size()][parentAttr.values.size()];
		this.twoPars = false;
	}
	
	public CondProb(Attribute attr, Attribute parentAttr1, Attribute parentAttr2) {
		this.attr = attr;
		this.parAttr1 = parentAttr1;
		this.parAttr2 = parentAttr2;
		this.counts2 = new int[attr.values.size()][parentAttr1.values.size()][parentAttr2.values.size()];
		this.twoPars = true;
	}

}
