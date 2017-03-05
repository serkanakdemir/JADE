package enums;

public enum DocumentType {
	SHOW("SHOW"),
	ADD("ADD"),
	UPDATE("UPDATE"),
	DELETE("DELETE"),
	COPY("COPY"),
	OWNERSHIP("OWNERSHIP"),
	OWNERSHIP_RELEASE("OWNERSHIP_RELEASE");
	DocumentType(String code) {
		this.setCode(code);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	private String code;
}
