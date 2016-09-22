import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class TANBuilder {

	private List<Attribute> attrs = null;
	private Attribute classes = null;
	private List<Attribute> attrsWClasses = null;
	private List<Instance> insts;
	private int numLabel1 = 0;
	private int numLabel2 = 0;
	private CondProb[] cpts = null;
	private List<List<Integer>> dependencies;

	public TANBuilder(List<Attribute> attrs, Attribute classes, List<Instance> train) {

		this.attrs = attrs;
		this.classes = classes;
		this.insts = train;

		// get counts for each label
		findNumLabels();

		// find conditional probability for each attribute
		double[][] condProbs = findCondProbs();
		
		Graph mst = maxSpanningTree(condProbs);
		List<int[]> edges = mst.treeEdges();
		
		// find which attributes have parents
		dependencies = new ArrayList<List<Integer>>();
		for(int attrIndex = 0; attrIndex < attrs.size(); attrIndex++) {
			ArrayList<Integer> list = new ArrayList<Integer>();
			for(int[] edge : edges) {
				if(edge[1] == attrIndex) {
					list.add(edge[0]);
				}
			}
			list.add(attrs.size()); // class always a parent //TODO changed
			dependencies.add(list);
		}
		
		// calculate final conditional probabilities
		cpts = new CondProb[attrs.size()];
		attrsWClasses = new ArrayList<Attribute>(attrs.size() + 1);
		attrsWClasses.addAll(attrs);
		attrsWClasses.add(classes);
		for(int i = 0; i < attrs.size(); i++) {
			List<Integer> dependence = dependencies.get(i);
			if(dependence.size() == 1) {
				cpts[i] = new CondProb(attrs.get(i), attrsWClasses.get(dependence.get(0)));
			} else {
				cpts[i] = new CondProb(attrs.get(i), attrsWClasses.get(dependence.get(0)), attrsWClasses.get(dependence.get(1)));
			}
		}
		
		for(Instance i : insts) {
			for(CondProb cp : cpts) {
				// find index or attribute value
				int attrIndex = attrsWClasses.indexOf(cp.attr);
				String attrVal = i.attributes.get(attrIndex);
				int aValIndex = attrsWClasses.get(attrIndex).values.indexOf(attrVal);
				
				if(cp.twoPars) {
					// get both attribute dependencies
					Attribute a1 = cp.parAttr1;
					Attribute a2 = cp.parAttr2;
					int a1Index = attrsWClasses.indexOf(a1);
					int a2Index = attrsWClasses.indexOf(a2);
					
					// find index of instance's value in each attribute
					String val1;
					if(a1Index == 18) {
						val1 = i.label;
					} else {
						val1 = i.attributes.get(a1Index);
					}
					String val2;
					if(a2Index == attrs.size()) { //TODO changed
						// if the attribute is the class
						val2 = i.label;
					} else {
						val2 = i.attributes.get(a2Index);
					}
					int valIndex1 = attrsWClasses.get(a1Index).values.indexOf(val1);
					int valIndex2 = attrsWClasses.get(a2Index).values.indexOf(val2);
					
					// increment correct cpt count
					cp.counts2[aValIndex][valIndex1][valIndex2]++;
				} else {
					// get attribute dependencies
					Attribute a1 = cp.parAttr1;
					int a1Index = attrsWClasses.indexOf(a1);
					
					// find index of instance's value for attribute
					String val1;
					if(a1Index == attrs.size()) { //TODO changed
						// if the attribute is the class
						val1 = i.label;
					} else {
						val1 = i.attributes.get(a1Index);
					}
					int valIndex1 = attrsWClasses.get(a1Index).values.indexOf(val1);
					
					// increment correct cpt count
					cp.counts1[aValIndex][valIndex1]++;
				}
			}
		}
	}

	private void findNumLabels() {

		int numLabel1 = 0;

		// increment count for each label
		for(Instance i : insts) {
			if(i.label.equals(classes.values.get(0))) {
				numLabel1++;
			}
		}

		this.numLabel1 = numLabel1;
		this.numLabel2 = insts.size() - numLabel1;
	}

	private double[][] findCondProbs() {
		double[][] condProbs = new double[attrs.size()][attrs.size()];

		// fill in conditional probability for each pair
		for(int attr1Index = 0; attr1Index < attrs.size(); attr1Index++) {
			for(int attr2Index = 0; attr2Index < attrs.size(); attr2Index++) {
				Attribute attr1 = attrs.get(attr1Index);
				Attribute attr2 = attrs.get(attr2Index);
				condProbs[attr1Index][attr2Index] = condMutualInfo(attr1, attr2);
			}
		}
		// change diagonal set to -1, each attribute with itself
		for(int i = 0; i < attrs.size(); i++) {
			condProbs[i][i] = -1;
		}
		
		return condProbs;
	}

	private double condMutualInfo(Attribute a, Attribute b) {

		int aIndex = attrs.indexOf(a);
		int bIndex = attrs.indexOf(b);

		double mutualInfo = 0;

		// sum for each value in both attributes
		for(String valueA : a.values) {
			for(String valueB : b.values) {

				// label 1 counts
				double numValABLabel1 = 0;
				double numValALabel1 = 0;
				double numValBLabel1 = 0;

				// label 2 counts
				double numValABLabel2 = 0;
				double numValALabel2 = 0;
				double numValBLabel2 = 0;

				for(Instance i : insts) {

					boolean isValA = i.attributes.get(aIndex).equals(valueA);
					boolean isValB = i.attributes.get(bIndex).equals(valueB);

					// increment counts
					if(i.label.equals(classes.values.get(0))) {
						// instance is label 1
						if(isValA && isValB) numValABLabel1++;
						if(isValA) numValALabel1++;
						if(isValB) numValBLabel1++;

					} else {
						// instance is label 2
						if(isValA && isValB) numValABLabel2++;
						if(isValA) numValALabel2++;
						if(isValB) numValBLabel2++;

					}
				}
				
				// calculate amount of mutual information label 1 adds
				double probZ1 = ((double) numLabel1 + 1) / ((double) insts.size() + 2);
				double probXYZ1 = (numValABLabel1 + 1) / ((double) insts.size() + a.values.size() * b.values.size() * 2);
				double probXZ1 = (numValALabel1 + 1) / ((double) insts.size() + a.values.size() * 2);
				double probYZ1 = (numValBLabel1 + 1) / ((double) insts.size() + b.values.size() * 2);
				
				double label1Part = (probZ1 * probXYZ1) / (probXZ1 * probYZ1);
				label1Part = probXYZ1 * Math.log(label1Part) / Math.log(2);
				
				// calculate amount of mutual information label 2 adds
				double probZ2 = ((double) numLabel2 + 1) / ((double) insts.size() + 2);
				double probXYZ2 = (numValABLabel2 + 1) / ((double) insts.size() + a.values.size() * b.values.size() * 2);
				double probXZ2 = (numValALabel2 + 1) / ((double) insts.size() + a.values.size() * 2);
				double probYZ2 = (numValBLabel2 + 1) / ((double) insts.size() + b.values.size() * 2);
				
				double label2Part = (probZ2 * probXYZ2) / (probXZ2 * probYZ2);
				label2Part = probXYZ2 * Math.log(label2Part) / Math.log(2);

				// update mutual information
				mutualInfo += label1Part + label2Part;
			}
		}
		return mutualInfo;
	}
	
	private Graph maxSpanningTree(double[][] condProbs) {
		
		// keep track of attributes to add
		List<Attribute> attrsToAdd = new LinkedList<Attribute>();
		for(Attribute a : attrs) {
			attrsToAdd.add(a);
		}
		
		// list to keep track of added nodes
		List<Attribute> addedAttrs = new LinkedList<Attribute>();
		
		// create graph and add first node
		Graph mst = new Graph();
		Attribute attr = attrsToAdd.remove(0);
		mst.addNode(attr);
		addedAttrs.add(attr);
		
		// continually add nodes and attributes until all are connected
		while(!attrsToAdd.isEmpty()) {
			
			// keep track of best attribute
			Attribute bestNeed = null, bestHave = null;
			double bestCondProb = -1;
			
			// for each attribute we still need...
			for(Attribute need : attrsToAdd) {
				int cpNeedIndex = attrs.indexOf(need);
				
				// find best conditional probability between it and the attributes we already have
				for(Attribute have : addedAttrs) {
					int cpHaveIndex = attrs.indexOf(have);
					
					// if this is the best pair of attributes, update bests
					if(condProbs[cpNeedIndex][cpHaveIndex] > bestCondProb) {
						bestNeed = need;
						bestHave = have;
						bestCondProb = condProbs[cpNeedIndex][cpHaveIndex];
					}
				}
			}
			
			// add edge between best two attributes
			mst.addEdge(bestNeed, bestHave);
			addedAttrs.add(bestNeed);
			attrsToAdd.remove(bestNeed);
		}
		
		return mst;
	}
	
	public void printDependencies() {
		for(int i = 0; i < attrs.size(); i++) {
			System.out.print(attrs.get(i).toString());
			List<Integer> dependence = dependencies.get(i);
			for(Integer attr : dependence) {
				System.out.print(" " + attrsWClasses.get(attr));
			}
			System.out.println();
		}
	}
	
	private double p_l(String label) {
		if(label.equals(classes.values.get(0))) {
			// first label
			return ((double)numLabel1 + 1) / ((double)numLabel1 + (double)numLabel2 + 2);
		} else {
			// second label
			return ((double)numLabel2 + 1) / ((double)numLabel1 + (double)numLabel2 + 2);
		}
	}
	
	private double p_v(Attribute attr, String value, Instance inst, String label) {
		
		// get correct conditional probability table
		int attrIndex = attrs.indexOf(attr);
		int valIndex = attr.values.indexOf(value);
		CondProb c = cpts[attrIndex];

		if(!c.twoPars) {
			// only conditioning on the class
			
			// find the index of the value for the label in the instance
			int instCondValIndex = classes.values.indexOf(label);
			double count = c.counts1[valIndex][instCondValIndex];
			
			// find sum of instances with the conditional attribute values
			double total = 0;
			for(int i = 0; i < c.counts1.length; i++) {
				total += c.counts1[i][instCondValIndex];
			}
			
			// return smoothed conditional probability
			return (count + 1) / (total + attr.values.size());
			
		} else {
			// conditioning on class and another attribute

			// find the index of the value for the first conditional attribute in the instance
			Attribute condAttr1 = c.parAttr1;
			int condAttr1Index = attrsWClasses.indexOf(condAttr1);
			String instCondVal1 = inst.attributes.get(condAttr1Index);
			if(condAttr1Index == attrsWClasses.size() - 1) {
				instCondVal1 = inst.attributes.get(condAttr1Index);
				instCondVal1 = inst.label;
			}
			int instCondVal1Index = condAttr1.values.indexOf(instCondVal1);
			
			// find the index of the value for the class in the instance
			int instCondVal2Index = classes.values.indexOf(label);
			
			double count = c.counts2[valIndex][instCondVal1Index][instCondVal2Index];
			
			// find sum of instances with the conditional attribute values
			double total = 0;
			for(int i = 0; i < c.counts2.length; i++) {
				total += c.counts2[i][instCondVal1Index][instCondVal2Index];
			}
			
			// return smoothed conditional probability
			return (count + 1) / (total + attr.values.size());
		}
	}
	
	public double p_label(Instance inst, String label) {
		
		// find prior probability for initialization
		double sumL1 = p_l(classes.values.get(0));
		double sumL2 = p_l(classes.values.get(1));
		
		// add conditional probabilities of each word
		for(int attrIndex = 0; attrIndex < attrs.size(); attrIndex++) {
			sumL1 *= p_v(attrs.get(attrIndex), inst.attributes.get(attrIndex), inst, classes.values.get(0));
			sumL2 *= p_v(attrs.get(attrIndex), inst.attributes.get(attrIndex), inst, classes.values.get(1));
		}
		
		if(label.equals(classes.values.get(0))) {
			return sumL1 / (sumL1 + sumL2);
		} else {
			return sumL2 / (sumL1 + sumL2);
		}
	}
	
}
