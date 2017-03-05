package read;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import Ontology.Document;
import Ontology.DocumentOntology;
import change.VersionManager;
import common.BaseAgent;
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

public class DocumentReaderAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("DocumentReader");
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
					System.out.println("Document read request is made, check invalid cache file first");
					Document document = (Document) getContentManager().extractContent(clientMessage);
					VersionManager manager = new VersionManager();
					String invalidFileOwnerContainer = manager
							.checkInvalidCacheFileAndGetLatestFileOwnerContainer(containerPath, document);
					// istenen döküman invalid listesinde bulunmuyor.
					if (invalidFileOwnerContainer == null) {
						Document localDocument = manager.getVersionline(
								containerPath + document.getRemotedocumentpath(), document.getDocumentname(), true);
						if (localDocument == null) {
							// Hem invalid listesinde yok, hem de lokal
							// makinede. Tüm makinelere sormalıyız.
							addBehaviour(new ReaderAgentCaller(clientMessage, document));
						} else {
							// elimizde son hali var demek, direk bunu dön
							// client'a
							ACLMessage reply = clientMessage.createReply();
							reply.setPerformative(ACLMessage.CONFIRM);
							document = new VersionManager().getVersionline(
									containerPath + document.getRemotedocumentpath(), document.getDocumentname(), true);
							getContentManager().fillContent(reply, document);
							myAgent.send(reply);
						}
					} else {
						// invalid döküman, git onun son halini ilgili makineden
						// çek.
						document.setOwnercontainer(invalidFileOwnerContainer);
						ACLMessage callForLatestVersion = new ACLMessage(ACLMessage.REQUEST);
						callForLatestVersion.setConversationId("get-file-stream");
						try {
							getContentManager().fillContent(callForLatestVersion, document);
							myAgent.send(callForLatestVersion);
							addBehaviour(new StreamAgentCaller(clientMessage, document));
						} catch (Exception e) {
							System.out.println(e);
						}
					}
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

	private class StreamAgentCaller extends OneShotBehaviour {
		Document document;
		ACLMessage clientMessage;

		StreamAgentCaller(ACLMessage clientMessage, Document document) {
			this.document = document;
			this.clientMessage = clientMessage;
		}

		@Override
		public void action() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-reader");
				sd.setOwnership(document.getOwnercontainer());
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("get-file-stream");
				cfp.setReplyWith(clientMessage.getReplyWith());
				cfp.setOntology(DocumentOntology.NAME);
				cfp.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
				for (int i = 0; i < result.length; ++i) {
					cfp.addReceiver(result[i].getName());
				}

				getContentManager().fillContent(cfp, document);
				myAgent.send(cfp);
				addBehaviour(new StreamListener(document, clientMessage));
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
		private Long version;
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
				Long responseVersion = null;
				String[] split = reply.getContent().split("&&&");
				String containerName = split[0];
				responseVersion = Long.valueOf(split[1]);

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
						reply = clientMessage.createReply();
						reply.setPerformative(ACLMessage.REFUSE);
						reply.setContent("NO SUCH FILE");
						myAgent.send(reply);
					} else if (containerName.equals(myAgent.getContainerController().getName())) {
						// son hali zaten elimizde var demek

						reply = clientMessage.createReply();
						reply.setPerformative(ACLMessage.CONFIRM);
						try {
							document = new VersionManager().getVersionline(
									containerPath + document.getRemotedocumentpath(), document.getDocumentname(), true);
							getContentManager().fillContent(reply, document);
							myAgent.send(reply);
						} catch (Exception e) {
							System.out.println("DocumentReaderAgent LocalReaderAgentResponseListener" + e);
						}

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
					// bence burda timer'a gerek yok.
					Long remoteVersion = versionManager.setVersionline(null, containerPath, document, null);
					ACLMessage responseClient = clientMessage.createReply();
					responseClient.setPerformative(ACLMessage.CONFIRM);
					file = new File(containerPath + document.getRemotedocumentpath() + document.getDocumentname());
					document.setRemoteversion(remoteVersion);
					document.setByteArray(Files.readAllBytes(file.toPath()));
					getContentManager().fillContent(responseClient, document);
					myAgent.send(responseClient);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			block();
		}

	}

}
