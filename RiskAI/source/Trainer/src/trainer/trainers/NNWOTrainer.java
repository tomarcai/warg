package trainer.trainers;

import java.util.Vector;
import javax.naming.OperationNotSupportedException;
import java.util.zip.GZIPInputStream;
import neuralnetwork.FeedForwardNetwork;
import java.io.*;

public class NNWOTrainer extends TrainerBase {
	
	/** Creates a new instance of NNTrainer */
	public NNWOTrainer() {
	}

	protected String getTrainingDataDirectory() {
		return "nn";
	}

	protected String getTrainingDataFilenameExtension() {
		return ".nntd";
	}
	
	public String getModelDirectory() {
		return "nn_wo";
	}

	public String getModelFilenameExtension() {
		return ".nnmodel";
	}

	protected void trainFromFile(String filename) {
		String modelName = this.makeModelFilename(filename);
		System.out.println("Loading "+filename+"...");
		try {
			// Load nntd-file
			InputStreamReader fileReader;
			//FileReader fileReader = new FileReader(filename);
			if (filename.endsWith(".gz")) {
				fileReader = new InputStreamReader(new GZIPInputStream(new FileInputStream(filename)));
			} else {
				fileReader = new InputStreamReader(new FileInputStream(filename));
			}
			BufferedReader filein = new BufferedReader(fileReader);
			
			
			int inputNodes = 0;
			int hiddenNodes = 0;
			int outputNodes = 0;

			String line;
			// Load number of input nodes
			line = filein.readLine();
			if (line.startsWith("@input_nodes")) {
				String[] num = line.split(" ");
				if (num.length > 1) {
					inputNodes = Integer.parseInt(num[1]);
				} else {
					System.out.println("error loading NN-Network ("+filename+"): cannot read number of input nodes!");
				}
			} else {
				System.out.println("error loading NN-Network ("+filename+"): file does not start with \"@input_nodes\"!");
			}

			// Load number of hidden nodes
			line = filein.readLine();
			if (line.startsWith("@hidden_nodes")) {
				String[] num = line.split(" ");
				if (num.length > 1) {
					int numHidden = Integer.parseInt(num[1]);
					hiddenNodes = numHidden;
				} else {
					System.out.println("error loading NN-Network ("+filename+"): cannot read number of hidden nodes!");
				}
			} else {
				System.out.println("error loading NN-Network ("+filename+"): file does not contain \"@hidden_nodes\" in line 2!");
			}

			// Load number of output nodes
			line = filein.readLine();
			if (line.startsWith("@output_nodes")) {
				String[] num = line.split(" ");
				if (num.length > 1) {
					int numOutput = Integer.parseInt(num[1]);
					outputNodes = numOutput;
				} else {
					System.out.println("error loading NN-Network ("+filename+"): cannot read number of output nodes!");
				}
			} else {
				System.out.println("error loading NN-Network ("+filename+"): file does not contain \"@output_nodes\" in line 3!");
			}
		
			Vector inputData = new Vector();
			Vector targetData = new Vector();
			
			// Load data
			while ((!line.equals("@data")) && (line != null)) {
				line = filein.readLine();
			}
			line = filein.readLine();
			if (line != null) {
				do {
					float[] inputDataLine = new float[inputNodes];
					float[] targetDataLine = new float[outputNodes];
					
					if (line.substring(0,1).equals("W")) {
						line = line.substring(1);
					
						String[] split = line.substring(1,line.length()-1).replace("},{",";").split(";"); // cuts first the beginning and end of the string, then replace the middle with a * and split it in two.
						String[] inputStr = split[0].split(",");
						String[] targetStr = split[1].split(",");
						if (inputStr.length == inputNodes) {
							for (int i = 0; i < inputStr.length; i++) {
								inputDataLine[i] = Float.parseFloat(inputStr[i]);
							}
						} else {
							System.out.println("error loading NN-Network ("+filename+"): amount of input data does not match the stated amount.");
						}
						if (targetStr.length == outputNodes) {
							for (int i = 0; i < targetStr.length; i++) {
								targetDataLine[i] = Float.parseFloat(targetStr[i]);
							}
						} else {
							System.out.println("error loading NN-Network ("+filename+"): amount of target data does not match the stated amount.");
						}

						inputData.add(inputDataLine);
						targetData.add(targetDataLine);
					}					
					line = filein.readLine();
				} while (line != null);
			}
			System.out.println("NNWOTraining started");
			// Start training
			
			FeedForwardNetwork nnet = new FeedForwardNetwork(inputNodes,hiddenNodes,outputNodes,0.03f);
			for (int i = 0; i < 50; i++) {
				System.out.print("NNWOTraining iteration: "+i);
				float error = 0;
				// The training set:
				for (int j = 0; j < inputData.size(); j++) {
					error += nnet.trainNetwork((float[])inputData.get(j),(float[])targetData.get(j), true);
				}
				error /= inputData.size();
				System.out.println(" (error: "+error+")");				
			}
			
			// Save the trained network
			this.createModelDirectory(modelName);
			nnet.saveNetwork(modelName);
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

}
