package trainer.trainers;

import java.io.*;
import progressstats.*;

/**
 * The base of all trainers.
 */
public abstract class TrainerBase {
	
	/**
	 * The abstract method that all trainers must implement.
	 * @param filename The training data file, from which the AI should learn.
	 */
	public abstract String getModelDirectory();
	public abstract String getModelFilenameExtension();
	protected abstract String getTrainingDataDirectory();
	protected abstract String getTrainingDataFilenameExtension();
	protected abstract void trainFromFile(String filename);
	
	public void train(String directory, String filename) {
		directory += getTrainingDataDirectory();
		filename = directory + "/" + filename;
		File file = new File(filename);
		if (file.exists()) {
			File[] files = null;
			if (file.isDirectory()) {
				// Make list of files in the directory
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						// Only accept files with the right extension
						if (name.endsWith(".gz")) {
							// If gzipped, remove ".gz" extension before check
							name = name.substring(0, name.length() - ".gz".length());
						}
						return name.endsWith(getTrainingDataFilenameExtension());
					}
				};
				files = file.listFiles(filter);
			} else {
				files = new File[1];
				files[0] = file;
			}
			trainFromFileArray(files);
		} else {
			System.err.println("Could not open file/directory " + filename);
		}
	}
	
	private void trainFromFileArray(File[] files) {
		// Load data from files
		System.out.println("Training started.");
		for (int fileIndex = 0; fileIndex < files.length; fileIndex++) {
			String filename = files[fileIndex].getPath();
			filename = filename.replace("\\", "/");
			trainFromFile(filename);
			System.out.println(ProgressStats.getText(fileIndex, files.length) + " Model trained from " + filename);
		}
		System.out.println("Training complete.");
	}
	
	/**
	 * Converts the training data filename into a model filename.
	 * @param filename The training data filename.
	 * @return returns the new model filename and path.
	 */
	protected String makeModelFilename(String filename) {
		/* Weka cannot read gzipped ARFF files so this will never happen anyway:
		 boolean gzipped = filename.endsWith(".gz");
		if (gzipped) {
			// If gzipped, remove ".gz" extension before model filename is constructed
			filename = filename.substring(0, filename.length() - ".gz".length());
		}
		*/
		String[] splitPath = filename.split("/");
		for (int i = 0; i < splitPath.length; i++) {
			if (splitPath[i].equals("training_examples_converted")) {
				splitPath[i] = "trained_models";
			} else if (splitPath[i].equals(getTrainingDataDirectory())) {
				splitPath[i] = getModelDirectory();
			}
		}
		filename = splitPath[0];
		for (int i = 1; i < splitPath.length; i++) {
			filename = filename + "/" + splitPath[i];
		}
		String result = filename.substring(0, filename.lastIndexOf(".")) + getModelFilenameExtension();
		/* Weka cannot read gzipped ARFF files so this will never happen anyway:
		if (gzipped) {
			result += ".gz";
		}
		*/
		return result;
	}
	
	protected void createModelDirectory(String modelFilename) {
		String directoryName = modelFilename.substring(0, modelFilename.lastIndexOf("/"));
		File directory = new File(directoryName);
		if (!directory.exists()) {
			directory.mkdirs();
		}
	}
}