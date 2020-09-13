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

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class Reporter {
	public static void report(String url, Span span, String rsocketName, long duratin) {
		try {
			final String content = "[" + span.toJsonString(rsocketName, duratin) + "]";
			final HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", String.valueOf(content.getBytes(StandardCharsets.UTF_8).length));
			conn.setDoOutput(true);
			try (final DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
				wr.writeBytes(content);
				wr.flush();
			}
			final int responseCode = conn.getResponseCode();
			if (responseCode != 202) {
				System.err.printf("!! Response Code: %d\n", responseCode);
			}
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
