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
package am.ik.rsocket.routing;

import java.nio.charset.StandardCharsets;

import am.ik.rsocket.MetadataEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * https://github.com/rsocket/rsocket/blob/master/Extensions/Routing.md
 */
public class Route implements MetadataEncoder {
	private final String value;

	public Route(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	@Override
	public ByteBuf toMetadata(ByteBufAllocator allocator) {
		final byte[] bytes = this.value.getBytes(StandardCharsets.UTF_8);
		final ByteBuf buf = allocator.buffer(bytes.length + 1);
		buf.writeByte(bytes.length);
		buf.writeBytes(bytes);
		return buf;
	}
}
