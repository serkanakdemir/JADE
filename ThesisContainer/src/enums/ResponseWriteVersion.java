package enums;

public enum ResponseWriteVersion {
	MISSING_VERSION("VERSIYON EKSİK", "Güncelleme için versiyon gerekli"),
	ALREADY_EXISTS("VAROLAN DOSYA", "Zaten varolan dosya ekleme"), 
	UNKNOWN_ERROR("BİLİNMEYEN HATA", "Bilinmeyen Hata"), 
	OLD_VERSION("VERSIYON_HATA","Güncel olmayan versiyon"),
	OWNED_BY_ANOTHER_USER("VERSIYON_HATA","Başka kullanıcı tutuyor"),
	SUCCESS("BAŞARILI", "Dosya yüklemeye uygun");

	ResponseWriteVersion(String code, String message) {
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
