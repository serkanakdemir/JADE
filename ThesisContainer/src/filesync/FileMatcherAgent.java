package filesync;

import java.util.ArrayList;

import Ontology.Document;
import Ontology.DocumentOntology;
import change.VersionManager;
import common.BaseAgent;
import enums.DocumentType;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.wrapper.ControllerException;

public class FileMatcherAgent extends BaseAgent {

	/**
	 * Bu agent tum containerler uzerinde gezerek file güncellemelerini yapacak
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {
		baseSetup("file-matcher-agent");
		askContainerList();
		addBehaviour(new FileChecker());
	}

	@Override
	protected void beforeMove() {
		containerPathAsked = false;
		containerListAsked = false;
		containerPath = null;
	}

	@Override
	protected void afterMove() {
		try {
			System.out.println(
					"FileMatcherAgent says: hello new container: " + getContainerController().getContainerName());
			sleepForAWhile();
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		handleContentProperties();
		callToGetContainerPath();
		addBehaviour(new FileChecker());
		sleepForAWhile();
	}

	@Override
	protected void goToNextContainer(Behaviour behaviour) {
		if (behaviour != null) {
			removeBehaviour(behaviour);
		}
		if (agentMoveContainerList.size() == 0) {
			System.out.println("fileMatcherAgent Says containerSize=0, ask for it");
			askContainerList();
		} else {
			try {
				System.out.println("FileMatcherAgent says: it is time to move from "
						+ getContainerController().getContainerName());
			} catch (ControllerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doMove((Location) agentMoveContainerList.remove(0));
		}

	}

	private class FileChecker extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633464210004640L;

		@Override
		public void action() {
			try {
				if (getContainerController().getContainerName().contains("Main")) {
					System.out.println("FileMatcherAgent MainContainer,skip it");
					goToNextContainer(this);
					return;
				}
			} catch (ControllerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (containerPath != null) {
				try {
					System.out.println("FileMatcher  : We are in " + getContainerController().getContainerName());
				} catch (ControllerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				VersionManager manager = new VersionManager();
				try {
					java.util.List<Document> syncFileList = manager.getDocumentSyncFile(containerPath);
					if (syncFileList != null && syncFileList.size() > 0) {
						for (Document document : syncFileList) {
							Document realDocument = manager.getVersionline(
									containerPath + document.getRemotedocumentpath(), document.getDocumentname(),
									false);
							// Dosyanın sahibi yani şuan bulunan container adı
							// yazılır ki, böylece karşı taraf ihtiyacı olunca
							// direk bu container üzerine talep açar.
							realDocument.setOwnercontainer(getContainerController().getContainerName());
							realDocument.setDocumenttype(DocumentType.COPY.getCode());
							addBehaviour(new SendInvalidCacheMessage(realDocument));
						}
						manager.setDocumentSyncFile(containerPath, new ArrayList<Document>());
					} else {
						System.out.println("FileMatcher : Nothing to process here");
					}
					goToNextContainer(this);

				} catch (Exception e) {
					System.out.println("FileMatcherAgent" + e);
				}
			} else {
				System.out.println(
						"missing attribute , containerPath : " + containerPath + "getGlobalTime : " + getGlobalTime());
				block(1000);
			}
		}

	}

	private class SendInvalidCacheMessage extends OneShotBehaviour {
		Document document;

		SendInvalidCacheMessage(Document document) {
			this.document = document;
		}

		@Override
		public void action() {
			try {
				DFAgentDescription template = new DFAgentDescription();
				ServiceDescription sd = new ServiceDescription();
				sd.setType("local-file-validator");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);
				ACLMessage cfp = new ACLMessage(ACLMessage.REQUEST);
				cfp.setConversationId("validate-local-machine");
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
	}

}
