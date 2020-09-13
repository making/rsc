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
package am.ik.rsocket.security;

import java.nio.charset.StandardCharsets;

import am.ik.rsocket.MetadataEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class BasicAuthentication implements MetadataEncoder {
	private final String username;

	private final String password;

	public static BasicAuthentication valueOf(String value) {
		final String[] split = value.split(":", 2);
		if (split.length != 2) {
			throw new IllegalArgumentException("The format of Basic Authentication must be 'username:password'.");
		}
		return new BasicAuthentication(split[0], split[1]);
	}

	public BasicAuthentication(String username, String password) {
		this.username = username;
		this.password = password;
	}

	@Override
	public ByteBuf toMetadata(ByteBufAllocator allocator) {
		final ByteBuf buf = allocator.buffer();
		final byte[] username = this.username.getBytes(StandardCharsets.UTF_8);
		buf.writeInt(username.length);
		buf.writeBytes(username);
		buf.writeBytes(this.password.getBytes(StandardCharsets.UTF_8));
		return buf;
	}
}
