package ownership;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Ontology.Document;
import Ontology.DocumentOntology;
import change.VersionManager;
import common.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class DocumentOwnerShipAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("OwnerShipHandler");
		addBehaviour(new DocumentOwnerListener());
		addBehaviour(new DocumentOwnerReleaseListener());// sadece kendine bak,
															// başka makinelere
															// requeste gerek
		/**
		 * Bunun için ayrı bir Agent yazabiliriz. // yok
		 */
		// addBehaviour(new DocumentOwnerReleaseTimeout());// arada bak ,
		// sahipliği
		// kaldır

	}

	private class OwnerShipAgentCaller extends OneShotBehaviour {
		Document document;
		ACLMessage clientMessage;

		OwnerShipAgentCaller(ACLMessage clientMessage, Document document) {
			this.document = document;
			this.clientMessage = clientMessage;
		}

		@Override
		public void action() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-ownership");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("document-ownership");
				cfp.setReplyWith(clientMessage.getReplyWith());
				cfp.setOntology(DocumentOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				for (int i = 0; i < result.length; ++i) {
					cfp.addReceiver(result[i].getName());
				}

				getContentManager().fillContent(cfp, document);
				addBehaviour(new LocalOwnershipAgentResponseListener(result.length, clientMessage, document));
				myAgent.send(cfp);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private class LocalOwnershipAgentResponseListener extends CyclicBehaviour {

		private Integer scannerCount = 0;
		private Integer responseCount = 0;
		Boolean failed = false;
		private ACLMessage clientMessage;
		private Document document;
		private List<String> messageList;

		public LocalOwnershipAgentResponseListener(Integer scannerCount, ACLMessage clientMessage, Document document) {
			this.scannerCount = scannerCount;
			this.clientMessage = clientMessage;
			this.document = document;
			messageList = new ArrayList<String>();
		}

		@Override
		public void action() {
			MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchConversationId("document-ownership"),
					MessageTemplate.MatchInReplyTo(clientMessage.getReplyWith()));
			ACLMessage reply = myAgent.receive(mt);
			if (reply != null) {
				// başka makinelerde sahiplenilmiş mi ona bak
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
						// dosya sahipliğini alabiliriz
						try {
							File file = new File(containerPath + document.getRemotedocumentpath());
							if (!file.exists()) {
								file.mkdirs();
							}

							VersionManager versionManager = new VersionManager();
							Document remoteDocument = versionManager.getVersionline(
									containerPath + document.getRemotedocumentpath(), document.getDocumentname(),
									false);

							if (!document.getOwner().equals(remoteDocument.getOwner())) {
								versionManager.setOwner(containerPath, document);
							}

							ACLMessage responseClient = clientMessage.createReply();
							responseClient.setPerformative(ACLMessage.CONFIRM);
							getContentManager().fillContent(responseClient, document);
							myAgent.send(responseClient);
						} catch (Exception e) {
							System.out.println("" + e);
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

	private class DocumentOwnerListener extends CyclicBehaviour {
		/**
		 * 
		 */
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
						MessageTemplate.MatchConversationId("client-document-ownership-request"));
				ACLMessage clientMessage = myAgent.receive(mt);
				if (clientMessage != null) {
					Document document = (Document) getContentManager().extractContent(clientMessage);
					if (checkIfDocumentExistsForUpdate(document, clientMessage)) {
						addBehaviour(new OwnerShipAgentCaller(clientMessage, document));
					}
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println("DocumentOwnerShipAgent during owning" + e);
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

	private class DocumentOwnerReleaseListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.and(MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
						MessageTemplate.MatchConversationId("client-document-ownership-release-request"));
				ACLMessage clientMessage = myAgent.receive(mt);
				if (clientMessage != null) {
					ACLMessage responseClient = clientMessage.createReply();
					Document document = (Document) getContentManager().extractContent(clientMessage);
					if (checkIfDocumentExistsForUpdate(document, clientMessage)) {
						VersionManager versionManager = new VersionManager();
						Document remoteDocument = versionManager.getVersionline(
								containerPath + document.getRemotedocumentpath(), document.getDocumentname(), false);

						if (document.getOwner().equals(remoteDocument.getOwner())) {
							versionManager.releaseOwner(containerPath, document);
							responseClient.setPerformative(ACLMessage.CONFIRM);
						} else {
							responseClient.setPerformative(ACLMessage.REFUSE);
							responseClient.setContent("Dosya sahibi uyuşmuyor.");
						}
						// getContentManager().fillContent(responseClient,
						// document);
						myAgent.send(responseClient);
					}
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println("DocumentOwnerShipAgent during release" + e);
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
}
