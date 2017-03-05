package enums;

public enum ResponseOwnerShip {
	ALREADY_OWNED("Sahibi olan dosya", "Dosyanın zaten sahibi var"), UNKNOWN_ERROR("BİLİNMEYEN HATA",
			"Bilinmeyen Hata"), SUCCESS("BAŞARILI", "Dosya sahiplenilmeye uygun");

	ResponseOwnerShip(String code, String message) {
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
