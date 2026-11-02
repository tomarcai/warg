package trainingdataconverter.trainingdata.winnerloser;

import java.io.File;

public class WinnerLoserFile {
    private File file;
    private boolean playerWon;
    private boolean picked;

    public WinnerLoserFile(File file, boolean playerWon) {
        this.file = file;
        this.playerWon = playerWon;
        this.picked = false;
    }

    public boolean getPlayerWon() {
        return this.playerWon;
    }

    public boolean getPicked() {
        return this.picked;
    }

    public void setPicked(boolean picked) {
        this.picked = picked;
    }

    public File getFile() {
        return this.file;
    }
}
