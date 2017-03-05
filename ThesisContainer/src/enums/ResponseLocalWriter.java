package enums;

public enum ResponseLocalWriter {
	ERROR("BAŞARILI", "Dosya yükelemede hata"),
	SUCCESS("BAŞARILI", "Dosya yükledi");

	ResponseLocalWriter(String code, String message) {
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
