
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class StartClass {

	public static void main(String[] args) throws StaleProxyException {
		Runtime runtime = Runtime.instance();
		Profile profile = new ProfileImpl();
		// profile.setParameter(Profile.GUI, Boolean.TRUE.toString());
		// profile.setParameter(Profile.MAIN_PORT, String.valueOf(8085));
		profile.setParameter(Profile.MAIN_HOST, "localhost");

		AgentContainer mainContainer = runtime.createMainContainer(profile);
		if (mainContainer == null) {
			System.out.println("null");
		} else {
			System.out.println("oh bee");
		}

		Object[] arguments1 = new Object[1];
		arguments1[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC1";
		AgentController writerAgent = mainContainer.createNewAgent("writerAgent", "serkan.DocumentWriterAgent",
				arguments1);
		writerAgent.start();

		Object[] arguments2 = new Object[1];
		arguments2[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC1";
		AgentController scannerAgent1 = mainContainer.createNewAgent("scannerAgent1", "serkan.LocalScannerAgent",
				arguments2);
		scannerAgent1.start();

		Object[] arguments3 = new Object[1];
		arguments3[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC2";
		AgentController scannerAgent2 = mainContainer.createNewAgent("scannerAgent2", "serkan.LocalScannerAgent",
				arguments3);
		scannerAgent2.start();

		Object[] arguments4 = new Object[1];
		arguments4[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC3";
		AgentController scannerAgent3 = mainContainer.createNewAgent("scannerAgent3", "serkan.LocalScannerAgent",
				arguments4);
		scannerAgent3.start();

		Object[] arguments5 = new Object[1];
		arguments5[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC1";
		AgentController readerAgent = mainContainer.createNewAgent("readerAgent", "read.DocumentReaderAgent",
				arguments5);
		readerAgent.start();

		Object[] arguments6 = new Object[1];
		arguments6[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC1";
		AgentController rdrAgent1 = mainContainer.createNewAgent("rdrAgent1", "read.LocalReaderAgent", arguments6);
		rdrAgent1.start();

		Object[] arguments7 = new Object[1];
		arguments7[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC2";
		AgentController rdrAgent2 = mainContainer.createNewAgent("rdrAgent2", "read.LocalReaderAgent", arguments7);
		rdrAgent2.start();

		Object[] arguments8 = new Object[1];
		arguments8[0] = "C:/Users/Serkan/Desktop/AgentContainers/AC3";
		AgentController rdrAgent3 = mainContainer.createNewAgent("rdrAgent3", "read.LocalReaderAgent", arguments8);
		rdrAgent3.start();

	}

}
