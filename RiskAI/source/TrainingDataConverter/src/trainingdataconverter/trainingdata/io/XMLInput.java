package trainingdataconverter.trainingdata.io;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.FileReader;
import java.io.IOException;

public class XMLInput {
    private String filename;
    private FileReader filein;
    private BufferedReader bufferin;
    private String input = "";

    public XMLInput(String filename) {
        this.filename = filename;
        int retriesLeft = 20;
        boolean success = false;

        while(retriesLeft > 0 && !success) {
            try {
                this.filein = new FileReader(filename);
                success = true;
            } catch (IOException var5) {
                System.err.println("Unable to open file " + filename);
                --retriesLeft;
                this.waitForRetry(5, retriesLeft);
            }
        }

        this.bufferin = new BufferedReader(this.filein);
        this.bufferin = this.bufferin;
    }

    public void readLine() throws EOFException {
        int retriesLeft = 20;
        boolean success = false;

        while(retriesLeft > 1 && !success) {
            try {
                this.input = this.bufferin.readLine();
                success = true;
            } catch (IOException var4) {
                System.err.println("Unable to read file " + this.filename);
                --retriesLeft;
                this.waitForRetry(5, retriesLeft);
            }
        }

        if (this.input == null) {
            this.close();
            throw new EOFException();
        }
    }

    private void waitForRetry(int secondsToWait, int retriesLeft) {
        if (retriesLeft > 0) {
            for(int i = secondsToWait; i > 0; --i) {
                System.out.println("Will retry in " + i + " second(s) (retries left: " + retriesLeft + ")");

                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException var5) {
                    var5.printStackTrace();
                }
            }
        } else {
            System.out.println("Giving up!");
        }

    }

    public void close() {
        try {
            this.bufferin.close();
            this.filein.close();
        } catch (IOException var2) {
            System.err.println("Unable to close file " + this.filename);
        }

    }

    public boolean contains(String text) {
        return this.input.contains(text);
    }

    public String getText() {
        return this.input.trim();
    }

    public String getText(String fieldName) {
        String text = this.getText();
        text = text.substring(text.indexOf(fieldName));
        text = text.substring(text.indexOf("\"") + 1);
        text = text.substring(0, text.indexOf("\""));
        return text;
    }

    public String toString() {
        return this.input;
    }

    public boolean endReached() {
        return this.input == null;
    }
}
