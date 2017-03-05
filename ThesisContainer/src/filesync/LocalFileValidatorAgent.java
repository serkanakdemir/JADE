package filesync;

import java.io.File;
import java.text.ParseException;

import Ontology.Document;
import change.VersionManager;
import common.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class LocalFileValidatorAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("local-file-validator");
		addBehaviour(new LocalFileValidatorListener());
	}

	private class LocalFileValidatorListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633444210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					Document document = (Document) getContentManager().extractContent(msg);
					updateLocalCacheFileIfNecessary(document);

				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println("LocalFileValidatorAgent" + e);
			}
		}

		private void updateLocalCacheFileIfNecessary(Document remoteDocument) throws ParseException {
			File file = new File(
					containerPath + remoteDocument.getRemotedocumentpath() + remoteDocument.getDocumentname());
			// böye bir dosya yoksa invalide çekilmesi gereken bir dosya yok
			// demektir.
			if (file.exists()) {
				VersionManager versionManager = new VersionManager();

				try {
					Document localDocument = versionManager.getVersionline(
							containerPath + remoteDocument.getRemotedocumentpath(), remoteDocument.getDocumentname(),
							false);
					// Eğer gönderilen dosya daha güncel ise, cache bilgisi
					// invalid'e çekilir.
					if (localDocument.getRemoteversion() < remoteDocument.getRemoteversion()) {
						localDocument.setOwnercontainer(remoteDocument.getOwnercontainer());
						versionManager.setInvalidCacheFile(containerPath, localDocument);
					}
				} catch (Exception e) {
					System.out.println("updateLocalCacheFileIfNecessary" + e);
				}

			}
		}
	}
}
