package read;

import java.io.File;
import java.nio.file.Files;

import Ontology.Document;
import change.VersionManager;
import common.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class LocalReaderAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("local-reader");
		addBehaviour(new LocalReaderListener());
		addBehaviour(new StreamRequestListener());
	}

	private class LocalReaderListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("document-read-check");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {

					Document document = (Document) getContentManager().extractContent(msg);

					ACLMessage reply = msg.createReply();
					Long remoteVersion = checkAndGetLocalVersion(document);
					reply.setContent(remoteVersion != null
							? myAgent.getContainerController().getName() + "&&&" + remoteVersion.toString()
							: myAgent.getContainerController().getName() + "&&&" + 0);
					reply.setPerformative(ACLMessage.INFORM);
					myAgent.send(reply);
				} else {
					block();
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private Long checkAndGetLocalVersion(Document clientDocument) {
			File file = new File(
					containerPath + clientDocument.getRemotedocumentpath() + clientDocument.getDocumentname());
			if (!file.exists()) {
				return null;
			} else {
				VersionManager versionManager = new VersionManager();
				Document remoteDocument = null;
				try {
					remoteDocument = versionManager.getVersionline(
							containerPath + clientDocument.getRemotedocumentpath(), file.getName(), false);
				} catch (Exception e) {
					System.out.println("LocalReaderAgent checkAndGetLocalVersion" + e);
				}
				return remoteDocument.getRemoteversion();
			}

		}
	}

	private class StreamRequestListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("get-file-stream");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {

					Document document = (Document) getContentManager().extractContent(msg);
					File file = new File(containerPath + document.getRemotedocumentpath() + document.getDocumentname());
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.AGREE);
					reply.setByteSequenceContent(Files.readAllBytes(file.toPath()));

					myAgent.send(reply);
				} else {
					block();
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private Long checkAndGetLocalVersion(Document clientDocument) {
			File file = new File(
					containerPath + clientDocument.getRemotedocumentpath() + clientDocument.getDocumentname());
			if (!file.exists()) {
				return null;
			} else {

				VersionManager versionManager = new VersionManager();
				Document remoteDocument = null;
				try {
					remoteDocument = versionManager.getVersionline(
							containerPath + clientDocument.getRemotedocumentpath(), file.getName(), false);
				} catch (Exception e) {
					System.out.println("LocalReaderAgent checkAndGetLocalVersion" + e);
				}
				return remoteDocument.getRemoteversion();
			}

		}
	}
}
