package serkan;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

public class DocumentWriterAgent extends Agent {

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
		sd.setType("DocumentHandler1");
		sd.setName("JADE-DocumentHandler1");
		sd.addOntologies(DocumentOntology.NAME);
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		addBehaviour(new DocumentWriteListener());
	}

	private class ScannerAgentCaller extends OneShotBehaviour {
		Document document;
		ACLMessage clientMessage;

		ScannerAgentCaller(ACLMessage clientMessage, Document document) {
			this.document = document;
			this.clientMessage = clientMessage;
		}

		@Override
		public void action() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-scanner");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("document-add-check");
				cfp.setReplyWith(clientMessage.getReplyWith());
				cfp.setOntology(DocumentOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				for (int i = 0; i < result.length; ++i) {
					cfp.addReceiver(result[i].getName());
				}

				getContentManager().fillContent(cfp, document);
				addBehaviour(new LocalScannerAgentResponseListener(result.length, clientMessage, document));
				myAgent.send(cfp);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private class LocalScannerAgentResponseListener extends CyclicBehaviour {

		private Integer scannerCount = 0;
		private Integer responseCount = 0;
		Boolean failed = false;
		private ACLMessage clientMessage;
		private Document document;
		private List<String> messageList;

		public LocalScannerAgentResponseListener(Integer scannerCount, ACLMessage clientMessage, Document document) {
			this.scannerCount = scannerCount;
			this.clientMessage = clientMessage;
			this.document = document;
			messageList = new ArrayList<String>();
		}

		@Override
		public void action() {
			System.out.println("Selaaam");
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("document-add-check"),
					MessageTemplate.MatchInReplyTo(clientMessage.getReplyWith()));
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				System.out.println("yyyyyyy");
				// dizinlerinde böyle bir döküman var mı, varsa reject
				// etmeliyiz.
				if (ACLMessage.CONFIRM == reply.getPerformative()) {
				} else {
					messageList.add(reply.getContent());
					failed = true;
				}
				responseCount++;

				if (scannerCount == responseCount) {
					System.out.println("dddddddddddd");
					if (failed) {
						ACLMessage responseClient = clientMessage.createReply();
						responseClient.setPerformative(ACLMessage.REFUSE);
						StringBuilder stringBuilder = new StringBuilder();
						for (String message : messageList) {
							stringBuilder.append(message);
							stringBuilder.append("&&");
						}
						responseClient.setContent(stringBuilder.toString());
						myAgent.send(responseClient);
					} else {
						// yükleme işlemini yap
						try {
							File file = new File(containerPath + document.getRemotedocumentpath());
							if (!file.exists()) {
								file.mkdirs();
							}

							FileOutputStream fos = new FileOutputStream(
									containerPath + document.getRemotedocumentpath() + document.getDocumentname());
							fos.write(document.getByteArray());
							fos.close();
							VersionManager versionManager = new VersionManager();
							Float remoteVersion = versionManager.setVersionline(
									containerPath + document.getRemotedocumentpath(), document.getDocumentname(),
									document.getRemotedocumentpath());

							ACLMessage responseClient = clientMessage.createReply();
							responseClient.setPerformative(ACLMessage.CONFIRM);
							document.setRemoteversion(remoteVersion);
							getContentManager().fillContent(responseClient, document);
							myAgent.send(responseClient);
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
				}
			} else {
				block();
			}
		}

		public List<String> getMessageList() {
			return messageList;
		}

		public void setMessageList(List<String> messageList) {
			this.messageList = messageList;
		}

	}

	// this behaviour will be called from Gui Agent
	private class DocumentWriteListener extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
						MessageTemplate.MatchConversationId("client-document-add-request"));
				ACLMessage clientMessage = myAgent.receive(mt);
				if (clientMessage != null) {
					Document document = (Document) getContentManager().extractContent(clientMessage);
					addBehaviour(new ScannerAgentCaller(clientMessage, document));
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
}
