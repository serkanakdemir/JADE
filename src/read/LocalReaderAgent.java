package read;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;

import Ontology.Document;
import Ontology.DocumentOntology;
import jade.content.lang.sl.SLCodec;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import serkan.VersionManager;

public class LocalReaderAgent extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String containerPath = null;

	@Override
	protected void setup() {
		getContentManager().registerOntology(DocumentOntology.getInstance());
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
		Object[] args = getArguments();
		containerPath = (String) args[0];

		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("local-reader");
		sd.setName("JADE-reader-scanner");
		sd.addOntologies(DocumentOntology.NAME);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new LocalReaderListener());
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
					Float remoteVersion = checkAndGetLocalVersion(document);
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

		private Float checkAndGetLocalVersion(Document clientDocument) {
			File file = new File(
					containerPath + clientDocument.getRemotedocumentpath() + clientDocument.getDocumentname());
			if (!file.exists()) {
				return null;
			} else {

				VersionManager versionManager = new VersionManager();
				Document remoteDocument = null;
				try {
					remoteDocument = versionManager
							.getVersionline(containerPath + clientDocument.getRemotedocumentpath(), file.getName());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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

		private Float checkAndGetLocalVersion(Document clientDocument) {
			File file = new File(
					containerPath + clientDocument.getRemotedocumentpath() + clientDocument.getDocumentname());
			if (!file.exists()) {
				return null;
			} else {

				VersionManager versionManager = new VersionManager();
				Document remoteDocument = null;
				try {
					remoteDocument = versionManager
							.getVersionline(containerPath + clientDocument.getRemotedocumentpath(), file.getName());
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return remoteDocument.getRemoteversion();
			}

		}
	}
}
