package Ontology;

import jade.content.Predicate;

public class ContainerInformationsWrapper implements Predicate {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1735985745116611966L;
	private String filechangemode = "dummy";
	private ContainerInformations containerInformations = new ContainerInformations();

	public ContainerInformationsWrapper(String fileChangeMode) {
		this.setFilechangemode(fileChangeMode);
	}

	public ContainerInformationsWrapper() {
		// TODO Auto-generated constructor stub
	}

	public ContainerInformations getContainerInformations() {
		return containerInformations;
	}

	public void setContainerInformations(ContainerInformations containerInformations) {
		this.containerInformations = containerInformations;
	}

	public String getFilechangemode() {
		return filechangemode;
	}

	public void setFilechangemode(String filechangemode) {
		this.filechangemode = filechangemode;
	}

}
