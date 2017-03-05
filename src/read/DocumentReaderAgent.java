package read;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import Ontology.Document;
import Ontology.DocumentOntology;
import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import serkan.VersionManager;

public class DocumentReaderAgent extends Agent {

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
		sd.setType("DocumentReader1");
		sd.setName("JADE-DocumentReader1");
		sd.addOntologies(DocumentOntology.NAME);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new DocumentReadListener());
	}

	private class DocumentReadListener extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
						MessageTemplate.MatchConversationId("client-document-show-request"));
				ACLMessage clientMessage = myAgent.receive(mt);
				if (clientMessage != null) {
					Document document = (Document) getContentManager().extractContent(clientMessage);
					addBehaviour(new ReaderAgentCaller(clientMessage, document));
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	private class ReaderAgentCaller extends OneShotBehaviour {
		Document document;
		ACLMessage clientMessage;

		ReaderAgentCaller(ACLMessage clientMessage, Document document) {
			this.document = document;
			this.clientMessage = clientMessage;
		}

		@Override
		public void action() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-reader");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("document-read-check");
				cfp.setReplyWith(clientMessage.getReplyWith());
				cfp.setOntology(DocumentOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				for (int i = 0; i < result.length; ++i) {
					cfp.addReceiver(result[i].getName());
				}

				getContentManager().fillContent(cfp, document);
				myAgent.send(cfp);
				addBehaviour(new LocalReaderAgentResponseListener(result.length, clientMessage, document));
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private class LocalReaderAgentResponseListener extends CyclicBehaviour {

		private Integer scannerCount = 0;
		private Integer responseCount = 0;
		Boolean failed = false;
		private ACLMessage clientMessage;
		private Document document;
		private Float version;
		private ACLMessage latestVersionMessage;

		public LocalReaderAgentResponseListener(Integer scannerCount, ACLMessage clientMessage, Document document) {
			this.scannerCount = scannerCount;
			this.clientMessage = clientMessage;
			this.document = document;
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("document-read-check"),
					MessageTemplate.MatchInReplyTo(clientMessage.getReplyWith()));
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				Float responseVersion = null;
				String[] split = reply.getContent().split("&&&");
				String containerName = split[0];
				responseVersion = Float.valueOf(split[1]);

				if (version == null) {
					version = responseVersion;
					latestVersionMessage = reply;
				} else if (responseVersion != null && responseVersion > version) {
					version = responseVersion;
					latestVersionMessage = reply;
				}

				responseCount++;

				if (scannerCount == responseCount) {
					myAgent.getContainerController().getName();

					// kimsede böyle döküman yok
					if (version == null) {

					} else if (containerName.equals(myAgent.getContainerController().getName())) {
						// son hali zaten elimizde var demek
					} else {
						// son hali başka makinede var, ondan son halini
						// almalıyız.
						ACLMessage callForLatestVersion = latestVersionMessage.createReply();
						callForLatestVersion.setConversationId("get-file-stream");
						try {
							getContentManager().fillContent(callForLatestVersion, document);
							myAgent.send(callForLatestVersion);
							addBehaviour(new StreamListener(document, clientMessage));
						} catch (CodecException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (OntologyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			} else {
				block();
			}
		}

	}

	private class StreamListener extends CyclicBehaviour {
		private Document document;
		private ACLMessage clientMessage;

		public StreamListener(Document document, ACLMessage clientMessage) {
			this.document = document;
			this.clientMessage = clientMessage;
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.MatchConversationId("get-file-stream");
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				File file = new File(containerPath + document.getRemotedocumentpath());
				if (!file.exists()) {
					file.mkdirs();
				}
				byte[] byteSequenceContent = reply.getByteSequenceContent();
				FileOutputStream fos;
				try {
					fos = new FileOutputStream(
							containerPath + document.getRemotedocumentpath() + document.getDocumentname());
					fos.write(byteSequenceContent);
					fos.close();
					VersionManager versionManager = new VersionManager();
					Float remoteVersion = versionManager.setVersionline(
							containerPath + document.getRemotedocumentpath(), document.getDocumentname(),
							document.getRemotedocumentpath());
					ACLMessage responseClient = clientMessage.createReply();
					responseClient.setByteSequenceContent(byteSequenceContent);
					responseClient.setPerformative(ACLMessage.CONFIRM);
					document.setRemoteversion(remoteVersion);
					getContentManager().fillContent(responseClient, document);
					myAgent.send(responseClient);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (CodecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OntologyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			block();
		}

	}

}
