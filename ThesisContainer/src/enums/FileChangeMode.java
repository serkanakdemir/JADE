package enums;

public enum FileChangeMode {
	ONLY_LOCAL_NODE("ONLY_LOCAL_NODE","Sadece bulunan node'u güncelle"),
	ALL_NODES("ALL_NODES","Tüm nodeları güncelle");

	FileChangeMode(String code, String message) {
		this.setCode(code);
		this.setMessage(message);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	private String code;
	private String message;
}
