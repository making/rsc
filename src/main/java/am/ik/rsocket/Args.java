/*
 * Copyright (C) 2019 Toshiaki Maki <makingx@gmail.com>
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

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.ClientTransport;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import reactor.netty.tcp.TcpClient;

import static am.ik.rsocket.Transport.TCP;
import static am.ik.rsocket.Transport.WEBSOCKET;

public class Args {

	private final OptionParser parser = new OptionParser();

	private final OptionSpec<Void> version = parser.acceptsAll(Arrays.asList("v", "version"), "Print version");

	private final OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("help"), "Print help");

	private final OptionSpec<Void> wiretap = parser.acceptsAll(Arrays.asList("w", "wiretap"), "Enable wiretap");

	private final OptionSpec<Void> debug = parser.acceptsAll(Arrays.asList("debug"), "Enable FrameLogger");

	private final OptionSpec<InteractionModel> interactionModel = parser
			.acceptsAll(Arrays.asList("im", "interactionModel"), "InteractionModel").withOptionalArg()
			.ofType(InteractionModel.class).defaultsTo(InteractionModel.REQUEST_RESPONSE);

	private final OptionSpec<Void> stream = parser.acceptsAll(Arrays.asList("stream"),
			"Shortcut of --im REQUEST_STREAM");

	private final OptionSpec<Void> request = parser.acceptsAll(Arrays.asList("request"),
			"Shortcut of --im REQUEST_RESPONSE");

	private final OptionSpec<Void> fnf = parser.acceptsAll(Arrays.asList("fnf"), "Shortcut of --im FIRE_AND_FORGET");

	private final OptionSpec<Void> channel = parser.acceptsAll(Arrays.asList("channel"),
			"Shortcut of --im REQUEST_CHANNEL");

	private final URI uri;

	private final OptionSpec<String> dataMimeType = parser
			.acceptsAll(Arrays.asList("dataMimeType"), "MimeType for data").withOptionalArg()
			.defaultsTo(WellKnownMimeType.APPLICATION_JSON.getString());

	private final OptionSpec<String> metadataMimeType = parser
			.acceptsAll(Arrays.asList("metadataMimeType"), "MimeType for metadata").withOptionalArg()
			.defaultsTo(WellKnownMimeType.TEXT_PLAIN.getString());

	private final OptionSpec<String> data = parser.acceptsAll(Arrays.asList("d", "data"), "Data").withOptionalArg()
			.defaultsTo("");

	private final OptionSpec<String> metadata = parser.acceptsAll(Arrays.asList("m", "metadata"), "Metadata")
			.withOptionalArg().defaultsTo("");

	private final OptionSpec<String> route = parser
			.acceptsAll(Arrays.asList("route", "r"), "Routing Metadata Extension").withOptionalArg();

	private final OptionSpec<String> log = parser.acceptsAll(Arrays.asList("log"), "Enable log()").withOptionalArg();

	private final OptionSpec<Integer> limitRate = parser
			.acceptsAll(Arrays.asList("limitRate"), "Enable limitRate(rate)").withOptionalArg().ofType(Integer.class);

	private final OptionSpec<Integer> take = parser.acceptsAll(Arrays.asList("take"), "Enable take(n)")
			.withOptionalArg().ofType(Integer.class);

	private final OptionSpec<Long> delayElements = parser
			.acceptsAll(Arrays.asList("delayElements"), "Enable delayElements(delay) in milli seconds")
			.withOptionalArg().ofType(Long.class);

	private final OptionSet options;

	public Args(String[] args) {
		final OptionSpec<String> uri = parser.nonOptions().describedAs("Uri");
		this.options = parser.parse(args);
		this.uri = Optional.ofNullable(uri.value(this.options)).map(URI::create).orElse(null);
	}

	public boolean hasUri() {
		return this.uri != null;
	}

	public String host() {
		return this.uri.getHost();
	}

	public int port() {
		return this.uri.getPort();
	}

	public boolean secure() {
		final String scheme = this.uri.getScheme();
		return scheme.endsWith("+tls") || scheme.equals("wss");
	}

	public String path() {
		return this.uri.getPath();
	}

	public InteractionModel interactionModel() {
		if (this.options.has(this.stream)) {
			return InteractionModel.REQUEST_STREAM;
		}
		if (this.options.has(this.request)) {
			return InteractionModel.REQUEST_RESPONSE;
		}
		if (this.options.has(this.channel)) {
			return InteractionModel.REQUEST_CHANNEL;
		}
		if (this.options.has(this.fnf)) {
			return InteractionModel.FIRE_AND_FORGET;
		}
		return this.options.valueOf(this.interactionModel);
	}

	public ByteBuffer data() {
		return ByteBuffer.wrap(this.options.valueOf(this.data).getBytes(StandardCharsets.UTF_8));
	}

	public String route() {
		return this.options.valueOf(this.route);
	}

	public ByteBuffer metadata() {
		if (this.options.has(this.route)) {
			// TODO composite-metadata
			return routingMetadata(this.route());
		}
		return ByteBuffer.wrap(this.options.valueOf(this.metadata).getBytes(StandardCharsets.UTF_8));
	}

	public String dataMimeType() {
		final String mimeType = this.options.valueOf(this.dataMimeType);
		try {
			return WellKnownMimeType.valueOf(mimeType).getString();
		} catch (IllegalArgumentException ignored) {
			return mimeType;
		}
	}

	public String metadataMimeType() {
		if (this.options.has(this.route)) {
			// TODO composite-metadata
			return WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString();
		}
		final String mimeType = this.options.valueOf(this.metadataMimeType);
		try {
			return WellKnownMimeType.valueOf(mimeType).getString();
		} catch (IllegalArgumentException ignored) {
			return mimeType;
		}
	}

	public ClientTransport clientTransport() {
		final String scheme = this.uri.getScheme();
		Transport transport;
		if (scheme.startsWith("ws")) {
			transport = WEBSOCKET;
		} else if (scheme.startsWith("tcp")) {
			transport = TCP;
		} else {
			throw new IllegalArgumentException(scheme + " is unsupported scheme.");
		}
		return transport.clientTransport(this);
	}

	public TcpClient tcpClient() {
		final TcpClient tcpClient = TcpClient.create().host(this.host()).port(this.port()).wiretap(this.wiretap());
		if (this.secure()) {
			return tcpClient.secure();
		}
		return tcpClient;
	}

	public boolean wiretap() {
		return this.options.has(this.wiretap);
	}

	public boolean debug() {
		return this.options.has(this.debug);
	}

	public Optional<Integer> limitRate() {
		if (this.options.has(this.limitRate)) {
			return Optional.ofNullable(this.options.valueOf(this.limitRate));
		} else {
			return Optional.empty();
		}
	}

	public Optional<Integer> take() {
		if (this.options.has(this.take)) {
			return Optional.ofNullable(this.options.valueOf(this.take));
		} else {
			return Optional.empty();
		}
	}

	public Optional<Duration> delayElements() {
		if (this.options.has(this.delayElements)) {
			return Optional.of(Duration.ofMillis(this.options.valueOf(this.delayElements)));
		} else {
			return Optional.empty();
		}
	}

	public Optional<String> log() {
		if (this.options.has(this.log)) {
			return Optional.of(Objects.toString(this.options.valueOf(this.log), "rsc"));
		} else {
			return Optional.empty();
		}
	}

	public boolean help() {
		return this.options.has(this.help);
	}

	public boolean version() {
		return this.options.has(this.version);
	}

	public void printHelp(PrintStream stream) {
		try {
			stream.println("usage: rsc Uri [Options]");
			stream.println();
			this.parser.printHelpOn(stream);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	/**
	 * https://github.com/rsocket/rsocket/blob/master/Extensions/Routing.md
	 */
	static ByteBuffer routingMetadata(String tag) {
		final byte[] bytes = tag.getBytes(StandardCharsets.UTF_8);
		final ByteBuffer buffer = ByteBuffer.allocate(1 + bytes.length);
		buffer.put((byte) bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
}
