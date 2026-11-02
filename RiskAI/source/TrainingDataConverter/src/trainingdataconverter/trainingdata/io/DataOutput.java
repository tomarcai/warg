package trainingdataconverter.trainingdata.io;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.GZIPOutputStream;

public class DataOutput {
    private StringBuilder stringOut = new StringBuilder();
    private static String newline = System.getProperty("line.separator");

    public DataOutput() {
    }

    protected void write(String text, boolean endLine) {
        this.stringOut.append(text);
        if (endLine) {
            this.stringOut.append(newline);
        }

    }

    public void writeLine(String text) {
        this.write(text, true);
    }

    public String getString() {
        return this.stringOut.toString();
    }

    public void saveToFile(String filename, boolean initFiles) {
        try {
            OutputStreamWriter fileWriter;
            if (filename.endsWith(".gz")) {
                fileWriter = new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(filename, !initFiles)));
            } else {
                fileWriter = new OutputStreamWriter(new FileOutputStream(filename, !initFiles));
            }

            BufferedWriter fileout = new BufferedWriter(fileWriter);
            fileout.write(this.getString());
            fileout.close();
        } catch (IOException var5) {
            System.err.println("Unable to write file " + filename);
        }

    }
}
