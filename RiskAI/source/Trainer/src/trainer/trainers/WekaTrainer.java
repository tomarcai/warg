package trainer.trainers;

import java.util.zip.GZIPInputStream;
import weka.classifiers.bayes.NaiveBayesSimple;
import classifiers.trees.Id3NoEmptyLeaves;
import java.io.*;
import weka.classifiers.*;
import weka.core.*;

public class WekaTrainer extends TrainerBase {
	
	private final static int DECISION_TREE = 0;
	private final static int NAIVE_BAYES = 1;

	private int type;
	
	private Id3NoEmptyLeaves decisionTree = null;
	private NaiveBayesSimple naiveBayes = null;
	
	/** Creates a new instance of WekaTrainer */
	public WekaTrainer(String classifierType) {
		if (classifierType.equals("dt")) {
			type = DECISION_TREE;
			decisionTree = new Id3NoEmptyLeaves();
		} else if (classifierType.equals("nb")) {
			type = NAIVE_BAYES;
			naiveBayes = new NaiveBayesSimple();
		} else {
			throw new RuntimeException(
					"Invalid classifier type! (type = \"" + classifierType + "\")\r\n" +
					"Type must be either \"dt\" (decision tree) or \"nb\" (na�ve Bayes).");
		}
	}
	
	public String getModelDirectory() {
		switch (type) {
			case DECISION_TREE:
				return "dt";
			case NAIVE_BAYES:
				return "nb";
		}
		return null;
	}
	
	public String getModelFilenameExtension() {
		switch (type) {
			case DECISION_TREE:
				return ".dtmodel";
			case NAIVE_BAYES:
				return ".nbmodel";
		}
		return null;
	}
	
	protected String getTrainingDataDirectory() {
		// Both can use the training data converted for the decision tree
		return "dt";
	}
	
	protected String getTrainingDataFilenameExtension() {
		return ".arff";
	}
	
	protected void trainFromFile(String filename) {
		String output = "";
		String modelFilename = this.makeModelFilename(filename);
		this.createModelDirectory(modelFilename);
		// Weka will compress the output model if the output filename ends with ".gz"
		//modelFilename += ".gz";
		String[] args = new String[4];
		args[0] = "-t";
		args[1] = filename;
		args[2] = "-d";
		args[3] = modelFilename;
		try {
			switch (type) {
				case DECISION_TREE:
					output = Evaluation.evaluateModel(decisionTree, args);
					break;
				case NAIVE_BAYES:
					output = Evaluation.evaluateModel(naiveBayes, args);
					break;
			}
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
		//System.out.println(output);
		int a = 0;
	}
	
	public void loadModel(String filename) {
		try {
			//InputStream is = new FileInputStream(filename);
			InputStream is;
			if (filename.endsWith(".gz")) {
				is = new GZIPInputStream(new FileInputStream(filename));
			} else {
				is = new FileInputStream(filename);
			}
			ObjectInputStream objectInputStream = new ObjectInputStream(is);
			switch (type) {
				case DECISION_TREE:
					decisionTree = (Id3NoEmptyLeaves) objectInputStream.readObject();
					break;
				case NAIVE_BAYES:
					naiveBayes = (NaiveBayesSimple) objectInputStream.readObject();
					break;
			}
			objectInputStream.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
		}
	}
	
	public int query(String arffString) {
		int result = -1;
		try {
			Instances input = new Instances(new StringReader(arffString));
			input.setClassIndex(input.numAttributes() - 1);
			Instance instance = input.firstInstance();
			double classification = Double.NaN;
			switch (type) {
				case DECISION_TREE:
					classification = decisionTree.classifyInstance(instance);
					break;
				case NAIVE_BAYES:
					classification = naiveBayes.classifyInstance(instance);
					break;
			}
			if (Double.isNaN(classification)) {
				result = -1;
			} else {
				result = (int) classification;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NoSupportForMissingValuesException ex) {
			ex.printStackTrace();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public String toString() {
		switch (type) {
			case DECISION_TREE:
				return decisionTree.toString();
			case NAIVE_BAYES:
				return naiveBayes.toString();
		}
		return null;
	}
}
