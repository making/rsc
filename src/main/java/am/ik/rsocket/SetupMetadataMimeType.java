package am.ik.rsocket;

import java.util.Objects;

enum SetupMetadataMimeType {
	TEXT_PLAIN("text/plain"),
	APPLICATION_JSON("application/json");

	private final String value;

	SetupMetadataMimeType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public static SetupMetadataMimeType of(String value) {
		for (SetupMetadataMimeType type : values()) {
			if (Objects.equals(type.value, value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("'" + value + "' is unsupported as a SetupMetadataMimeType.");
	}
}