package change;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Ontology.Document;
import Ontology.DocumentOntology;
import common.BaseAgent;
import enums.DocumentType;
import enums.FileChangeMode;
import jade.content.lang.Codec.CodecException;
import jade.content.onto.OntologyException;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DocumentWriterAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("DocumentHandler");
		addBehaviour(new DocumentWriteListener());
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
					if (checkIfDocumentExistsForUpdate(document, clientMessage)) {
						addBehaviour(new ScannerAgentCaller(clientMessage, document));
					}
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private boolean checkIfDocumentExistsForUpdate(Document document, ACLMessage clientMessage) {
			File file = new File(containerPath + document.getRemotedocumentpath());
			if (!file.exists()) {
				if (document.getDocumenttype().equals("UPDATE")) {
					ACLMessage responseClient = clientMessage.createReply();
					responseClient.setPerformative(ACLMessage.REFUSE);
					responseClient.setContent("NOTHING TO UPDATE");
					myAgent.send(responseClient);
					return false;
				}
			}
			return true;
		}
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
			System.out.println("LocalScannerAgentResponseListener");
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("document-add-check"),
					MessageTemplate.MatchInReplyTo(clientMessage.getReplyWith()));
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				// dizinlerinde böyle bir döküman var mı, varsa reject
				// etmeliyiz.
				if (ACLMessage.CONFIRM == reply.getPerformative()) {
				} else {
					messageList.add(reply.getContent());
					failed = true;
				}
				responseCount++;

				if (scannerCount == responseCount) {
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
						System.out.println("herkes ok dedi, yazma zamanı");
						callLocalWriterAgent();

					}
				}
			} else {
				block();
			}
		}

		private void callLocalWriterAgent() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-writer");
				sd.setOwnership(myAgent.getContainerController().getContainerName());
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("document-local-write");
				cfp.setReplyWith(clientMessage.getReplyWith());
				cfp.setOntology(DocumentOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				for (int i = 0; i < result.length; ++i) {
					cfp.addReceiver(result[i].getName());
				}

				getContentManager().fillContent(cfp, document);
				addBehaviour(new LocalWriterAgentResponseListener(clientMessage, document));
				myAgent.send(cfp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public List<String> getMessageList() {
			return messageList;
		}

		public void setMessageList(List<String> messageList) {
			this.messageList = messageList;
		}

	}

	private class LocalWriterAgentResponseListener extends CyclicBehaviour {

		private ACLMessage clientMessage;
		private Document document;
		private List<String> messageList;

		public LocalWriterAgentResponseListener(ACLMessage clientMessage, Document document) {
			this.clientMessage = clientMessage;
			this.document = document;
			messageList = new ArrayList<String>();
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("document-local-write"),
					MessageTemplate.MatchInReplyTo(clientMessage.getReplyWith()));
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {

				if (ACLMessage.CONFIRM == reply.getPerformative()) {
					try {
						document = (Document) getContentManager().extractContent(reply);
						if (FileChangeMode.ALL_NODES.getCode().equals(fileChangeMode)) {
							document.setDocumenttype(DocumentType.COPY.getCode());
							callAllWriterAgent();
						}
					} catch (CodecException | OntologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ACLMessage responseClient = clientMessage.createReply();
					responseClient.setPerformative(ACLMessage.CONFIRM);

					try {
						getContentManager().fillContent(responseClient, document);
					} catch (CodecException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (OntologyException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					myAgent.send(responseClient);
				} else {
					ACLMessage responseClient = clientMessage.createReply();
					responseClient.setPerformative(ACLMessage.REFUSE);
					StringBuilder stringBuilder = new StringBuilder();
					for (String message : messageList) {
						stringBuilder.append(message);
						stringBuilder.append("&&");
					}
					responseClient.setContent(stringBuilder.toString());
					myAgent.send(responseClient);
				}

			} else {
				block();
			}
		}

		private void callAllWriterAgent() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-writer");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("document-write-all");
				cfp.setReplyWith(clientMessage.getReplyWith());
				cfp.setOntology(DocumentOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				for (int i = 0; i < result.length; ++i) {

					cfp.addReceiver(result[i].getName());
				}
				getContentManager().fillContent(cfp, document);

				myAgent.send(cfp);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public List<String> getMessageList() {
			return messageList;
		}

		public void setMessageList(List<String> messageList) {
			this.messageList = messageList;
		}

	}

}
