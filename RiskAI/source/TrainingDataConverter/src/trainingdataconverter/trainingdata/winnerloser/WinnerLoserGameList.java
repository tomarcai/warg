package trainingdataconverter.trainingdata.winnerloser;

import java.io.File;
import java.util.Vector;

public class WinnerLoserGameList {
    private Vector<String> gameFilenames = new Vector();
    private Vector<WinnerLoserGame> winnerLoserGameList = new Vector();
    private File[] fileArray;

    public WinnerLoserGameList() {
    }

    public void add(String gameFilename, WinnerLoserFile winnerLoserFile) {
        int index = this.gameFilenames.indexOf(gameFilename);
        if (index == -1) {
            this.gameFilenames.add(gameFilename);
            index = this.gameFilenames.size() - 1;
            this.winnerLoserGameList.add(new WinnerLoserGame());
        }

        ((WinnerLoserGame)this.winnerLoserGameList.get(index)).add(winnerLoserFile);
    }

    public File[] getWinnerLoserList() {
        int fileCount = 0;

        int fileIndex;
        for(fileIndex = 0; fileIndex < this.winnerLoserGameList.size(); ++fileIndex) {
            fileCount += ((WinnerLoserGame)this.winnerLoserGameList.get(fileIndex)).makeWinnerLoserList();
        }

        this.fileArray = new File[fileCount];
        fileIndex = 0;

        for(int gameIndex = 0; gameIndex < this.winnerLoserGameList.size(); ++gameIndex) {
            File[] gameFiles = ((WinnerLoserGame)this.winnerLoserGameList.get(gameIndex)).getWinnerLoserList();

            for(int gameFileIndex = 0; gameFileIndex < gameFiles.length; ++gameFileIndex) {
                this.fileArray[fileIndex] = gameFiles[gameFileIndex];
                ++fileIndex;
            }
        }

        return this.fileArray;
    }
}
