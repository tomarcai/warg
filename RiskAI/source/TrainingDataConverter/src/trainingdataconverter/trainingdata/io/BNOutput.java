package trainingdataconverter.trainingdata.io;

public class BNOutput extends DataOutput {
    public BNOutput() {
    }

    public void writeHeader(String header, int header_size) {
        if (header_size != header.split(",").length) {
            throw new UnsupportedOperationException("The header size does not match the statet size!");
        } else {
            this.writeLine(header);
        }
    }

    public void writeData(String data, int header_size) {
        int data_size = data.split(",").length;
        if (data_size != header_size) {
            throw new UnsupportedOperationException("The data set size does not match the number of nodes! (nodes=" + header_size + ", data size=" + data_size + ")");
        } else {
            this.writeLine(data);
        }
    }
}
