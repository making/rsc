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
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.rsocket.metadata.CompositeMetadataFlyweight;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.ClientTransport;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import reactor.netty.tcp.TcpClient;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import static am.ik.rsocket.Transport.TCP;
import static am.ik.rsocket.Transport.WEBSOCKET;
import static java.util.stream.Collectors.toList;

public class Args {

	private final OptionParser parser = new OptionParser();

	private final OptionSpec<Void> version = parser.acceptsAll(Arrays.asList("v", "version"), "Print version");

	private final OptionSpec<Void> help = parser.acceptsAll(Arrays.asList("help"), "Print help");

	private final OptionSpec<Void> wiretap = parser.acceptsAll(Arrays.asList("w", "wiretap"), "Enable wiretap");

	private final OptionSpec<Void> debug = parser.acceptsAll(Arrays.asList("debug"), "Enable FrameLogger");

	private final OptionSpec<Void> quiet = parser.acceptsAll(Arrays.asList("q", "quiet"), "Disable the output on next");

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

	private final OptionSpec<Integer> resume = parser.acceptsAll(Arrays.asList("resume"),
			"Enable resume. Resume session duration can be configured in seconds. Unless the duration is specified, the default value (2min) is used.")
			.withOptionalArg().ofType(Integer.class);

	private final URI uri;

	private final OptionSpec<String> dataMimeType = parser
			.acceptsAll(Arrays.asList("dataMimeType", "dmt"), "MimeType for data").withOptionalArg()
			.defaultsTo(WellKnownMimeType.APPLICATION_JSON.getString());

	private final OptionSpec<String> metadataMimeType = parser
			.acceptsAll(Arrays.asList("metadataMimeType", "mmt"), "MimeType for metadata (default: text/plain)")
			.withOptionalArg();

	private final OptionSpec<String> data = parser
			.acceptsAll(Arrays.asList("d", "data"), "Data. Use '-' to read data from standard input.").withOptionalArg()
			.defaultsTo("");

	private final OptionSpec<String> metadata = parser
			.acceptsAll(Arrays.asList("m", "metadata"), "Metadata (default: )").withOptionalArg();

	private final OptionSpec<String> setup = parser.acceptsAll(Arrays.asList("s", "setup"), "Setup payload")
			.withOptionalArg();

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

	private final OptionSpec<Void> stacktrace = parser.acceptsAll(Arrays.asList("stacktrace"),
			"Show Stacktrace when an exception happens");

	private final OptionSpec<Void> showSystemProperties = parser.acceptsAll(Arrays.asList("show-system-properties"),
			"Show SystemProperties for troubleshoot");

	private final OptionSet options;

	private Tuple2<String, ByteBuf> composedMetadata = null;

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
		final int port = this.uri.getPort();
		if (port < 0) {
			if (secure()) {
				return 443;
			} else {
				return 80;
			}
		}
		return port;
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

	public ByteBuf data() {
		return Unpooled.wrappedBuffer(this.options.valueOf(this.data).getBytes(StandardCharsets.UTF_8));
	}

	public boolean readFromStdin() {
		return "-".equals(this.options.valueOf(this.data));
	}

	public String route() {
		return this.options.valueOf(this.route);
	}

	public String dataMimeType() {
		final String mimeType = this.options.valueOf(this.dataMimeType);
		try {
			return WellKnownMimeType.valueOf(mimeType).getString();
		} catch (IllegalArgumentException ignored) {
			return mimeType;
		}
	}

	public Optional<ByteBuf> setup() {
		if (this.options.has(this.setup) && this.options.valueOf(this.setup) != null) {
			return Optional
					.of(Unpooled.wrappedBuffer(this.options.valueOf(this.setup).getBytes(StandardCharsets.UTF_8)));
		} else {
			return Optional.empty();
		}
	}

	/**
	 * https://github.com/rsocket/rsocket/blob/master/Extensions/CompositeMetadata.md
	 */
	public Tuple2<String, ByteBuf> composeMetadata() {
		if (this.composedMetadata != null) {
			return this.composedMetadata;
		}
		final List<String> mimeTypeList = this.metadataMimeType();
		final List<ByteBuf> metadataList = this.metadata();
		if (metadataList.size() != mimeTypeList.size()) {
			throw new IllegalArgumentException(
					String.format("The size of metadata(%d) and metadataMimeType(%d) don't match!", metadataList.size(),
							mimeTypeList.size()));
		}
		if (metadataList.isEmpty()) {
			return Tuples.of(WellKnownMimeType.TEXT_PLAIN.getString(), Unpooled.buffer());
		}
		if (metadataList.size() == 1) {
			return Tuples.of(mimeTypeList.get(0), metadataList.get(0));
		}
		final CompositeByteBuf compositeByteBuf = Unpooled.compositeBuffer();
		final ByteBufAllocator allocator = ByteBufAllocator.DEFAULT;
		final Iterator<String> mimeTypeIterator = mimeTypeList.iterator();
		final Iterator<ByteBuf> metadataIterator = metadataList.iterator();
		while (mimeTypeIterator.hasNext()) {
			final String mimeType = mimeTypeIterator.next();
			final ByteBuf metadata = metadataIterator.next();
			final WellKnownMimeType wellKnownMimeType = WellKnownMimeType.fromString(mimeType);
			if (wellKnownMimeType != WellKnownMimeType.UNPARSEABLE_MIME_TYPE) {
				CompositeMetadataFlyweight.encodeAndAddMetadata(compositeByteBuf, allocator, wellKnownMimeType,
						metadata);
			} else {
				CompositeMetadataFlyweight.encodeAndAddMetadata(compositeByteBuf, allocator, mimeType, metadata);
			}
		}
		this.composedMetadata = Tuples.of(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString(),
				compositeByteBuf);
		return this.composedMetadata;
	}

	List<ByteBuf> metadata() {
		List<ByteBuf> list = new ArrayList<>();
		if (this.options.has(this.route)) {
			list.add(routingMetadata(this.route()));
		}
		list.addAll(this.options.valuesOf(this.metadata).stream()
				.map(metadata -> Unpooled.wrappedBuffer(metadata.getBytes(StandardCharsets.UTF_8))).collect(toList()));
		return list;
	}

	List<String> metadataMimeType() {
		List<String> list = new ArrayList<>();
		if (this.options.has(this.route)) {
			list.add(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString());
		}
		list.addAll(this.options.valuesOf(this.metadataMimeType).stream().map(mimeType -> {
			try {
				return WellKnownMimeType.valueOf(mimeType).getString();
			} catch (IllegalArgumentException ignored) {
				return mimeType;
			}
		}).collect(toList()));
		return list;
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

	public boolean quiet() {
		return this.options.has(this.quiet);
	}

	public boolean stacktrace() {
		return this.options.has(this.stacktrace);
	}

	public Optional<Duration> resume() {
		if (this.options.has(this.resume)) {
			final Integer duration = this.options.valueOf(this.resume);
			return Optional.of(duration == null ? Duration.ofMinutes(2) : Duration.ofSeconds(duration));
		} else {
			return Optional.empty();
		}
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

	public boolean showSystemProperties() {
		return this.options.has(this.showSystemProperties);
	}

	/**
	 * https://github.com/rsocket/rsocket/blob/master/Extensions/Routing.md
	 */
	static ByteBuf routingMetadata(String tag) {
		final byte[] bytes = tag.getBytes(StandardCharsets.UTF_8);
		final ByteBuf buf = Unpooled.buffer();
		buf.writeByte(bytes.length);
		buf.writeBytes(bytes);
		return buf;
	}
}
