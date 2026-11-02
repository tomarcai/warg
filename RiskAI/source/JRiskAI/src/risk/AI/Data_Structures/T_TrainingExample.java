package risk.AI.Data_Structures;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

/**
 *
 * @author Administrator
 */
public class T_TrainingExample {
	
	private Vector fileContent = new Vector();
	private String filename;
	private String module_name;
	private String dir;
	private String fullPath;
	
	/** Creates a new instance of T_TrainingExample  - a single training example for a single module (or board/game) */
	public T_TrainingExample(String baseDir, String dir, String module_name, String filename, Vector fileContent) {
		this.dir = dir;
		this.module_name = module_name;
		this.filename = filename;
		this.fileContent = fileContent;
		this.fullPath = baseDir+"training_examples/" + dir + "/" + module_name + "/" + filename;
	}
	
	public void saveExample() {
		try {
			FileWriter writer = new FileWriter(fullPath);
			BufferedWriter out = new BufferedWriter(writer);
			writeLine(out, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			for (int i = 0; i < fileContent.size(); i++) {
				writeLine(out, (String)fileContent.get(i));
			}
			out.close();
			writer.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private void writeLine(BufferedWriter out, String line) {
		try {
			out.write(line);
			out.newLine();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}	
	
	public String getFullPath() {
		return this.fullPath;
	}
		
}
