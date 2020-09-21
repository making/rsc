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
package am.ik.rsocket.tracing;

import java.util.Random;

import io.rsocket.metadata.TracingMetadataCodec.Flags;


public final class Tracing {
	public static Span createSpan(Flags flags) {
		final long spanId = randomLong();
		final long traceIdHigh = nextTraceIdHigh();
		final long traceId = spanId;
		return new Span(spanId, traceIdHigh, traceId, flags);
	}

	/**
	 * Copied from brave.internal.Platform
	 */
	private static long randomLong() {
		long nextId;
		do {
			nextId = new Random(System.nanoTime()).nextLong();
		} while (nextId == 0L);
		return nextId;
	}

	private static long nextTraceIdHigh() {
		return nextTraceIdHigh(new Random(System.nanoTime()).nextInt());
	}

	private static long nextTraceIdHigh(int random) {
		long epochSeconds = System.currentTimeMillis() / 1000;
		return (epochSeconds & 0xffffffffL) << 32
				| (random & 0xffffffffL);
	}
}
