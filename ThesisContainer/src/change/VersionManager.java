package change;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;

import Ontology.Document;
import enums.DocumentType;
import enums.FileChangeMode;

public class VersionManager {

	private final static String JADE_VERSION = "JadeVersion.txt";
	private final static String WAITING_LIST = "JadeWaitingList.txt";
	private final static String INVALID_CACHE_LIST = "JadeInvalidCacheList.txt";
	private final static String TIMEOUT_LIST = "JadeTimeoutList.txt";
	private final static String SEPERATOR = "&&&";
	private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");

	public synchronized Document getVersionline(String filePath, String fileToSearch, Boolean includeDocumentFile)
			throws ParseException, IOException {
		File file = new File(filePath + "/" + JADE_VERSION);
		if (file.exists()) {
			Scanner scan = new Scanner(file);
			while (scan.hasNext()) {
				String line = scan.nextLine();
				if (line.contains(fileToSearch)) {
					scan.close();
					return getLineObject(line, filePath, includeDocumentFile);
				}
			}
			scan.close();
		}
		return null;
	}

	public synchronized Document getLineObject(String line, String filePath, Boolean includeDocumentFile)
			throws ParseException, IOException {
		Document document = new Document();
		String[] split = line.split(SEPERATOR);
		if (split != null) {
			document.setDocumentname(split[0]);
			document.setRemotedocumentpath(split[1]);
			document.setRemoteversion(Long.valueOf(split[2]));

			if (split.length > 3) {
				document.setOwner(split[3]);
				document.setLastOwnTime(simpleDateFormat.parse(split[4]));
			}
		}

		if (includeDocumentFile) {
			File file = new File(filePath + document.getDocumentname());
			document.setByteArray(Files.readAllBytes(file.toPath()));
		}
		return document;
	}

	public static void main(String[] args) throws IOException {

	}

	@SuppressWarnings("unused")
	public synchronized Long setVersionline(String fileChangeMode, String containerPath, Document document,
			Long version) throws IOException {
		// Eger dosya yazma sadece tek makineye ise ve kopya ile olusturulan bir
		// dosya degilse bekleyen listesi beslenir.
		if (FileChangeMode.ONLY_LOCAL_NODE.getCode().equals(fileChangeMode)
				&& !DocumentType.COPY.getCode().equals(document.getDocumenttype())) {
			setDocumentScannerFile(containerPath, document);
		}
		File fileOfDocumentLocation = new File(containerPath + document.getRemotedocumentpath() + JADE_VERSION);
		if (!fileOfDocumentLocation.exists()) {
			fileOfDocumentLocation.createNewFile();
		}

		Long newFileVersion;
		if (DocumentType.COPY.getCode().equals(document.getDocumenttype())) {
			newFileVersion = document.getRemoteversion();
		} else {
			newFileVersion = version;
		}
		String newLine = document.getDocumentname() + SEPERATOR + document.getRemotedocumentpath() + SEPERATOR
				+ newFileVersion;
		List<String> lines = Files.readAllLines(fileOfDocumentLocation.toPath());
		List<String> newLines = new ArrayList<String>();
		Boolean exists = false;
		for (String line : lines) {
			if (line.contains(document.getDocumentname())) {
				exists = true;
				newLines.add(newLine);
			} else {
				newLines.add(line);
			}
		}
		if (!exists) {
			newLines.add(newLine);
		}
		Files.write(fileOfDocumentLocation.toPath(), newLines);
		return newFileVersion;
	}

	@SuppressWarnings("unused")
	public synchronized void setDocumentScannerFile(String containerPath, Document document) throws IOException {
		File fileForDocumentScanner = new File(containerPath + File.separator + WAITING_LIST);

		if (!fileForDocumentScanner.exists()) {
			fileForDocumentScanner.createNewFile();
		}
		String newLine = document.getDocumentname() + SEPERATOR + document.getRemotedocumentpath() + SEPERATOR
				+ document.getDocumentname();
		List<String> lines = Files.readAllLines(fileForDocumentScanner.toPath());
		List<String> newLines = new ArrayList<String>();
		Boolean exists = false;
		for (String line : lines) {
			if (line.contains(document.getRemotedocumentpath())) {
				exists = true;
				newLines.add(newLine);
			} else {
				newLines.add(line);
			}
		}
		if (!exists) {
			newLines.add(newLine);
		}
		Files.write(fileForDocumentScanner.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized void setOwner(String containerPath, Document document) throws IOException {
		File file = new File(containerPath + document.getRemotedocumentpath() + JADE_VERSION);
		if (!file.exists()) {
			file.createNewFile();
		}

		// Eger dosya yazma sadece tek makineye ise ve kopya ile olusturulan bir
		// dosya degilse bekleyen listesi beslenir.
		// BU KISIM DEGISTI, ARTIK HEP GUNCELLEME YAPACAK,MOD YOK YANI
		setDocumentTimeoutFile(containerPath, document);

		List<String> lines = Files.readAllLines(file.toPath());
		List<String> newLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.contains(document.getDocumentname())) {

				String newline = line + SEPERATOR + document.getOwner() + SEPERATOR
						+ simpleDateFormat.format(Calendar.getInstance().getTime());
				newLines.add(newline);
			} else {
				newLines.add(line);
			}
		}
		Files.write(file.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized void setDocumentTimeoutFile(String containerPath, Document document) throws IOException {
		File fileForTimeoutScanner = new File(containerPath + File.separator + TIMEOUT_LIST);

		if (!fileForTimeoutScanner.exists()) {
			fileForTimeoutScanner.createNewFile();
		}
		String newLine = document.getDocumentname() + SEPERATOR + document.getRemotedocumentpath();
		List<String> lines = Files.readAllLines(fileForTimeoutScanner.toPath());
		List<String> newLines = new ArrayList<String>();
		Boolean exists = false;
		for (String line : lines) {
			if (line.contains(document.getRemotedocumentpath())) {
				exists = true;
				newLines.add(newLine);
			} else {
				newLines.add(line);
			}
		}
		if (!exists) {
			newLines.add(newLine);
		}
		Files.write(fileForTimeoutScanner.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized void setDocumentTimeoutFile(String containerPath, List<Document> documentList)
			throws IOException {
		File fileForTimeoutScanner = new File(containerPath + File.separator + TIMEOUT_LIST);

		if (!fileForTimeoutScanner.exists()) {
			fileForTimeoutScanner.createNewFile();
		}
		List<String> newLines = new ArrayList<String>();
		for (Document document : documentList) {
			String newLine = document.getDocumentname() + SEPERATOR + document.getRemotedocumentpath();
			newLines.add(newLine);
		}
		Files.write(fileForTimeoutScanner.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized void setDocumentSyncFile(String containerPath, List<Document> documentList) throws IOException {
		File fileForTimeoutScanner = new File(containerPath + File.separator + WAITING_LIST);

		if (!fileForTimeoutScanner.exists()) {
			fileForTimeoutScanner.createNewFile();
		}
		List<String> newLines = new ArrayList<String>();
		for (Document document : documentList) {
			String newLine = document.getDocumentname() + SEPERATOR + document.getRemotedocumentpath();
			newLines.add(newLine);
		}
		Files.write(fileForTimeoutScanner.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized List<Document> getDocumentTimeoutFile(String containerPath) throws IOException {
		return getFile(containerPath, TIMEOUT_LIST);
	}

	@SuppressWarnings("unused")
	public synchronized List<Document> getDocumentSyncFile(String containerPath) throws IOException {
		return getFile(containerPath, WAITING_LIST);
	}

	@SuppressWarnings("unused")
	public synchronized List<Document> getInvalidCacheFile(String containerPath) throws IOException {
		return getFile(containerPath, INVALID_CACHE_LIST);
	}

	@SuppressWarnings("unused")
	public synchronized String checkInvalidCacheFileAndGetLatestFileOwnerContainer(String containerPath,
			Document document) throws IOException {
		List<Document> invalidFiles = getFile(containerPath, INVALID_CACHE_LIST);
		for (Document invalidfile : invalidFiles) {
			if (invalidfile.getRemotedocumentpath().equals(document.getRemotedocumentpath())
					&& invalidfile.getDocumentname().equals(document.getDocumentname())) {
				return invalidfile.getOwnercontainer();
			}
		}
		return null;
	}

	@SuppressWarnings("unused")
	public synchronized List<Document> getFile(String containerPath, String fileName) throws IOException {
		File fileForTimeoutScanner = new File(containerPath + File.separator + fileName);
		List<Document> documentList = new ArrayList<Document>();

		if (!fileForTimeoutScanner.exists()) {
			return documentList;
		}

		List<String> lines = Files.readAllLines(fileForTimeoutScanner.toPath());
		for (String line : lines) {
			Document document = new Document();
			String[] split = line.split(SEPERATOR);
			if (split != null) {
				document.setDocumentname(split[0]);
				document.setRemotedocumentpath(split[1]);
				if (split.length > 2) {
					document.setOwnercontainer(split[2]);
				}
			}
			documentList.add(document);
		}
		return documentList;
	}

	@SuppressWarnings("unused")
	public synchronized void releaseOwner(String containerPath, Document document) throws IOException {
		File file = new File(containerPath + document.getRemotedocumentpath() + JADE_VERSION);
		if (!file.exists()) {
			file.createNewFile();
		}
		cleanDocumentTimeoutFile(containerPath, document);
		List<String> lines = Files.readAllLines(file.toPath());
		List<String> newLines = new ArrayList<String>();
		for (String line : lines) {
			if (line.contains(document.getDocumentname())) {
				String newline = line.substring(0, line.indexOf(document.getOwner()) - 3);
				newLines.add(newline);
			} else {
				newLines.add(line);
			}
		}
		Files.write(file.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized void cleanDocumentTimeoutFile(String containerPath, Document document) throws IOException {
		File fileForTimeoutScanner = new File(containerPath + File.separator + TIMEOUT_LIST);
		if (!fileForTimeoutScanner.exists()) {
			return;
		}
		List<String> lines = Files.readAllLines(fileForTimeoutScanner.toPath());
		List<String> newLines = new ArrayList<String>();
		Boolean exists = false;
		for (String line : lines) {
			if (!line.contains(document.getRemotedocumentpath())) {
				newLines.add(line);
			}
		}
		Files.write(fileForTimeoutScanner.toPath(), newLines);
	}

	@SuppressWarnings("unused")
	public synchronized void setInvalidCacheFile(String containerPath, Document document) throws IOException {
		File invalidCacheFile = new File(containerPath + File.separator + INVALID_CACHE_LIST);

		if (!invalidCacheFile.exists()) {
			invalidCacheFile.createNewFile();
		}
		String newLine = document.getDocumentname() + SEPERATOR + document.getRemotedocumentpath() + SEPERATOR
				+ document.getOwnercontainer();
		List<String> lines = Files.readAllLines(invalidCacheFile.toPath());
		List<String> newLines = new ArrayList<String>();
		Boolean exists = false;
		for (String line : lines) {
			if (line.contains(document.getRemotedocumentpath())) {
				exists = true;
				newLines.add(newLine);
			} else {
				newLines.add(line);
			}
		}
		if (!exists) {
			newLines.add(newLine);
		}
		Files.write(invalidCacheFile.toPath(), newLines);
	}

}
