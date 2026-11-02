package trainer.trainers;

import classifiers.trees.Id3NoEmptyLeaves;
import java.io.*;
import weka.classifiers.trees.*;
import weka.classifiers.*;
import weka.core.*;

/** 
 * @deprecated Use WekaTrainer instead.
 */
class DTTrainer extends TrainerBase {
	
	private Id3NoEmptyLeaves dt;
	private String output;
	
	/** Creates a new instance of DTTrainer */
	public DTTrainer() {
		dt = new Id3NoEmptyLeaves();
	}
	
	protected String getTrainingDataDirectory() {
		return "dt";
	}
	
	protected String getTrainingDataFilenameExtension() {
		return ".arff";
	}
	
	public String getModelDirectory() {
		return "dt";
	}
	
	public String getModelFilenameExtension() {
		return ".dtmodel";
	}
	
	protected void trainFromFile(String filename) {
		String modelFilename = this.makeModelFilename(filename);
		this.createModelDirectory(modelFilename);
		String[] args = new String[4];
		args[0] = "-t";
		args[1] = filename;
		args[2] = "-d";
		args[3] = modelFilename;
		try {
			output = Evaluation.evaluateModel(dt, args);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			output = null;
		}
	}
	
	public void loadModel(String filename) {
		try {
			InputStream is = new FileInputStream(filename);
			ObjectInputStream objectInputStream = new ObjectInputStream(is);
			dt = (Id3NoEmptyLeaves) objectInputStream.readObject();
			objectInputStream.close();
		} catch (Exception e) {
			System.err.println(e.getMessage());
			output = null;
		}
	}
	
	public int query(String arffString) {
		int result = -1;
		try {
			Instances input = new Instances(new StringReader(arffString));
			input.setClassIndex(input.numAttributes() - 1);
			Instance instance = input.firstInstance();
			double classification = dt.classifyInstance(instance);
			if (Double.isNaN(classification)) {
				result = -1;
			} else {
				result = (int) classification;
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (NoSupportForMissingValuesException ex) {
			ex.printStackTrace();
		}
		return result;
	}
	
	public String toString() {
		return dt.toString();
	}
}
