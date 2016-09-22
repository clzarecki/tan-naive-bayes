import java.util.ArrayList;
import java.util.List;

/**
 * Your implementation of a naive bayes classifier. Please implement all four methods.
 */

public class NaiveBayesClassifierImpl {

	// data members
	Attribute classes = null;
	List<Attribute> attrs;
	List<Integer> numPerClass = null;
	private int numLabel1 = 0; // number of label 1
	private int numLabel2 = 0; // number of label 2
	// dictionary of counts for each label
	private List<List<ValueCount>> attrCounts;

	/**
	 * Trains the classifier with the provided training data
	 */
	public void train(List<Instance> trainingData, Attribute classes, List<Attribute> attrs) {
		
		this.classes = classes;
		this.attrs = attrs;
		
		// create data structure to hold probability counts for each label
		attrCounts = new ArrayList<List<ValueCount>>(this.attrs.size());
		for(Attribute a : attrs) {
			List<ValueCount> attrCount = new ArrayList<ValueCount>(a.values.size());
			for(int i = 0; i < a.values.size(); i++) {
				attrCount.add(new ValueCount());
			}
			attrCounts.add(attrCount);
		}

		// add training data
		for(Instance inst : trainingData) {
			addInst(inst);
		}
		
	}

	/**
	 * Adds word tokens from a message to the dictionary of counts
	 * @param inst the message to add
	 */
	private void addInst(Instance inst) {

		if(inst.label.equals(classes.values.get(0))) {
			// instance is label 1
			numLabel1++;
		} else {
			// instance is label 2
			numLabel2++;
		}

		for(int attrIndex = 0; attrIndex < inst.attributes.size(); attrIndex++) {
			String attrValue = inst.attributes.get(attrIndex);
			int valueIndex = attrs.get(attrIndex).values.indexOf(attrValue);
			ValueCount vc = attrCounts.get(attrIndex).get(valueIndex);
			if(inst.label.equals(classes.values.get(0))) {
				// instance is label 1
				vc.numLabel1++;
			} else {
				// instance is label 2
				vc.numLabel2++;
			}
		}
	}

	/**
	 * Returns the prior probability of the label parameter
	 */
	public double p_l(String label) {
		double total = (double) numLabel1 + (double) numLabel2;
		if(label.equals(classes.values.get(0))) {
			return (double) (numLabel1 + 1) / (total + 2);
		} else {
			return (double) (numLabel2 + 1) / (total + 2);
		}
	}

	/**
	 * Returns the smoothed conditional probability of the attribute value
	 * given the label
	 */
	public double p_w_given_l(Attribute attr, String value, String label) {

		int attrIndex = attrs.indexOf(attr);
		int valueIndex = attr.values.indexOf(value);

		// find count of the specified word for the label
		ValueCount vc = attrCounts.get(attrIndex).get(valueIndex);
		double count;
		double total = 0;
		for(ValueCount vcTest : attrCounts.get(attrIndex)) {
			if(label.equals(classes.values.get(0))) {
				// label 1
				total += vcTest.numLabel1;
			} else {
				// label 2
				total += vcTest.numLabel2;
			}
		}
		if(label.equals(classes.values.get(0))) {
			// label 1
			count = vc.numLabel1;
		} else {
			// label 2
			count = vc.numLabel2;
		}

		// calculate conditional probability with smoothing
		return (count + 1) / (total + attr.values.size());
	}

	/**
	 * Finds the probability of the specified label for the instance
	 */
	public double p_label(Instance inst, String label) {
		
		// find prior probability for initialization
		double sumL1 = p_l(classes.values.get(0));
		double sumL2 = p_l(classes.values.get(1));
//		System.out.println("Class 0 " + sumL1);
//		System.out.println("Class 1 " + sumL2);
		
		// add conditional probabilities of each word
		for(int attrIndex = 0; attrIndex < attrs.size(); attrIndex++) {
//			double l1test = p_w_given_l(attrs.get(attrIndex), inst.attributes.get(attrIndex), classes.values.get(0));
//			double l2test = p_w_given_l(attrs.get(attrIndex), inst.attributes.get(attrIndex), classes.values.get(1));
//			System.out.println("Class 0 " + l1test);
//			System.out.println("Class 1 " + l2test);
			sumL1 *= p_w_given_l(attrs.get(attrIndex), inst.attributes.get(attrIndex), classes.values.get(0));
			sumL2 *= p_w_given_l(attrs.get(attrIndex), inst.attributes.get(attrIndex), classes.values.get(1));
		}
		
		if(label.equals(classes.values.get(0))) {
			return sumL1 / (sumL1 + sumL2);
		} else {
			return sumL2 / (sumL1 + sumL2);
		}
	}
	
	public void printDependencies() {
		for(Attribute a : attrs) {
			System.out.println(a.toString() + " " + classes.name);
		}
	}

}
