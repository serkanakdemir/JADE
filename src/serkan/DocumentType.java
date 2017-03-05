package serkan;

public enum DocumentType {
	ADD("ADD"), UPDATE("UPDATE"), DELETE("DELETE");
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
