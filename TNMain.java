import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;


public class TNMain {

	public static List<Attribute> attrs = new ArrayList<Attribute>();
	public static Attribute classes;

	public static void main(String[] args) {
		
		String train = args[0];
		String test = args[1];
		String mode = args[2];

		// parse train file
		List<Instance> trainInsts = new ArrayList<Instance>();
		parseARFF(train,attrs,trainInsts);

		// parse test file
		List<Attribute> notAttrs = new ArrayList<Attribute>();
		List<Instance> testInsts = new ArrayList<Instance>();
		parseARFF(test,notAttrs,testInsts);
		notAttrs = null; // don't need the duplicate
		
		test1(trainInsts, testInsts, mode);
		//test2(trainInsts, testInsts);

	}
	
	private static void parseARFF(String file, List<Attribute> attrs, List<Instance> instances) {
		
		// check file
		File in = new File(file); // file to be read
		Scanner scan; // scanner for file reading
		try {
			scan = new Scanner(in);
		} catch (FileNotFoundException e) {
			System.out.println("Error: Cannot access input file");
			return;
		}
		
		// read attributes
		String line = null;
		while(true) {
			line = scan.nextLine();
			if(line.contains("@attribute")) {
				break;
			}
		}
		do {
			String attrName = line.substring(line.indexOf(39) + 1, line.indexOf(39, line.indexOf(39) + 1));
			boolean real = !line.contains("{");
			Attribute attr = new Attribute(attrName);
			
			// get attribute values
			if(!real) {
				String valStr = line.substring(line.indexOf('{') + 1, line.indexOf('}'));
				Scanner valScan = new Scanner(valStr).useDelimiter(",| |'");
				while(valScan.hasNext()) {
					String nextStr = valScan.next();
					if(nextStr != null && !nextStr.equals("")) {
						attr.values.add(nextStr);
					}
				}
			}
			
			// add attribute
			if(!attr.name.equalsIgnoreCase("class")) {
				attrs.add(attr);
			} else {
				classes = attr;
			}
		} while((line = scan.nextLine()).contains("@attribute"));
		
		// read instances
		while(scan.hasNext()) {
			line = scan.next();
			List<String> attributes = new ArrayList<String>(Arrays.asList(line.split(",|'")));
			
			// remove empty strings
			List<String> toRemove = new ArrayList<String>();
			toRemove.add("");
			attributes.removeAll(toRemove);
			
			String label = attributes.get(attributes.size() - 1);
			attributes.remove(attributes.size() - 1);
			Instance inst = new Instance(label, attributes);
			instances.add(inst);
		}
		
	}
	
	public static void test1(List<Instance> trainInsts, List<Instance> testInsts, String mode) {
		
		if(mode.equals("t")) {
			TANBuilder tan = new TANBuilder(attrs, classes, trainInsts);
			tan.printDependencies();
			System.out.println();
			
			// classify test instances
			int numCorrect = 0;
			for(Instance i : testInsts) {
				double label1Prob = tan.p_label(i, classes.values.get(0));
				double label2Prob = tan.p_label(i, classes.values.get(1));
				if(label1Prob > label2Prob) {
					System.out.println(classes.values.get(0) + " " + i.label + " " + label1Prob);
					if(classes.values.get(0).equals(i.label)) numCorrect++;
				} else {
					System.out.println(classes.values.get(1) + " " + i.label + " " + label2Prob);
					if(classes.values.get(1).equals(i.label)) numCorrect++;
				}
			}
			System.out.println("\n" + numCorrect);
		} else if(mode.equals("n")) {
			NaiveBayesClassifierImpl nb = new NaiveBayesClassifierImpl();
			nb.train(trainInsts, classes, attrs);
			nb.printDependencies();
			System.out.println();
			
			// classify test instances
			int numCorrect = 0;
			for(Instance i : testInsts) {
				double label1Prob = nb.p_label(i, classes.values.get(0));
				double label2Prob = nb.p_label(i, classes.values.get(1));
				if(label1Prob > label2Prob) {
					System.out.println(classes.values.get(0) + " " + i.label + " " + label1Prob);
					if(classes.values.get(0).equals(i.label)) numCorrect++;
				} else {
					System.out.println(classes.values.get(1) + " " + i.label + " " + label2Prob);
					if(classes.values.get(1).equals(i.label)) numCorrect++;
				}
			}
			System.out.println("\n" + numCorrect);
		} else {
			System.out.println("Mode not valid, exiting");
		}
	}
	
	public static void test2(List<Instance> trainInsts, List<Instance> testInsts) {
		
		// get training instances
		int numTrain = 100;
		Random rand = new Random();
		List<Instance> realTrain = new ArrayList<Instance>(numTrain);
		for(int i = 0; i < numTrain; i++) {
			int index = rand.nextInt(trainInsts.size());
			realTrain.add(trainInsts.remove(index));
		}
		
		// train naive bayes and TAN
		TANBuilder tan = new TANBuilder(attrs, classes, realTrain);
		NaiveBayesClassifierImpl nb = new NaiveBayesClassifierImpl();
		nb.train(realTrain, classes, attrs);
		
		// test each
		int numTAN = 0;
		int numNB = 0;
		for(Instance i : testInsts) {
			
			// classify TAN
			double tanLabel1Prob = tan.p_label(i, classes.values.get(0));
			double tanLabel2Prob = tan.p_label(i, classes.values.get(1));
			if(tanLabel1Prob > tanLabel2Prob) {
				if(classes.values.get(0).equals(i.label)) numTAN++;
			} else {
				if(classes.values.get(1).equals(i.label)) numTAN++;
			}
			
			// classify naive bayes
			double nbLabel1Prob = nb.p_label(i, classes.values.get(0));
			double nbLabel2Prob = nb.p_label(i, classes.values.get(1));
			if(nbLabel1Prob > nbLabel2Prob) {
				if(classes.values.get(0).equals(i.label)) numNB++;
			} else {
				if(classes.values.get(1).equals(i.label)) numNB++;
			}
		}
		
		System.out.println("TAN Correct: " + numTAN);
		System.out.println("Naive Bayes Correct: " + numNB);
	}

}
