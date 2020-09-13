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

import am.ik.rsocket.MetadataEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.metadata.AuthMetadataCodec;

public class BearerAuthentication implements MetadataEncoder {
	private final String token;

	public BearerAuthentication(String token) {
		this.token = token;
	}

	@Override
	public ByteBuf toMetadata(ByteBufAllocator allocator) {
		return AuthMetadataCodec.encodeBearerMetadata(allocator, this.token.toCharArray());
	}
}
