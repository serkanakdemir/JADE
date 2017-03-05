package Timeout;

import java.util.Calendar;
import java.util.Date;

import Ontology.Document;
import change.VersionManager;
import common.BaseAgent;
import jade.core.Location;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.wrapper.ControllerException;

public class TimeoutSailorAgent extends BaseAgent {

	/**
	 * Bu agent tum containerler uzerinde gezerek zamani dolan dosyalardan
	 * ownerligi geri alacak
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void setup() {
		baseSetup("sailor-agent");
		addTimeBehaviour();
		askContainerList();
		addBehaviour(new TimeoutChecker());
	}

	@Override
	protected void beforeMove() {
		containerPathAsked = false;
		containerListAsked = false;
		containerPath = null;
		globalTime = null;
	}

	@Override
	protected void afterMove() {
		try {
			System.out.println("hello new container: " + getContainerController().getContainerName());
		} catch (ControllerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		handleContentProperties();
		callToGetContainerPath();
		addBehaviour(new TimeoutChecker());
		sleepForAWhile();
	}

	@Override
	protected void goToNextContainer(Behaviour behaviour) {
		if (behaviour != null) {
			removeBehaviour(behaviour);
		}
		if (agentMoveContainerList.size() == 0) {
			System.out.println("containerSize=0, ask for it");
			askContainerList();
		} else {
			try {
				System.out.println("it is time to move from " + getContainerController().getContainerName());
			} catch (ControllerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			doMove((Location) agentMoveContainerList.remove(0));
		}

	}

	private class TimeoutChecker extends CyclicBehaviour {
		private static final long serialVersionUID = -9220633464210004640L;

		@Override
		public void action() {
			try {
				if (getContainerController().getContainerName().contains("Main")) {
					System.out.println("MainContainer,skip it");
					goToNextContainer(this);
					return;
				}
			} catch (ControllerException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (containerPath != null && getGlobalTime() != null) {
				try {
					System.out.println("We are in " + getContainerController().getContainerName());
				} catch (ControllerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				VersionManager manager = new VersionManager();
				try {
					java.util.List<Document> documentTimeoutFile = manager.getDocumentTimeoutFile(containerPath);
					java.util.List<Document> newDocumentTimeoutFile = new java.util.ArrayList<>();
					for (Document document : documentTimeoutFile) {
						// ilgili dizindeki jade-versionu oku
						// oradan bak timeout olmuşmu
						// olmuşsa listeden kaldır
						Document realDocument = manager.getVersionline(containerPath + document.getRemotedocumentpath(),
								document.getDocumentname(), false);
						Date startTime = realDocument.getLastOwnTime();
						Date endTime = new Date(getGlobalTime());

						Calendar calendar = Calendar.getInstance();
						calendar.setTime(startTime);
						calendar.add(Calendar.MINUTE, 30);
						startTime = calendar.getTime();

						if (startTime.before(endTime)) {
							// it is expired
							System.out.println(document.getDocumentname() + " is expired ");
						} else {
							newDocumentTimeoutFile.add(document);
						}

					}
					manager.setDocumentTimeoutFile(containerPath, newDocumentTimeoutFile);
					goToNextContainer(this);

				} catch (Exception e) {
					System.out.println("TimeoutSailorAgent" + e);
				}
			} else {
				System.out.println(
						"missing attribute , containerPath : " + containerPath + "getGlobalTime : " + getGlobalTime());
				block(60000);
			}
		}

	}

}
