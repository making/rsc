/*
 * Copyright 2013-2020 The OpenZipkin Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package am.ik.rsocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.rsocket.metadata.TracingMetadataCodec;
import io.rsocket.metadata.TracingMetadataCodec.Flags;


public class Tracing {
	public static ByteBuf zipkinMetadata(Flags flags, boolean printB3) {
		final long spanId = randomLong();
		final long traceIdHigh = nextTraceIdHigh();
		final long traceId = spanId;
		if (printB3) {
			System.err.println("b3=" + b3SingleHeaderFormat(traceIdHigh, traceId, spanId, flags));
		}
		return TracingMetadataCodec.encode128(ByteBufAllocator.DEFAULT, traceIdHigh, traceId, spanId, flags);
	}

	private static String b3SingleHeaderFormat(long traceIdHigh, long traceId, long spanId, Flags flags) {
		final String b3 = String.format("%s%s-%s", Long.toHexString(traceIdHigh), Long.toHexString(traceId), Long.toHexString(spanId));
		if (flags == Flags.DEBUG) {
			return b3 + "-d";
		}
		else if (flags == Flags.SAMPLE) {
			return b3 + "-1";
		}
		else if (flags == Flags.NOT_SAMPLE) {
			return b3 + "-0";
		}
		else {
			return b3;
		}
	}

	/**
	 * Copied from brave.internal.Platform
	 */
	private static long randomLong() {
		long nextId;
		do {
			nextId = java.util.concurrent.ThreadLocalRandom.current().nextLong();
		} while (nextId == 0L);
		return nextId;
	}

	private static long nextTraceIdHigh() {
		return nextTraceIdHigh(java.util.concurrent.ThreadLocalRandom.current().nextInt());
	}

	private static long nextTraceIdHigh(int random) {
		long epochSeconds = System.currentTimeMillis() / 1000;
		return (epochSeconds & 0xffffffffL) << 32
				| (random & 0xffffffffL);
	}
}
