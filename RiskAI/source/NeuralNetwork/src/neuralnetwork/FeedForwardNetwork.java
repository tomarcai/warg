package neuralnetwork;

import java.io.*;

/**
 * A feedforward neural network.
 */
public class FeedForwardNetwork {
	
	private int inputNodes;
	private Neuron[] hiddenNodes;
	private Neuron[] outputNodes;
	private float learningRate;
	
	/**
	 * Creates a new instance of FeedForwardNetwork. Creates #hiddenNodes + #outputNodes neurons.
	 * @param learningRate The learning rate of the network
	 * @param inputNodes The number of input nodes in the network
	 * @param hiddenNodes The number of hidden nodes in the network
	 * @param outputNodes The number of output nodes in the network
	 */
	public FeedForwardNetwork(int inputNodes, int hiddenNodes, int outputNodes, float learningRate) {
		this.inputNodes = inputNodes;
		this.hiddenNodes = new Neuron[hiddenNodes];
		for (int i = 0; i < hiddenNodes; i++) {
			this.hiddenNodes[i] = new Neuron(inputNodes, 0.2f);
		}
		this.outputNodes = new Neuron[outputNodes];
		for (int i = 0; i < outputNodes; i++) {
			this.outputNodes[i] = new Neuron(hiddenNodes, 0.2f);
		}
		this.learningRate = learningRate;
	}
	
	/**
	 * Creates a new instance of a feedforward network from a file. The learning rate should manually be set afterward, if the network should further trained!
	 * @param filename The .nnmodel file to load.
	 */
	public FeedForwardNetwork(String filename) {
		this.loadNetwork(filename);
		this.learningRate = 1;
	}
	
	/**
	 * Sets the learning rate manually.
	 * @param learningRate the learning rate of the network.
	 */
	public void setLearningRate(float learningRate) {
		this.learningRate = learningRate;
	}

	/**
	 * Gets the number of input nodes.
	 * @return the number of input nodes of the network.
	 */
	public int getNumberOfInputNodes() {
		return this.inputNodes;
	}
	
	/**
	 * Calculates the output of the network from a given input. No training is involved!
	 * @param input The input to the network. The length should be as long as there are input nodes in the network, otherwise it will return null.
	 * @return The output from the network. The length is as long as there are output nodes in the network. 
	 */
	public float[] calcOutput(float[] input) {
		if (input.length == inputNodes) {
			float[] inputToOutputLayer = new float[hiddenNodes.length];
			for (int i = 0; i < hiddenNodes.length; i++) {
				inputToOutputLayer[i] = hiddenNodes[i].calculateOutput(input);
			}
			float[] output = new float[outputNodes.length];
			for (int i = 0; i < outputNodes.length; i++) {
				output[i] = outputNodes[i].calculateOutput(inputToOutputLayer);
			}
			return output;
		} else {
			return null;
		}
	} 
	
	/**
	 * Trains the network. The input to this network should be the same as the training data (targetOutput) is based on.
	 * @param positiveExample If true, the network will train as a normal network. If false, the network will negate the error in the network - this is ment as "This example is negative, so do the opposite".
	 * @param input The input to the network.
	 * @param targetOutput The training data - the output the network should have given.
	 * @return Returns the error in the network.
	 */
	public float trainNetwork(float[] input, float[] targetOutput, boolean positiveExample) {
		float multiplier = 1.0f;
		if (!positiveExample) {
			multiplier = -1.0f;
		}
		if ((input.length == inputNodes) && (targetOutput.length == outputNodes.length)) {
			// Calculate output.
			float[] inputToOutputLayer = new float[hiddenNodes.length];
			for (int i = 0; i < hiddenNodes.length; i++) {
				inputToOutputLayer[i] = hiddenNodes[i].calculateOutput(input);
			}
			float[] output = new float[outputNodes.length];
			for (int i = 0; i < outputNodes.length; i++) {
				output[i] = outputNodes[i].calculateOutput(inputToOutputLayer);
			}
			// Train the network - backprop.
			float[] errorInOutput = new float[outputNodes.length];
			float[] errorInHidden = new float[hiddenNodes.length];
			// Calculate the error in each neuron.
			for (int k = 0; k < errorInOutput.length; k++) {
				errorInOutput[k] = output[k]*(1 - output[k]) * (targetOutput[k] - output[k]) * multiplier;
			}
			for (int h = 0; h < errorInHidden.length; h++) {
				float target = 0;
				for (int k = 0; k < errorInOutput.length; k++) {
					target += outputNodes[k].getWeights()[h] * errorInOutput[k];
				}
				errorInHidden[h] = inputToOutputLayer[h] * (1 - inputToOutputLayer[h]) * target;
			}
			// Update the weights.
			for (int j = 0; j < hiddenNodes.length; j++) {
				float[] newWeights = new float[inputNodes];
				for (int i = 0; i < inputNodes; i++) {
					newWeights[i] = hiddenNodes[j].getWeights()[i] + this.learningRate * errorInHidden[j] * input[i];
				}
				hiddenNodes[j].setWeights(newWeights);
			}
			for (int j = 0; j < outputNodes.length; j++) {
				float[] newWeights = new float[hiddenNodes.length];
				for (int i = 0; i < hiddenNodes.length; i++) {
					newWeights[i] = outputNodes[j].getWeights()[i] + this.learningRate * errorInOutput[j] * inputToOutputLayer[i];
				}
				outputNodes[j].setWeights(newWeights);
			}
			
			float networkError = 0f;
			float tmp;
			for (int i = 0; i < outputNodes.length; i++) {
				tmp = targetOutput[i] - output[i];
				networkError += tmp*tmp;
			}
			return networkError * 0.5f;
			
		} else {
			throw new UnsupportedOperationException("FeedForwardNetwork.trainNetwork(): input or targetOutput length mismatch.");
		}
	}
	
	/**
	 * Saves the network to a file. The filetype should be .nnmodel - but not necessarily.
	 * @param filename The filename of the saved network.
	 */
	public void saveNetwork(String filename) {
		try {
			FileWriter filewriter = new FileWriter(filename);
			BufferedWriter fileout = new BufferedWriter(filewriter);
			
			fileout.write("@input_nodes "+this.inputNodes);
			fileout.newLine();
			fileout.write("@hidden_nodes "+this.hiddenNodes.length);
			fileout.newLine();
			fileout.write("@output_nodes "+this.outputNodes.length);
			fileout.newLine();
			fileout.newLine();
			fileout.write("@weights");
			fileout.newLine();
			String str = "";
			for (int i = 0; i < this.inputNodes; i++) {
				for (int h = 0; h < this.hiddenNodes.length; h++) {
					str = str + hiddenNodes[h].getAllWeights()[i];
					if (!((i == inputNodes-1) && (h == hiddenNodes.length-1))) {
						str = str + ",";
					}
				}
			}
			fileout.write("hidden_weights="+str);
			fileout.newLine();
			str = "";
			for (int h = 0; h < this.hiddenNodes.length; h++) {
				for (int o = 0; o < this.outputNodes.length; o++) {
					str = str + outputNodes[o].getAllWeights()[h];
					if (!((h == hiddenNodes.length-1) && (o == outputNodes.length-1))) {
						str = str + ",";
					}
				}
			}
			fileout.write("output_weights="+str);
			fileout.close();
			filewriter.close();
		} catch (IOException e) {
			throw new UnsupportedOperationException("Unable to create file " + filename);
		}
	}
	
	private void loadNetwork(String filename) {
		try {
			FileReader filereader = new FileReader(filename);
			BufferedReader filein = new BufferedReader(filereader);
			boolean error = false;
			
			String line;
			
			// Load number of input nodes
			line = filein.readLine();
			if (line.startsWith("@input_nodes")) {
				String[] num = line.split(" ");
				if (num.length > 1) {
					this.inputNodes = Integer.parseInt(num[1]);
				} else {
					throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): cannot read number of input nodes!");
				}
			} else {
				throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): file does not start with \"@input_nodes\"!");
			}
			
			// Load number of hidden nodes
			line = filein.readLine();
			if (line.startsWith("@hidden_nodes")) {
				String[] num = line.split(" ");
				if (num.length > 1) {
					int numHidden = Integer.parseInt(num[1]);
					this.hiddenNodes = new Neuron[numHidden];
					for (int i = 0; i < numHidden; i++) {
						this.hiddenNodes[i] = new Neuron(this.inputNodes, 0.2f);
					}
				} else {
					throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): cannot read number of hidden nodes!");
				}
			} else {
				throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): file does not contain \"@hidden_nodes\" in line 2!");
			}
			
			// Load number of output nodes
			line = filein.readLine();
			if (line.startsWith("@output_nodes")) {
				String[] num = line.split(" ");
				if (num.length > 1) {
					int numOutput = Integer.parseInt(num[1]);
					this.outputNodes = new Neuron[numOutput];
					for (int i = 0; i < numOutput; i++) {
						this.outputNodes[i] = new Neuron(this.hiddenNodes.length, 0.2f);
					}
				} else {
					throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): cannot read number of output nodes!");
				}
			} else {
				throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): file does not contain \"@output_nodes\" in line 3!");
			}
			
			// Load weights
			while ((!line.equals("@weights")) && (line != null)) {
				line = filein.readLine();
			}
			line = filein.readLine();
			if (line != null) {
				do {
					String[] split = line.split("=");
					if (split.length > 1) {
						if (split[0].equals("hidden_weights")) {
							String[] weights = split[1].split(",");
							if (weights.length == (this.inputNodes*this.hiddenNodes.length)) {
								float[] w = new float[weights.length];
								for (int i = 0; i < w.length; i++) {
									w[i] = Float.parseFloat(weights[i]);
								}
								int k = 0;
								for (int i = 0; i < this.inputNodes; i++) {
									for (int h = 0; h < this.hiddenNodes.length; h++) {
										this.hiddenNodes[h].setWeight(i,w[k++]);
									}
								}
							} else {
								throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): the amount of hidden_weights does not match the stated amount.");
							}
						} else if (split[0].equals("output_weights")) {
							String[] weights = split[1].split(",");
							if (weights.length == (this.hiddenNodes.length*this.outputNodes.length)) {
								float[] w = new float[weights.length];
								for (int i = 0; i < w.length; i++) {
									w[i] = Float.parseFloat(weights[i]);
								}
								int k = 0;
								for (int h = 0; h < this.hiddenNodes.length; h++) {
									for (int o = 0; o < this.outputNodes.length; o++) {
										this.outputNodes[o].setWeight(h,w[k++]);
									}
								}
								
							} else {
								throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): the amount of output_weights does not match the stated amount.");
							}
						} else {
							throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): unknown entry found in weights");
						}
					} else {
						throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): syntax error in a weight-entry! (=)");
					}
					line = filein.readLine();
				} while (line != null);
			} else {
				throw new UnsupportedOperationException("error loading NN-Network ("+filename+"): file does not contain \"@weights\"!");
			}
			
			filein.close();
			filereader.close();
		} catch (IOException e) {
			throw new UnsupportedOperationException("Unable to load file " + filename);
		}
		

	}
	
}
