/*
 * Copyright (C) 2020 Toshiaki Maki <makingx@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package am.ik.rsocket;

import java.util.Objects;

import io.rsocket.metadata.WellKnownMimeType;

enum SetupMetadataMimeType {
	TEXT_PLAIN(WellKnownMimeType.TEXT_PLAIN),
	APPLICATION_JSON(WellKnownMimeType.APPLICATION_JSON),
	MESSAGE_RSOCKET_AUTHENTICATION(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION);

	private final WellKnownMimeType value;

	SetupMetadataMimeType(WellKnownMimeType value) {
		this.value = value;
	}

	public WellKnownMimeType getValue() {
		return value;
	}

	public static SetupMetadataMimeType of(String value) {
		for (SetupMetadataMimeType type : values()) {
			if (Objects.equals(type.value.getString(), value) ||
					Objects.equals(type.value.name(), value)) {
				return type;
			}
		}
		throw new IllegalArgumentException("'" + value + "' is unsupported as a SetupMetadataMimeType.");
	}
}