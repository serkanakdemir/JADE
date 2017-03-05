
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class StartClass {

	public static void main(String[] args) throws StaleProxyException, InterruptedException {

		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl();
		// profile.setParameter(Profile.GUI, Boolean.TRUE.toString());
		// profile.setParameter(Profile.MAIN_PORT, String.valueOf(8085));
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.GUI, "true");

		AgentContainer mainContainer = runtime.createMainContainer(profile);
		if (mainContainer == null) {
			System.out.println("null");
		} else {
			System.out.println("oh bee");
		}

		runtime = Runtime.instance();
		profile = new ProfileImpl();
		// profile.setParameter(Profile.GUI, Boolean.TRUE.toString());
		// profile.setParameter(Profile.MAIN_PORT, String.valueOf(8085));
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.CONTAINER_NAME, "CONTAINER_1");
		AgentContainer agentContainer1 = runtime.createAgentContainer(profile);
		runtime = Runtime.instance();
		profile = new ProfileImpl();
		// profile.setParameter(Profile.GUI, Boolean.TRUE.toString());
		// profile.setParameter(Profile.MAIN_PORT, String.valueOf(8085));
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.CONTAINER_NAME, "CONTAINER_2");
		AgentContainer agentContainer2 = runtime.createAgentContainer(profile);
		runtime = Runtime.instance();
		profile = new ProfileImpl();
		// profile.setParameter(Profile.GUI, Boolean.TRUE.toString());
		// profile.setParameter(Profile.MAIN_PORT, String.valueOf(8085));
		profile.setParameter(Profile.MAIN_HOST, "localhost");
		profile.setParameter(Profile.CONTAINER_NAME, "CONTAINER_3");
		AgentContainer agentContainer3 = runtime.createAgentContainer(profile);

		String CHANGE_MODE = "ONLY_LOCAL_NODE";

		// Only One ContainerAgent!!
		Object[] pathFinderArgument = new Object[1];
		pathFinderArgument[0] = CHANGE_MODE;
		AgentController containerAgent = mainContainer.createNewAgent("containerAgent", "PathFinder.PathFinderAgent",
				pathFinderArgument);
		containerAgent.start();

		Thread.sleep(5000);

		AgentController writerAgent = agentContainer1.createNewAgent("writerAgent", "change.DocumentWriterAgent", null);
		writerAgent.start();

		AgentController scannerAgent1 = agentContainer1.createNewAgent("scannerAgent1", "change.LocalScannerAgent",
				null);
		scannerAgent1.start();

		AgentController scannerAgent2 = agentContainer2.createNewAgent("scannerAgent2", "change.LocalScannerAgent",
				null);
		scannerAgent2.start();

		AgentController scannerAgent3 = agentContainer3.createNewAgent("scannerAgent3", "change.LocalScannerAgent",
				null);
		scannerAgent3.start();

		AgentController readerAgent = agentContainer1.createNewAgent("readerAgent", "read.DocumentReaderAgent", null);
		readerAgent.start();

		AgentController rdrAgent1 = agentContainer1.createNewAgent("rdrAgent1", "read.LocalReaderAgent", null);
		rdrAgent1.start();

		AgentController rdrAgent2 = agentContainer2.createNewAgent("rdrAgent2", "read.LocalReaderAgent", null);
		rdrAgent2.start();

		AgentController rdrAgent3 = agentContainer3.createNewAgent("rdrAgent3", "read.LocalReaderAgent", null);
		rdrAgent3.start();

		AgentController ownerShipAgent = agentContainer1.createNewAgent("ownerShipAgent",
				"ownership.DocumentOwnerShipAgent", null);
		ownerShipAgent.start();

		AgentController ownerShipAgent1 = agentContainer1.createNewAgent("ownerShipAgent1",
				"ownership.LocalOwnerShipAgent", null);
		ownerShipAgent1.start();

		AgentController ownerShipAgent2 = agentContainer2.createNewAgent("ownerShipAgent2",
				"ownership.LocalOwnerShipAgent", null);
		ownerShipAgent2.start();

		AgentController ownerShipAgent3 = agentContainer3.createNewAgent("ownerShipAgent3",
				"ownership.LocalOwnerShipAgent", null);
		ownerShipAgent3.start();

		AgentController localWriterAgent1 = agentContainer1.createNewAgent("localWriterAgent1",
				"change.LocalWriterAgent", null);
		localWriterAgent1.start();

		AgentController localWriterAgent2 = agentContainer2.createNewAgent("localWriterAgent2",
				"change.LocalWriterAgent", null);
		localWriterAgent2.start();

		AgentController localWriterAgent3 = agentContainer3.createNewAgent("localWriterAgent3",
				"change.LocalWriterAgent", null);
		localWriterAgent3.start();

		AgentController invalidCacheAgent1 = agentContainer1.createNewAgent("invalidCacheAgent1",
				"filesync.LocalFileValidatorAgent", null);
		invalidCacheAgent1.start();

		AgentController invalidCacheAgent2 = agentContainer2.createNewAgent("invalidCacheAgent2",
				"filesync.LocalFileValidatorAgent", null);
		invalidCacheAgent2.start();

		AgentController invalidCacheAgent3 = agentContainer3.createNewAgent("invalidCacheAgent3",
				"filesync.LocalFileValidatorAgent", null);
		invalidCacheAgent3.start();

		// important things
		// Only One TimerAgent!!
		AgentController timerAgent = mainContainer.createNewAgent("timerAgent", "Synchronization.TimerAgent", null);
		timerAgent.start();

		//
		// AgentController timeoutSailorAgent =
		// mainContainer.createNewAgent("timeoutSailorAgent",
		// "Timeout.TimeoutSailorAgent", null);
		// timeoutSailorAgent.start();
		//
		AgentController fileMatcherAgent = mainContainer.createNewAgent("fileMatcherAgent", "filesync.FileMatcherAgent",
				null);
		fileMatcherAgent.start();

		System.out.println("DONE");
	}

}
