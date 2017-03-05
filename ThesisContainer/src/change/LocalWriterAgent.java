package change;

import java.io.File;
import java.io.FileOutputStream;

import Ontology.Document;
import Ontology.Timer;
import Ontology.TimerOntology;
import common.BaseAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class LocalWriterAgent extends BaseAgent {

	@Override
	protected void setup() {
		baseSetup("local-writer");
		addBehaviour(new LocalWriterListener());
	}

	private class LocalWriterListener extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633464210004640L;

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					System.out.println("Yazıcı version çağrısını başlatıyor");
					Document document = (Document) getContentManager().extractContent(msg);
					ACLMessage callTimer = new ACLMessage(ACLMessage.REQUEST);

					// findTimerAgent
					DFAgentDescription template = new DFAgentDescription();
					ServiceDescription sd = new ServiceDescription();
					sd.setType("TimerAgent");
					template.addServices(sd);

					DFAgentDescription[] result = DFService.search(myAgent, template);
					callTimer.setConversationId("get-current-time");
					callTimer.setReplyWith(msg.getReplyWith());
					callTimer.setOntology(TimerOntology.NAME);
					callTimer.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
					for (int i = 0; i < result.length; ++i) {
						callTimer.addReceiver(result[i].getName());
					}
					myAgent.send(callTimer);
					addBehaviour(new LocalWriter(msg, document));
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

	}

	private class LocalWriter extends OneShotBehaviour {
		private static final long serialVersionUID = -9220633464210004640L;
		private ACLMessage clientMessge;
		private Document document;

		public LocalWriter(ACLMessage clientMessge, Document document) {
			this.clientMessge = clientMessge;
			this.document = document;
		}

		@Override
		public void action() {
			try {
				MessageTemplate mt = MessageTemplate.MatchConversationId("get-current-time");
				ACLMessage msg = myAgent.receive(mt);
				if (msg != null) {
					System.out.println("Yazıcı çağırıldı");
					Timer timer = (Timer) getContentManager().extractContent(msg);
					ACLMessage reply = clientMessge.createReply();
					setDocument(writeToLocalNode(getDocument(), timer.getTime()));
					if (getDocument() != null) {
						reply.setPerformative(ACLMessage.CONFIRM);
						getContentManager().fillContent(reply, getDocument());
						System.out.println(myAgent.getLocalName() + "yükleme işlemi yapıldı");
					} else {
						reply.setPerformative(ACLMessage.REFUSE);
						System.out.println(myAgent.getLocalName() + "yükleme işlemi hata");
					}
					myAgent.send(reply);
				} else {
					block();
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}

		private Document writeToLocalNode(Document document, Long version) {
			// yükleme işlemini yap
			try {
				File file = new File(containerPath + document.getRemotedocumentpath());
				if (!file.exists()) {
					file.mkdirs();
				}

				FileOutputStream fos = new FileOutputStream(
						containerPath + document.getRemotedocumentpath() + document.getDocumentname());
				System.out.println("File is written to : " + containerPath + document.getRemotedocumentpath()
						+ document.getDocumentname());
				fos.write(document.getByteArray());
				fos.close();
				VersionManager versionManager = new VersionManager();
				Long remoteVersion = versionManager.setVersionline(fileChangeMode, containerPath, document, version);
				document.setRemoteversion(remoteVersion);
				return document;
			} catch (Exception e) {
				System.out.println("writeToLocalNode hatası!" + e.getMessage());
				return null;
			}

		}

		public ACLMessage getClientMessge() {
			return clientMessge;
		}

		public void setClientMessge(ACLMessage clientMessge) {
			this.clientMessge = clientMessge;
		}

		public Document getDocument() {
			return document;
		}

		public void setDocument(Document document) {
			this.document = document;
		}

	}
}
