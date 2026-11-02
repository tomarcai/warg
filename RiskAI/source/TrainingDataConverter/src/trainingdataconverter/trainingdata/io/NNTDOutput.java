package trainingdataconverter.trainingdata.io;

public class NNTDOutput extends DataOutput {
    public NNTDOutput() {
    }

    public void writeHeader(int inputNodes, int hiddenNodes, int outputNodes) {
        String text = "@input_nodes " + inputNodes;
        this.writeLine(text);
        text = "@hidden_nodes " + hiddenNodes;
        this.writeLine(text);
        text = "@output_nodes " + outputNodes;
        this.writeLine(text);
    }

    public void writeData(boolean winner, String input, String target, int inputNodes, int outputNodes) {
        int in_values = input.split(",").length;
        if (in_values != inputNodes) {
            throw new RuntimeException("Number of input values does not match number of input nodes! (nodes=" + inputNodes + ", values=" + in_values + ")");
        } else {
            int out_values = target.split(",").length;
            if (out_values != outputNodes) {
                throw new RuntimeException("Number of target values does not match number of output nodes! (nodes=" + inputNodes + ", values=" + out_values + ")");
            } else {
                String str;
                if (winner) {
                    str = "W";
                } else {
                    str = "L";
                }

                String text = str + "{" + input + "},{" + target + "}";
                this.writeLine(text);
            }
        }
    }
}
