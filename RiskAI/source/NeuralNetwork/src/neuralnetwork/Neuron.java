package neuralnetwork;

import java.util.*;

/**
 * A sigmoid neuron.
 */
public class Neuron {
	private int numberOfInputs;
	private float[] weights;
	private Random rand = new Random();
	
	/**
	 * Constructor for the Neuron class. Initializes the weights.
	 * @param numberOfInputs The number of inputs to this particular neuron.
	 * @param initWeightSpan The weights are initially set to a random value between -initWeightSpan/2 and +initWeightSpan/2.
	 */
	public Neuron(int numberOfInputs, float initWeightSpan) {
		this.numberOfInputs = numberOfInputs;
		weights = new float[numberOfInputs+1];
		// Initialize all weights to a random value between -initWeightSpan/2 and +initWeightSpan/2.
		for (int i = 0; i < weights.length; i++) {
			weights[i] = rand.nextFloat()*initWeightSpan-initWeightSpan/2f;
		}
	}
	
	/**
	 * Returns the number of inputs to this neuron.
	 * @return the number of inputs to this neuron.
	 */
	public int getNumberOfInputs() {
		return this.numberOfInputs;
	}
	
	/**
	 * Returns the set of weights in the network (does NOT include the bias).
	 * @return a float[] which holds the set of weights (does NOT include the bias). The size is equal to the number of inputs to this neuron.
	 */
	public float[] getWeights() { // This does NOT include the bias weight!
		float[] res = new float[this.weights.length-1];
		for (int i = 0; i < this.weights.length-1; i++) {
			res[i] = this.weights[i+1];
		}
		return res;
	}
	
	/**
	 * Returns all weights - including the bias!
	 * @return a float[] which holds the set of weights (INCLUDING the bias). The size is equal to the number of inputs to this neuron +1!
	 */
	public float[] getAllWeights() {
		return this.weights;
	}
	
	/**
	 * Sets a specific weight in the network. Index 0 is the bias.
	 * @param index The index of the weight that is set. Index 0 is the bias.
	 * @param w The value of the weight.
	 */
	public void setWeight(int index, float w) {
		this.weights[index] = w;
	}
	
	/**
	 * Sets new weights in the network.
	 * @param newWeights a float[] which holds the new set of weights in the network (does NOT include the bias). The size is equal to the number of inputs to this neuron.
	 */
	public void setWeights(float[] newWeights) { // This should NOT include the bias weight!
		if (newWeights.length == numberOfInputs) {
			for (int i = 0; i < this.weights.length-1; i++) {
				this.weights[i+1] = newWeights[i];
			}
		}
	}
	
	/**
	 * Similar to setWeights(), except this one also includes the bias. Used when a network is loaded from a file.
	 * @param newWeights a float[] which holds the new set of weights in the network (INCLUDIMG the bias). The size is equal to the number of inputs to this neuron + 1!
	 */
	public void setAllWeights(float[] newWeights) {
		this.weights = newWeights;
	}
	
	/**
	 * Makes a run of the neuron. Calculates an output float from a set of inputs. The size of the input is equal to the number of inputs to the neuron.
	 * @param input the input for the run of the network. The float[] has the same size as the number of inputs.
	 * @return returns the output of the run. Returns a single float.
	 */
	public float calculateOutput(float[] input) {
		if (input.length == numberOfInputs) {
			float summedInput = weights[0]; // The bias!
			for (int i = 1; i < weights.length; i++) {
				summedInput += (input[i-1] * weights[i]);
			}
			return sigmoidFunction(summedInput);
		} else {
			throw new UnsupportedOperationException("Neuron.calculateOutput(): input length mismatch.");
		}
	}
	
	private float sigmoidFunction(float intput) {
		float response = 1.0f;
		return (float)(1.0 / ( 1.0 + Math.exp((-intput / response))));
	}
}