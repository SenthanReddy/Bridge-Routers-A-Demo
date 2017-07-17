import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class Bridge {
	public static File FDBFile;
	public static File FrameFile;
	public static ArrayList<String> FrameList;
	public static ArrayList<String> FrameOUTList;
	public static int NPo = 0;
	public static HashMap<Character, Integer> FDBMap;
	public static HashMap<Character, Integer> NewFDB;
	public static boolean checkerror = false;

	public static void main(String[] args) {
		JFileChooser FDBChooser = new JFileChooser();
		FDBChooser.setDialogTitle("Open Forwarding Database File");
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"Text Files", "txt");
		FDBChooser.setFileFilter(filter);
		int result = FDBChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			FDBFile = FDBChooser.getSelectedFile();
			System.out.println("\n FDB File Selected: "
					+ FDBFile.getAbsolutePath());
		} else {
			System.out.println("FDB File not selected");
		}

		JFileChooser FrameFileChooser = new JFileChooser();
		FrameFileChooser.setDialogTitle("Open Frame File");
		FileNameExtensionFilter frmfilter = new FileNameExtensionFilter(
				"Text Files", "txt");
		FrameFileChooser.setFileFilter(frmfilter);
		int frmresult = FrameFileChooser.showOpenDialog(null);
		if (frmresult == JFileChooser.APPROVE_OPTION) {
			FrameFile = FrameFileChooser.getSelectedFile();
			System.out.println("Selected Frame File: "
					+ FrameFile.getAbsolutePath() + "\n");
		} else {
			checkerror = true;
			System.out.println("Frame File not selected\n");
		}
		Operating();
	}

	public static void Operating() {
		FDBMap = new HashMap<Character, Integer>();
		FrameList = new ArrayList<String>();
		try (BufferedReader br = new BufferedReader(new FileReader(FDBFile))) {
			String FDBLine;
			Boolean isFirstLine = true;
			while ((FDBLine = br.readLine()) != null) {
				FDBLine.trim();
				FDBLine = FDBLine.replaceAll("\\s+", "");
				if (isFirstLine) {
					NPo = Integer.parseInt(FDBLine);
					isFirstLine = false;
				} else {
					FDBMap.put(FDBLine.charAt(0),
							Character.getNumericValue(FDBLine.charAt(1)));
				}
			}
		} catch (Exception e) {
			System.out.println("Error " + e.getMessage());
		}

		try (BufferedReader br = new BufferedReader(new FileReader(FrameFile))) {
			String FrameLine;
			while ((FrameLine = br.readLine()) != null) {
				FrameLine.trim();
				FrameLine = FrameLine.replaceAll("\\s+", "");
				FrameList.add(FrameLine);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
		Output();
	}

	public static void Output() {
		FrameOUTList = new ArrayList<String>();
		NewFDB = new HashMap<Character, Integer>();
		char Source = 'A';
		char Destination;
		int InputPort = 1;
		for (String Frame : FrameList) {
			InputPort = Character.getNumericValue(Frame.charAt(2));
			Source = Frame.charAt(0);
			Destination = Frame.charAt(1);
			if (InputPort <= NPo) {
				if (!FDBMap.containsKey(Source)
						|| FDBMap.get(Source) != InputPort) {
					FrameOUTList.add(Source + " " + Destination + " "
							+ InputPort + " FDB Updated; ");
				}
				if (FDBMap.containsKey(Source)
						&& FDBMap.containsKey(Destination)
						&& FDBMap.get(Source) == FDBMap.get(Destination)) {
					FrameOUTList.add(Source + " " + Destination + " "
							+ InputPort + " Frame Discarded ");
				}
				if (FDBMap.containsKey(Destination)
						&& FDBMap.get(Source) != FDBMap.get(Destination)) {
					FrameOUTList.add(Source + " " + Destination + " "
							+ InputPort + " Frame sent on port "
							+ FDBMap.get(Destination));
				}
				if (!FDBMap.containsKey(Destination)) {
					FrameOUTList.add(Source + " " + Destination + " "
							+ InputPort + " Frame Broadcast on all ports");
				}
			} else {
				System.out.println("Invalid Frame: " + Source + " "
						+ Destination + " " + InputPort);
			}
		}
		System.out.println("\nBridge Operations");
		for (String Frame : FrameOUTList) {
			System.out.println(Frame);
		}
		for (String Frame : FrameList) {
			NewFDB = FDBMap;
			NewFDB.put(Source, InputPort);
		}
		NewFDB(NewFDB);
	}

	public static void NewFDB(HashMap<Character, Integer> NewFDB) {
		try {
			File fileToSave = null;
			JFileChooser fileChooser = new JFileChooser();
			fileChooser
					.setDialogTitle("Set Location of updated FDB file");
			int userSelection = fileChooser.showSaveDialog(null);
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				fileToSave = fileChooser.getSelectedFile();
				System.out.println("\nLocation of updated FDB File: "
						+ fileToSave.getAbsolutePath());
			}

			PrintWriter writer = new PrintWriter(fileToSave + ".txt");
			System.out.println("\nUpdated Forwarding Database: ");
			writer.println(NPo);
			for (char node : NewFDB.keySet()) {
				writer.println(node + " " + NewFDB.get(node));
				System.out.println(node + " " + NewFDB.get(node));
			}
			writer.close();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}