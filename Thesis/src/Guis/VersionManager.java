package Guis;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import Ontology.Document;

public class VersionManager {

	private final static String JADE_VERSION = "JadeVersion.txt";
	private final static String SEPERATOR = "&&&";

	public synchronized Document getVersionline(String filePath, String fileToSearch) throws FileNotFoundException {
		File file = new File(filePath + JADE_VERSION);
		if (!file.exists()) {
			return null;
		}
		Scanner scan = new Scanner(file);
		while (scan.hasNext()) {
			String line = scan.nextLine();
			if (line.contains(fileToSearch)) {
				scan.close();
				return getLineObject(line);
			}
		}
		scan.close();
		return null;
	}

	public synchronized Document getLineObject(String line) {
		Document document = new Document();
		String[] split = line.split(SEPERATOR);
		if (split != null) {
			document.setDocumentname(split[0]);
			document.setRemotedocumentpath(split[1]);
			document.setLocalversion(Long.valueOf(split[2]));
		}
		return document;
	}

	public static void main(String[] args) throws IOException {

	}

	@SuppressWarnings("unused")
	public synchronized static void setVersionline(String filePath, String fileToVersion, String newFilePath,
			Long newFileVersion) throws IOException {
		File file = new File(filePath + "/" + JADE_VERSION);
		if (!file.exists()) {
			file.createNewFile();
		}
		// String fileName = file.getName();
		// String filePath = file.getAbsolutePath().substring(0,
		// file.getAbsolutePath().lastIndexOf(File.separator));

		List<String> lines = Files.readAllLines(file.toPath());
		String newLine = fileToVersion + SEPERATOR + newFilePath + SEPERATOR + newFileVersion;
		List<String> newLines = new ArrayList<String>();
		Boolean exists = false;
		for (String line : lines) {
			if (line.contains(fileToVersion)) {
				exists = true;
				newLines.add(newLine);
			} else {
				newLines.add(line);
			}
		}
		if (!exists) {
			newLines.add(newLine);
		}
		Files.write(file.toPath(), newLines);
	}
}
