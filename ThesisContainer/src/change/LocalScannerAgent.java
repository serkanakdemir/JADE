package change;

import java.io.File;
import java.io.FilenameFilter;

import Ontology.Document;
import common.BaseAgent;
import enums.DocumentType;
import enums.ResponseWriteVersion;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class LocalScannerAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("local-scanner");
		addBehaviour(new LocalScannerListener());
	}

	private class LocalScannerListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {

					Document document = (Document) getContentManager().extractContent(msg);

					ACLMessage reply = msg.createReply();
					ResponseWriteVersion checkLocalPathIsOkay = checkLocalPathIsOkay(document);
					reply.setContent(myAgent.getLocalName() + " : " + checkLocalPathIsOkay.getMessage());
					if (ResponseWriteVersion.SUCCESS.equals(checkLocalPathIsOkay)) {
						reply.setPerformative(ACLMessage.CONFIRM);
						System.out.println(myAgent.getLocalName() + "kabul etti");
					} else {
						reply.setPerformative(ACLMessage.REFUSE);
						System.out.println(myAgent.getLocalName() + "reject etti");
					}
					myAgent.send(reply);
				} else {
					block();
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private ResponseWriteVersion checkLocalPathIsOkay(Document clientDocument) {
			File file = new File(
					containerPath + clientDocument.getRemotedocumentpath() + clientDocument.getDocumentname());
			// böye bir directory yoksa sorun yok demektir
			if (!file.exists()) {
				return ResponseWriteVersion.SUCCESS;
			} else if (DocumentType.ADD.getCode().equals(clientDocument.getDocumenttype())) {
				return ResponseWriteVersion.ALREADY_EXISTS;
			} else {
				FilenameFilter filter = new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						if (name.equals(clientDocument.getDocumentname())) {
							return true;
						}
						return false;
					}
				};
				file = new File(containerPath + clientDocument.getRemotedocumentpath());
				String[] list = file.list(filter);
				// böyle bir dosya yoksa sorun yok demektir
				if (list == null || list.length == 0) {
					return ResponseWriteVersion.SUCCESS;
				}
				// böyle bir dosya varsa client Version Remote versiyondan büyük
				// yada eşit olmalı.
				if (clientDocument.getRemoteversion() == null) {
					return ResponseWriteVersion.MISSING_VERSION;
				}
				VersionManager versionManager = new VersionManager();
				try {
					Document remoteDocument = versionManager.getVersionline(file.getPath(),
							clientDocument.getDocumentname(), false);
					if (remoteDocument.getOwner() != null
							&& !remoteDocument.getOwner().equals(clientDocument.getOwner())) {
						return ResponseWriteVersion.OWNED_BY_ANOTHER_USER;
					}
					if (remoteDocument.getRemoteversion() <= clientDocument.getLocalversion()) {
						return ResponseWriteVersion.SUCCESS;
					} else {
						return ResponseWriteVersion.OLD_VERSION;
					}
				} catch (Exception e) {
					System.out.println("LocalScannerAgent" + e);
				}
				return ResponseWriteVersion.UNKNOWN_ERROR;
			}

		}
	}
}
