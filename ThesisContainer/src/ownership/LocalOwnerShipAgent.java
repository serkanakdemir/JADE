package ownership;

import java.io.File;

import Ontology.Document;
import change.VersionManager;
import common.BaseAgent;
import enums.ResponseOwnerShip;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class LocalOwnerShipAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("local-ownership");
		addBehaviour(new LocalOwnerShipListener());
	}

	private class LocalOwnerShipListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {

					Document document = (Document) getContentManager().extractContent(msg);

					ACLMessage reply = msg.createReply();
					ResponseOwnerShip checkOwner = checkLocalForOwnerShip(document);
					reply.setContent(myAgent.getLocalName() + " : " + checkOwner.getMessage());
					if (ResponseOwnerShip.SUCCESS.equals(checkOwner)) {
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

		private ResponseOwnerShip checkLocalForOwnerShip(Document clientDocument) {
			File file = new File(
					containerPath + clientDocument.getRemotedocumentpath() + clientDocument.getDocumentname());
			// böye bir directory yoksa zaten sahibi olan biri yok demektir.
			if (!file.exists()) {
				return ResponseOwnerShip.SUCCESS;
			} else {

				VersionManager versionManager = new VersionManager();
				try {
					Document remoteDocument = versionManager.getVersionline(
							containerPath + clientDocument.getRemotedocumentpath(), clientDocument.getDocumentname(),
							false);
					if (remoteDocument.getOwner() == null || remoteDocument.getOwner().length() == 0
							|| clientDocument.getOwner().equals(remoteDocument.getOwner())) {
						return ResponseOwnerShip.SUCCESS;
					} else {
						return ResponseOwnerShip.ALREADY_OWNED;
					}
				} catch (Exception e) {
					System.out.println("LocalOwnerShipAgent" + e);

				}
				return ResponseOwnerShip.UNKNOWN_ERROR;
			}

		}
	}
}
