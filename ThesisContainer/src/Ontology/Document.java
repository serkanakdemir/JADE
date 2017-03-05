package Ontology;

import java.util.Date;

import jade.content.Predicate;

public class Document implements Predicate {
	private String documenttype;
	private String documentname;
	private String remotedocumentpath;
	private String localdocumentpath = "";
	private Long localversion = 0L;
	private Long remoteversion = 0L;
	private String response = "";
	private String owner = "";
	private String ownercontainer = "";
	private Date lastOwnTime;
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

	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public Date getLastOwnTime() {
		return lastOwnTime;
	}

	public void setLastOwnTime(Date lastOwnTime) {
		this.lastOwnTime = lastOwnTime;
	}

	public Long getLocalversion() {
		return localversion;
	}

	public void setLocalversion(Long localversion) {
		this.localversion = localversion;
	}

	public Long getRemoteversion() {
		return remoteversion;
	}

	public void setRemoteversion(Long remoteversion) {
		this.remoteversion = remoteversion;
	}

	public String getOwnercontainer() {
		return ownercontainer;
	}

	public void setOwnercontainer(String ownercontainer) {
		this.ownercontainer = ownercontainer;
	}

}
