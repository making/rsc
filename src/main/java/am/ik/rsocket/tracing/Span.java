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
package am.ik.rsocket.tracing;

import java.time.Instant;

import am.ik.rsocket.MetadataEncoder;
import am.ik.rsocket.Version;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.metadata.TracingMetadataCodec;
import io.rsocket.metadata.TracingMetadataCodec.Flags;

public class Span implements MetadataEncoder {
	private final long spanId;

	private final long traceIdHigh;

	private final long traceId;

	private final Flags flags;

	private final long timestamp;

	public Span(long spanId, long traceIdHigh, long traceId, Flags flags) {
		this.spanId = spanId;
		this.traceIdHigh = traceIdHigh;
		this.traceId = traceId;
		this.flags = flags;
		Instant instant = java.time.Clock.systemUTC().instant();
		this.timestamp = (instant.getEpochSecond() * 1000000) + (instant.getNano() / 1000);
	}

	public String toJsonString(String rsocketMethod, long duration) {
		return String.format("{\n"
						+ "    \"id\": \"%s\",\n"
						+ "    \"traceId\": \"%s%s\",\n"
						+ "    \"name\": \"%s\",\n"
						+ "    \"timestamp\": %d,\n"
						+ "    \"duration\": %d,\n"
						+ "    \"kind\": \"CLIENT\",\n"
						+ "    \"localEndpoint\": {\n"
						+ "      \"serviceName\": \"rsc\"\n"
						+ "    },\n"
						+ "    \"tags\": {\n"
						+ "      \"rsocket.method\": \"%s\",\n"
						+ "      \"rsc.version\": \"%s\",\n"
						+ "      \"rsc.build\": \"%s\",\n"
						+ "      \"rsocket-java.version\": \"%s\"\n"
						+ "    }\n"
						+ "  }",
				Long.toHexString(spanId),
				Long.toHexString(traceIdHigh),
				Long.toHexString(traceId),
				rsocketMethod,
				this.timestamp,
				duration,
				rsocketMethod,
				Version.getVersion(),
				Version.getBuild(),
				Version.getRSocketJava()
		);
	}

	public String toB3SingleHeaderFormat() {
		final String b3 = String.format("%s%s-%s",
				Long.toHexString(this.traceIdHigh),
				Long.toHexString(this.traceId),
				Long.toHexString(this.spanId));
		if (this.flags == Flags.DEBUG) {
			return b3 + "-d";
		}
		else if (this.flags == Flags.SAMPLE) {
			return b3 + "-1";
		}
		else if (this.flags == Flags.NOT_SAMPLE) {
			return b3 + "-0";
		}
		else {
			return b3;
		}
	}

	@Override
	public ByteBuf toMetadata(ByteBufAllocator allocator) {
		return TracingMetadataCodec.encode128(allocator, this.traceIdHigh, this.traceId, this.spanId, this.flags);
	}
}
