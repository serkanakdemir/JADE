package enums;

public enum ResponseReadVersion {
	NO_SUCH_FILE("DOSYA BULUNAMADI", "Dosya bulunamadı"),
	ALREADY_EXISTS("VAROLAN DOSYA", "Zaten varolan dosya ekleme"), 
	UNKNOWN_ERROR("BİLİNMEYEN HATA", "Bilinmeyen Hata"), 
	OLD_VERSION("VERSIYON_HATA","Güncel olmayan versiyon"), 
	SUCCESS("BAŞARILI", "Dosya yüklemeye uygun");

	ResponseReadVersion(String code, String message) {
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
