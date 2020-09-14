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

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import am.ik.rsocket.security.AuthenticationSetupMetadata;
import am.ik.rsocket.security.BasicAuthentication;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.rsocket.metadata.WellKnownMimeType;

enum SetupMetadataMimeType {
	TEXT_PLAIN(MimeType.wellKnown(WellKnownMimeType.TEXT_PLAIN)),
	APPLICATION_JSON(MimeType.wellKnown(WellKnownMimeType.APPLICATION_JSON)),
	MESSAGE_RSOCKET_AUTHENTICATION(MimeType.wellKnown(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION)) {
		@Override
		public ByteBuf encode(String metadata) {
			return AuthenticationSetupMetadata.valueOf(metadata).toMetadata(new PooledByteBufAllocator(true));
		}
	},
	AUTHENTICATION_BASIC(MimeType.custom("message/x.rsocket.authentication.basic.v0")) {
		@Override
		public ByteBuf encode(String metadata) {
			return BasicAuthentication.valueOf(metadata).toMetadata(new PooledByteBufAllocator(true));
		}
	};

	private final MimeType mimeType;

	SetupMetadataMimeType(MimeType mimeType) {
		this.mimeType = mimeType;
	}

	public String getValue() {
		if (this.mimeType.isWellKnown()) {
			return this.mimeType.wellKnownMimeType.getString();
		}
		else {
			return this.mimeType.custom;
		}
	}

	public ByteBuf encode(String metadata) {
		return Unpooled.wrappedBuffer(metadata.getBytes(StandardCharsets.UTF_8));
	}

	public static SetupMetadataMimeType of(String value) {
		for (SetupMetadataMimeType type : values()) {
			if (Objects.equals(type.name(), value)) {
				return type;
			}
			if (type.mimeType.isWellKnown()) {
				if (Objects.equals(type.mimeType.wellKnownMimeType.getString(), value)) {
					return type;
				}
			}
			else {
				if (Objects.equals(type.mimeType.custom, value)) {
					return type;
				}
			}
		}
		throw new IllegalArgumentException("'" + value + "' is unsupported as a SetupMetadataMimeType.");
	}

	private static class MimeType {
		private final WellKnownMimeType wellKnownMimeType;

		private final String custom;

		static MimeType wellKnown(WellKnownMimeType wellKnownMimeType) {
			return new MimeType(wellKnownMimeType, null);
		}

		static MimeType custom(String custom) {
			return new MimeType(null, custom);
		}

		private MimeType(WellKnownMimeType wellKnownMimeType, String custom) {
			this.wellKnownMimeType = wellKnownMimeType;
			this.custom = custom;
		}

		boolean isWellKnown() {
			return this.wellKnownMimeType != null;
		}
	}
}