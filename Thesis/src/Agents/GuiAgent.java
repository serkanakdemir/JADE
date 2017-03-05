package Agents;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;

import Guis.ClientGui;
import Guis.VersionManager;
import Ontology.Document;
import Ontology.DocumentOntology;
import jade.content.lang.sl.SLCodec;
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

public class GuiAgent extends Agent {

	protected static final String writeDocumentHandlerName = "DocumentHandler";
	protected static final String readDocumentHandlerName = "DocumentReader";
	protected static final String ownerShipHandlerName = "OwnerShipHandler";
	private ClientGui myGui;
	private String serverContainerName;

	@Override
	protected void setup() {
		myGui = new ClientGui(this);
		myGui.showGui();
		getContentManager().registerOntology(DocumentOntology.getInstance());
		getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
		registerService();
		setArguments();
		addBehaviour(new ServerResponseListener());
		addBehaviour(new StreamResponseListener());
		addBehaviour(new OwnerShipResponseListener());
		addBehaviour(new OwnerShipReleaseResponseListener());
	}

	private void setArguments() {
		Object[] arguments = getArguments();
		if (arguments != null && arguments.length > 0) {
			serverContainerName = (String) arguments[0];
			;
		}
	}

	private void registerService() {
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("guiAgent");
		sd.setName("JADE-guiAgent");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
	}

	public void addDocument(String fileName, String remoteFilePath, String localfilePath, String owner) {
		saveOrUpdateDocument(fileName, remoteFilePath, localfilePath, 0L, owner, "ADD");
	}

	public void updateDocument(String fileName, String remoteFilePath, String localfilePath, Long documentVersion,
			String owner) {
		saveOrUpdateDocument(fileName, remoteFilePath, localfilePath, documentVersion, owner, "UPDATE");
	}

	private void saveOrUpdateDocument(String fileName, String remoteFilePath, String localfilePath,
			Long documentVersion, String owner, String documenttype) {
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				try {
					DFAgentDescription[] result = findDocumentHandlers();
					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

					for (int i = 0; i < result.length; ++i) {
						message.addReceiver(result[i].getName());
					}
					message.setConversationId("client-document-add-request");
					message.setReplyWith("GuiAgent" + System.currentTimeMillis());
					message.setOntology(DocumentOntology.NAME);
					message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

					File file = new File(localfilePath + File.separator + fileName);

					Document document = new Document();
					document.setDocumenttype(documenttype);
					document.setDocumentname(fileName);
					document.setRemotedocumentpath(remoteFilePath);
					document.setLocaldocumentpath(localfilePath);
					document.setLocalversion(documentVersion);
					document.setOwner(owner);

					byte[] bytes = Files.readAllBytes(file.toPath());

					document.setByteArray(bytes);
					getContentManager().fillContent(message, document);
					myAgent.send(message);
				} catch (Exception e) {
					System.out.println(e);
				}
			}

			private DFAgentDescription[] findDocumentHandlers() throws FIPAException {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(writeDocumentHandlerName);
				sd.setOwnership(serverContainerName);
				template.addServices(sd);
				DFAgentDescription[] result = DFService.search(myAgent, template);
				return result;
			}
		});
	}

	public void showDocument(String fileName, String remoteFilePath, String localfilePath) {
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				try {
					DFAgentDescription[] result = findDocumentHandlers();
					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

					for (int i = 0; i < result.length; ++i) {
						message.addReceiver(result[i].getName());
					}
					message.setConversationId("client-document-show-request");
					message.setReplyWith("GuiAgent" + System.currentTimeMillis());

					message.setOntology(DocumentOntology.NAME);
					message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

					Document document = new Document();
					document.setDocumenttype("SHOW");
					document.setDocumentname(fileName);
					document.setRemotedocumentpath(remoteFilePath);
					document.setLocaldocumentpath(localfilePath);
					document.setLocalversion(0L);
					getContentManager().fillContent(message, document);
					myAgent.send(message);
				} catch (Exception e) {
					System.out.println(e);
				}
			}

			private DFAgentDescription[] findDocumentHandlers() throws FIPAException {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(readDocumentHandlerName);
				sd.setOwnership(serverContainerName);
				template.addServices(sd);
				DFAgentDescription[] result = DFService.search(myAgent, template);
				return result;
			}
		});
	}

	public void setDocumentOwner(String fileName, String remoteFilePath, String owner) {
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				try {
					DFAgentDescription[] result = findDocumentHandlers();
					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

					for (int i = 0; i < result.length; ++i) {
						message.addReceiver(result[i].getName());
					}
					message.setConversationId("client-document-ownership-request");
					message.setReplyWith("GuiAgent" + System.currentTimeMillis());

					message.setOntology(DocumentOntology.NAME);
					message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

					Document document = new Document();
					document.setDocumenttype("OWNERSHIP");
					document.setDocumentname(fileName);
					document.setRemotedocumentpath(remoteFilePath);
					document.setLocaldocumentpath("");
					document.setLocalversion(0L);
					document.setOwner(owner);
					getContentManager().fillContent(message, document);
					myAgent.send(message);
				} catch (Exception e) {
					System.out.println(e);
				}
			}

			private DFAgentDescription[] findDocumentHandlers() throws FIPAException {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(ownerShipHandlerName);
				sd.setOwnership(serverContainerName);
				template.addServices(sd);
				DFAgentDescription[] result = DFService.search(myAgent, template);
				return result;
			}
		});
	}

	public void releaseDocumentOwner(String fileName, String remoteFilePath, String owner) {
		addBehaviour(new OneShotBehaviour() {
			@Override
			public void action() {
				try {
					DFAgentDescription[] result = findDocumentHandlers();
					ACLMessage message = new ACLMessage(ACLMessage.REQUEST);

					for (int i = 0; i < result.length; ++i) {
						message.addReceiver(result[i].getName());
					}
					message.setConversationId("client-document-ownership-release-request");
					message.setReplyWith("GuiAgent" + System.currentTimeMillis());

					message.setOntology(DocumentOntology.NAME);
					message.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);

					Document document = new Document();
					document.setDocumenttype("OWNERSHIP_RELEASE");
					document.setDocumentname(fileName);
					document.setRemotedocumentpath(remoteFilePath);
					document.setLocaldocumentpath("");
					document.setLocalversion(0L);
					document.setOwner(owner);
					getContentManager().fillContent(message, document);
					myAgent.send(message);
				} catch (Exception e) {
					System.out.println(e);
				}
			}

			private DFAgentDescription[] findDocumentHandlers() throws FIPAException {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType(ownerShipHandlerName);
				sd.setOwnership(serverContainerName);
				template.addServices(sd);
				DFAgentDescription[] result = DFService.search(myAgent, template);
				return result;
			}
		});
	}

	class ServerResponseListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("client-document-show-request");
				ACLMessage serverMessage = myAgent.receive(mt);
				if (serverMessage != null) {
					if (serverMessage.getPerformative() == ACLMessage.CONFIRM) {
						myGui.showMessage("SUCCESS");
						Document document = (Document) getContentManager().extractContent(serverMessage);
						VersionManager versionManager = new VersionManager();
						versionManager.setVersionline(document.getLocaldocumentpath(), document.getDocumentname(),
								document.getRemotedocumentpath(), document.getRemoteversion());
					} else {
						String[] serverErrors = serverMessage.getContent().split("&&");
						StringBuilder stringBuilder = new StringBuilder();

						for (String error : serverErrors) {
							stringBuilder.append(error);
						}
						myGui.showMessage(stringBuilder.toString());
					}
				} else {
					block();
				}
			} catch (

			Exception e) {
				System.out.println(e);
			}
		}

	}

	class StreamResponseListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("client-document-add-request");
				ACLMessage serverMessage = myAgent.receive(mt);
				if (serverMessage != null) {
					System.out.println("STREAMRESPONSE");
					if (serverMessage.getPerformative() == ACLMessage.CONFIRM) {
						myGui.showMessage("SUCCESS");
						Document document = (Document) getContentManager().extractContent(serverMessage);
						VersionManager versionManager = new VersionManager();
						versionManager.setVersionline(document.getLocaldocumentpath(), document.getDocumentname(),
								document.getRemotedocumentpath(), document.getRemoteversion());
						FileOutputStream fos = new FileOutputStream(
								document.getLocaldocumentpath() + document.getDocumentname());
						fos.write(serverMessage.getByteSequenceContent());
						fos.close();
					} else {
						myGui.showMessage(serverMessage.getContent());
					}
				} else {
					block();
				}
			} catch (

			Exception e) {
				System.out.println(e);
			}
		}

	}

	class OwnerShipResponseListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("client-document-ownership-request");
				ACLMessage serverMessage = myAgent.receive(mt);
				if (serverMessage != null) {
					if (serverMessage.getPerformative() == ACLMessage.CONFIRM) {
						myGui.showMessage("SUCCESS");
					} else {
						myGui.showMessage(serverMessage.getContent());
					}
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	class OwnerShipReleaseResponseListener extends CyclicBehaviour {

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("client-document-ownership-release-request");
				ACLMessage serverMessage = myAgent.receive(mt);
				if (serverMessage != null) {
					if (serverMessage.getPerformative() == ACLMessage.CONFIRM) {
						myGui.showMessage("SUCCESS");
					} else {
						myGui.showMessage(serverMessage.getContent());
					}
				} else {
					block();
				}
			} catch (

			Exception e) {
				System.out.println(e);
			}
		}

	}

}
