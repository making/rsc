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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import am.ik.rsocket.routing.Route;
import am.ik.rsocket.security.BasicAuthentication;
import am.ik.rsocket.security.BearerAuthentication;
import am.ik.rsocket.security.SimpleAuthentication;
import am.ik.rsocket.tracing.Span;
import am.ik.rsocket.tracing.Tracing;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.metadata.CompositeMetadataCodec;
import io.rsocket.metadata.TracingMetadataCodec.Flags;
import io.rsocket.metadata.WellKnownMimeType;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.DefaultPayload;
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
			"Enable resume. Resume session duration can be configured in seconds.")
			.withOptionalArg().ofType(Integer.class);

	private final OptionSpec<Integer> retry = parser.acceptsAll(Arrays.asList("retry"), "Enable retry. Retry every 1 second with the given max attempts.")
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

	private final OptionSpec<String> setupData = parser.acceptsAll(Arrays.asList("setupData", "sd"), "Data for Setup payload")
			.withOptionalArg();

	private final OptionSpec<String> setupDataDeprecated = parser.acceptsAll(Arrays.asList("s", "setup"), "[DEPRECATED] Data for Setup payload. Use --setupData or --sd instead.")
			.withOptionalArg();

	private final OptionSpec<String> setupMetadata = parser.acceptsAll(Arrays.asList("sm", "setupMetadata"), "Metadata for Setup payload")
			.withOptionalArg();

	private final OptionSpec<String> setupMetadataMimeType = parser.acceptsAll(Arrays.asList("smmt", "setupMetadataMimeType"), "Metadata MimeType for Setup payload.")
			.withOptionalArg();

	private final OptionSpec<String> route = parser
			.acceptsAll(Arrays.asList("route", "r"), "Enable Routing Metadata Extension").withOptionalArg();

	private final OptionSpec<String> authSimple = parser
			.acceptsAll(Arrays.asList("authSimple", "as", "u"), "Enable Authentication Metadata Extension (Simple). The format must be 'username:password'.").withOptionalArg();

	private final OptionSpec<String> authBearer = parser
			.acceptsAll(Arrays.asList("authBearer", "ab"), "Enable Authentication Metadata Extension (Bearer).").withOptionalArg();

	private final OptionSpec<String> authBasic = parser
			.acceptsAll(Arrays.asList("authBasic"), "[DEPRECATED] Enable Authentication Metadata Extension (Basic). This Metadata exist only for the backward compatibility with Spring Security 5.2").withOptionalArg();

	private final OptionSpec<Flags> trace = parser
			.acceptsAll(Arrays.asList("trace"), "Enable Tracing (Zipkin) Metadata Extension. Unless sampling state (UNDECIDED, NOT_SAMPLE, SAMPLE, DEBUG) is specified, DEBUG is used by default.")
			.withOptionalArg().ofType(Flags.class);

	private final OptionSpec<String> zipkinUrl = parser
			.acceptsAll(Arrays.asList("zipkinUrl"), "Zipkin URL to send a span (ex. http://localhost:9411). Ignored unless --trace is set.").withOptionalArg();

	private final OptionSpec<Void> printB3 = parser.acceptsAll(Arrays.asList("printB3"), "Print B3 propagation info. Ignored unless --trace is set.");

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

	private final OptionSpec<Void> showSystemProperties = parser.acceptsAll(Arrays.asList("showSystemProperties"),
			"Show SystemProperties for troubleshoot");

	private final OptionSpec<Void> showSystemPropertiesDeprecated = parser.acceptsAll(Arrays.asList("show-system-properties"),
			"[DEPRECATED] Show SystemProperties for troubleshoot. Use --showSystemProperties instead.");

	private final OptionSpec<String> wsHeader = parser.acceptsAll(Arrays.asList("wsh", "wsHeader"), "Header for web socket connection")
			.withOptionalArg();


	private final OptionSet options;

	private Tuple2<String, ByteBuf> composedMetadata = null;

	private Span span;

	public Args(String[] args) {
		final OptionSpec<String> uri = parser.nonOptions().describedAs("Uri");
		this.options = parser.parse(args);
		this.uri = Optional.ofNullable(uri.value(this.options)).map(URI::create).orElse(null);
	}

	public Args(String args) {
		this(args.split("\\s"));
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
			}
			else {
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

	public Route route() {
		final String route = this.options.valueOf(this.route);
		if (route == null) {
			throw new IllegalArgumentException("'route' is not specified.");
		}
		return new Route(route);
	}

	public SimpleAuthentication authSimple() {
		final String authSimple = this.options.valueOf(this.authSimple);
		if (authSimple == null) {
			throw new IllegalArgumentException("'authSimple' is not specified.");
		}
		return SimpleAuthentication.valueOf(authSimple);
	}

	public BearerAuthentication authBearer() {
		final String authBearer = this.options.valueOf(this.authBearer);
		if (authBearer == null) {
			throw new IllegalArgumentException("'authBearer' is not specified.");
		}
		return new BearerAuthentication(authBearer);
	}

	public BasicAuthentication authBasic() {
		final String authBasic = this.options.valueOf(this.authBasic);
		if (authBasic == null) {
			throw new IllegalArgumentException("'authBasic' is not specified.");
		}
		return BasicAuthentication.valueOf(authBasic);
	}

	public String dataMimeType() {
		final String mimeType = this.options.valueOf(this.dataMimeType);
		try {
			return WellKnownMimeType.valueOf(mimeType).getString();
		}
		catch (IllegalArgumentException ignored) {
			return mimeType;
		}
	}

	private Optional<ByteBuf> setupData() {
		final OptionSpec<String> setupData;
		if (this.options.has(this.setupDataDeprecated)) {
			System.err.println("[WARNING] --setup / -s option is deprecated. Use --setupData / --sd instead.");
			setupData = this.setupDataDeprecated;
		}
		else {
			setupData = this.setupData;
		}

		if (this.options.has(setupData)) {
			final String data = this.options.valueOf(setupData);
			if (data == null) {
				throw new IllegalArgumentException("'setupData' is not specified.");
			}
			return Optional
					.of(Unpooled.wrappedBuffer(data.getBytes(StandardCharsets.UTF_8)));
		}
		else {
			return Optional.empty();
		}
	}

	private Optional<ByteBuf> setupMetadata() {
		if (this.options.has(this.setupMetadata)) {
			final String metadata = this.options.valueOf(this.setupMetadata);
			if (metadata == null) {
				throw new IllegalArgumentException("'setupMetadata' is not specified.");
			}
			if (this.options.has(this.setupMetadataMimeType)) {
				final String mimeType = this.options.valueOf(this.setupMetadataMimeType);
				if (mimeType == null) {
					throw new IllegalArgumentException("'setupMetadataMimeType' is not specified.");
				}
				return Optional.of(SetupMetadataMimeType.of(mimeType).encode(metadata));
			}
			return Optional.of(SetupMetadataMimeType.TEXT_PLAIN.encode(metadata));
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Payload> setupPayload() {
		final Optional<Payload> payload = this.setupData()
				.map(data -> this.setupMetadata()
						.map(metadata -> DefaultPayload.create(data, metadata))
						.orElseGet(() -> DefaultPayload.create(data)));
		if (payload.isPresent()) {
			return payload;
		}
		else {
			return this.setupMetadata()
					.map(metadata -> DefaultPayload.create(Unpooled.EMPTY_BUFFER, metadata));
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
		final ByteBufAllocator allocator = new PooledByteBufAllocator(true);
		final Iterator<String> mimeTypeIterator = mimeTypeList.iterator();
		final Iterator<ByteBuf> metadataIterator = metadataList.iterator();
		while (mimeTypeIterator.hasNext()) {
			final String mimeType = mimeTypeIterator.next();
			final ByteBuf metadata = metadataIterator.next();
			final WellKnownMimeType wellKnownMimeType = WellKnownMimeType.fromString(mimeType);
			if (wellKnownMimeType != WellKnownMimeType.UNPARSEABLE_MIME_TYPE) {
				CompositeMetadataCodec.encodeAndAddMetadata(compositeByteBuf, allocator, wellKnownMimeType,
						metadata);
			}
			else {
				CompositeMetadataCodec.encodeAndAddMetadata(compositeByteBuf, allocator, mimeType, metadata);
			}
		}
		this.composedMetadata = Tuples.of(WellKnownMimeType.MESSAGE_RSOCKET_COMPOSITE_METADATA.getString(),
				compositeByteBuf);
		return this.composedMetadata;
	}

	public Optional<Span> span() {
		return Optional.ofNullable(this.span);
	}

	List<ByteBuf> metadata() {
		final List<ByteBuf> list = new ArrayList<>();
		final List<MetadataEncoder> metadataEncoders = new ArrayList<>();
		if (this.options.has(this.route)) {
			metadataEncoders.add(this.route());
		}
		if (this.options.has(this.authSimple)) {
			metadataEncoders.add(this.authSimple());
		}
		if (this.options.has(this.authBearer)) {
			metadataEncoders.add(this.authBearer());
		}
		if (this.options.has(this.authBasic)) {
			metadataEncoders.add(this.authBasic());
		}
		if (this.options.has(this.trace)) {
			final Flags flags = Optional.ofNullable(this.options.valueOf(this.trace)).orElse(Flags.DEBUG);
			this.span = Tracing.createSpan(flags);
			metadataEncoders.add(this.span);
		}
		metadataEncoders.forEach(metadataEncoder -> list.add(metadataEncoder.toMetadata(new PooledByteBufAllocator(true))));
		list.addAll(this.options.valuesOf(this.metadata).stream()
				.map(metadata -> Unpooled.wrappedBuffer(metadata.getBytes(StandardCharsets.UTF_8))).collect(toList()));
		return list;
	}

	List<String> metadataMimeType() {
		List<String> list = new ArrayList<>();
		if (this.options.has(this.route)) {
			list.add(WellKnownMimeType.MESSAGE_RSOCKET_ROUTING.getString());
		}
		if (this.options.has(this.authSimple) || this.options.has(this.authBearer)) {
			list.add(WellKnownMimeType.MESSAGE_RSOCKET_AUTHENTICATION.getString());
		}
		if (this.options.has(this.authBasic)) {
			list.add("message/x.rsocket.authentication.basic.v0");
		}
		if (this.options.has(this.trace)) {
			list.add(WellKnownMimeType.MESSAGE_RSOCKET_TRACING_ZIPKIN.getString());
		}
		list.addAll(this.options.valuesOf(this.metadataMimeType).stream().map(mimeType -> {
			try {
				return WellKnownMimeType.valueOf(mimeType).getString();
			}
			catch (IllegalArgumentException ignored) {
				return mimeType;
			}
		}).collect(toList()));
		return list;
	}

	Map<String, String> wsHeaders() {
		Map<String, Set<String>> headerSet = new LinkedHashMap<>();
		this.options.valuesOf(wsHeader).forEach(header -> {
			String[] nameValue = header.split(":", 2);
			if (nameValue.length == 2) {
				headerSet.computeIfAbsent(nameValue[0], k -> new LinkedHashSet<>()).add(nameValue[1]);
			}
		});

		Map<String, String> headers = new LinkedHashMap<>();
		headerSet.forEach((key, value) -> headers.put(key, String.join(";", value)));
		return headers;
	}


	public ClientTransport clientTransport() {
		final String scheme = this.uri.getScheme();
		Transport transport;
		if (scheme.startsWith("ws")) {
			transport = WEBSOCKET;
		}
		else if (scheme.startsWith("tcp")) {
			transport = TCP;
		}
		else {
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

	public Optional<Flags> trace() {
		if (this.options.has(this.trace)) {
			final Flags flags = this.options.valueOf(this.trace);
			return Optional.of(flags == null ? Flags.DEBUG : flags);
		}
		else {
			return Optional.empty();
		}
	}

	public boolean printB3() {
		return this.options.has(this.printB3);
	}

	public Optional<String> zipkinUrl() {
		if (this.options.has(this.zipkinUrl)) {
			final List<String> zipkinUrl = this.options.valuesOf(this.zipkinUrl);
			if (zipkinUrl == null) {
				throw new IllegalArgumentException("'zipkinUrl' is not specified.");
			}
			return Optional.of(this.options.valueOf(this.zipkinUrl));
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Duration> resume() {
		if (this.options.has(this.resume)) {
			final Integer resume = this.options.valueOf(this.resume);
			if (resume == null) {
				throw new IllegalArgumentException("'resume' is not specified.");
			}
			return Optional.of(resume).map(Duration::ofSeconds);
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Integer> retry() {
		if (this.options.has(this.retry)) {
			final Integer retry = this.options.valueOf(this.retry);
			if (retry == null) {
				throw new IllegalArgumentException("'retry' is not specified.");
			}
			return Optional.of(retry);
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Integer> limitRate() {
		if (this.options.has(this.limitRate)) {
			final Integer limitRate = this.options.valueOf(this.limitRate);
			if (limitRate == null) {
				throw new IllegalArgumentException("'limitRate' is not specified.");
			}
			return Optional.of(limitRate);
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Integer> take() {
		if (this.options.has(this.take)) {
			final Integer take = this.options.valueOf(this.take);
			if (take == null) {
				throw new IllegalArgumentException("'take' is not specified.");
			}
			return Optional.of(take);
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<Duration> delayElements() {
		if (this.options.has(this.delayElements)) {
			final Long delayElements = this.options.valueOf(this.delayElements);
			if (delayElements == null) {
				throw new IllegalArgumentException("'delayElements' is not specified.");
			}
			return Optional.of(delayElements).map(Duration::ofMillis);
		}
		else {
			return Optional.empty();
		}
	}

	public Optional<String> log() {
		if (this.options.has(this.log)) {
			return Optional.of(Objects.toString(this.options.valueOf(this.log), "rsc"));
		}
		else {
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
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	public boolean showSystemProperties() {
		if (this.options.has(this.showSystemPropertiesDeprecated)) {
			System.err.println("[WARNING] --show-system-properties option is deprecated. Use --showSystemProperties instead.");
			return true;
		}
		return this.options.has(this.showSystemProperties);
	}
}
