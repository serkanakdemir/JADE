package Ontology;

import jade.content.Predicate;

public class Document implements Predicate {
	private String documenttype;
	private String documentname;
	private String remotedocumentpath;
	private String localdocumentpath;
	private Float localversion = 0F;
	private Float remoteversion = 0F;
	private String response = "";
	private byte[] byteArray;

	public String getDocumentname() {
		return documentname;
	}

	public void setDocumentname(String documentname) {
		this.documentname = documentname;
	}

	public String getRemotedocumentpath() {
		return remotedocumentpath;
	}

	public void setRemotedocumentpath(String remotedocumentpath) {
		this.remotedocumentpath = remotedocumentpath;
	}

	public String getLocaldocumentpath() {
		return localdocumentpath;
	}

	public void setLocaldocumentpath(String localdocumentpath) {
		this.localdocumentpath = localdocumentpath;
	}

	public Float getLocalversion() {
		return localversion;
	}

	public void setLocalversion(Float localversion) {
		this.localversion = localversion;
	}

	public Float getRemoteversion() {
		return remoteversion;
	}

	public void setRemoteversion(Float remoteversion) {
		this.remoteversion = remoteversion;
	}

	public String getDocumenttype() {
		return documenttype;
	}

	public void setDocumenttype(String documenttype) {
		this.documenttype = documenttype;
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}

	public byte[] getByteArray() {
		return byteArray;
	}

	public void setByteArray(byte[] byteArray) {
		this.byteArray = byteArray;
	}

}
