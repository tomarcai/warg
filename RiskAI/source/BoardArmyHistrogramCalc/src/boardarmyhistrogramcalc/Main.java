/*
 * Main.java
 *
 * Created on 7. april 2006, 10:29
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package boardarmyhistrogramcalc;

import java.io.*;

/**
 *
 * @author Administrator
 */
public class Main {
	
	private static long[] histogram = new long[1000];
	
	/** Creates a new instance of Main */
	public Main() {
	}

	private static void loadBoardData(String filename, int index, int all) {
		float percentage = (float)index/(float)all*100f;
		String input;
		FileReader filein;
		try {
			filein = new FileReader(filename);
			BufferedReader bufin = new BufferedReader(filein);
			int territoryIndex = 0;
			input = bufin.readLine();
			System.out.println("["+(int)percentage+"% done, "+(all-index)+" left] loading: "+filename);
			while(input != null) {
				if (input.contains("<boardstate>")) {
					do {
						input = bufin.readLine();
						if (input.contains("<territory")) {
							do {
								input = bufin.readLine();
								String owner = null;
								if (input.contains("<owner>")) {
									input = bufin.readLine();
									do {
										input = bufin.readLine();
									} while (!input.contains("</owner>"));
								}
								input = bufin.readLine();
								int armies = 0;
								if (input.contains("<armies>")) {
									input = bufin.readLine();
									armies = Integer.parseInt(input.trim());
									do {
										input = bufin.readLine();
									} while (!input.contains("</armies>"));
								}
								if (armies > 1000) {
									System.out.println("Armies larger than 1000! ("+armies+")");
									armies = 1000;
								}
								histogram[armies]++;
								//debug("board[" + territoryIndex + "] = " + board[territoryIndex]);
								territoryIndex++;
								input = bufin.readLine();
							} while (!input.contains("</territory>"));
						}
					} while (!input.contains("</boardstate>"));
				}
				input = bufin.readLine();
			}
			bufin.close();
		} catch (IOException e) {
			System.err.println("Unable to open file " + filename);
		}
	}
	
	
	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		String boardStateDirectory;
		if (args.length == 1) {
			boardStateDirectory = args[0];
		}
                else {
			boardStateDirectory = "ai-data_10_first/ai-data/training_examples/boardstates/";
		}

		if (!boardStateDirectory.endsWith("/")) {
			boardStateDirectory += "/";
		}
		File f = new File(boardStateDirectory);
		if (f.exists()) {
			if (f.isDirectory()) {
				FilenameFilter filter = new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.endsWith(".xml");
					}							
				};
				File[] files = f.listFiles(filter);	
				for (int i = 0; i < files.length; i++) {
					loadBoardData(files[i].getPath(),i,files.length-1);
				}
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("histogram.txt"));
			long sum = 0;
			for (int i = 0; i < histogram.length; i++) {
				sum += histogram[i];
				if (!isRestZero(histogram,i)) {
					out.write(String.valueOf(histogram[i]));
					out.newLine();
				}
			}
			out.write("sum: "+sum);
			out.close();
			System.out.println("histogram.txt written.");
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static boolean isRestZero(long[] histogram, int index) {
		for (int i = index; i < histogram.length; i++) {
			if (histogram[i] != 0) {
				return false;
			}
		}
		return true;
	}
	
}
