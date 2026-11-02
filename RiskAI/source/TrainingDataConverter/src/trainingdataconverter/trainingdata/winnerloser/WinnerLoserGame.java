package trainingdataconverter.trainingdata.winnerloser;

import java.io.File;
import java.util.Random;
import java.util.Vector;

public class WinnerLoserGame {
    private Vector<WinnerLoserFile> winnerList = new Vector();
    private Vector<WinnerLoserFile> loserList = new Vector();
    private File[] fileList;

    public WinnerLoserGame() {
    }

    public void add(WinnerLoserFile file) {
        if (file.getPlayerWon()) {
            this.winnerList.add(file);
        } else {
            this.loserList.add(file);
        }

    }

    public File[] getWinnerLoserList() {
        if (this.fileList == null) {
            throw new RuntimeException("fileList is null. Remember to call makeWinnerLoserList() first");
        } else {
            return this.fileList;
        }
    }

    public int makeWinnerLoserList() {
        int smallestListSize = Math.min(this.winnerList.size(), this.loserList.size());
        this.fileList = new File[smallestListSize * 2];

        for(int fileIndex = 0; fileIndex < smallestListSize; ++fileIndex) {
            this.fileList[fileIndex * 2] = this.pickRandomFile(this.winnerList);
            this.fileList[fileIndex * 2 + 1] = this.pickRandomFile(this.loserList);
        }

        return smallestListSize * 2;
    }

    private File pickRandomFile(Vector<WinnerLoserFile> list) {
        Random random = new Random();

        WinnerLoserFile file;
        do {
            int index = random.nextInt(list.size());
            file = (WinnerLoserFile)list.get(index);
        } while(file.getPicked());

        file.setPicked(true);
        return file.getFile();
    }
}
