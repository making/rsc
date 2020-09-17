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

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import am.ik.rsocket.routing.Route;
import am.ik.rsocket.security.BasicAuthentication;
import am.ik.rsocket.security.BearerAuthentication;
import am.ik.rsocket.security.SimpleAuthentication;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.rsocket.metadata.TracingMetadataCodec.Flags;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;

import static am.ik.rsocket.Args.DEFAULT_METADATA_MIME_TYPE;
import static am.ik.rsocket.Args.addCompositeMetadata;
import static am.ik.rsocket.SetupMetadataMimeType.AUTHENTICATION_BASIC;
import static am.ik.rsocket.SetupMetadataMimeType.MESSAGE_RSOCKET_AUTHENTICATION;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgsTest {

	@Test
	void clientTransportTcp() {
		final Args args = new Args(new String[] { "tcp://localhost:8080" });
		final ClientTransport clientTransport = args.clientTransport();
		assertThat(clientTransport).isOfAnyClassIn(TcpClientTransport.class);
	}

	@Test
	void clientTransportWebsocket() {
		final Args args = new Args(new String[] { "ws://localhost:8080" });
		final ClientTransport clientTransport = args.clientTransport();
		assertThat(clientTransport).isOfAnyClassIn(WebsocketClientTransport.class);
	}

	@Test
	void clientTransportHttp() {
		final Args args = new Args(new String[] { "http://localhost:8080" });
		final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
				args::clientTransport);
		assertThat(exception.getMessage()).isEqualTo("http is unsupported scheme.");
	}

	@Test
	void port() {
		final Args args = new Args(new String[] { "ws://localhost:8080" });
		assertThat(args.port()).isEqualTo(8080);
	}

	@Test
	void portNoSecureDefault() {
		final Args args = new Args(new String[] { "ws://localhost" });
		assertThat(args.port()).isEqualTo(80);
	}

	@Test
	void portSecureDefault() {
		final Args args = new Args(new String[] { "wss://localhost" });
		assertThat(args.port()).isEqualTo(443);
	}

	@Test
	void resumeDisabled() {
		final Args args = new Args(new String[] { "tcp://localhost:8080" });
		assertThat(args.resume().isPresent()).isFalse();
	}

	@Test
	void resumeEnabledWithoutValue() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", "--resume" });
		assertThatThrownBy(args::resume).isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void resumeEnabledWithDuration() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", "--resume", "600" });
		assertThat(args.resume().isPresent()).isTrue();
		assertThat(args.resume().get()).isEqualTo(Duration.ofSeconds(600));
	}

	@Test
	void route() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", "-r", "locate.aircrafts.for" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.routing.v0");
		assertThat(metadata.getT2()).isEqualTo(new Route("locate.aircrafts.for").toMetadata(ByteBufAllocator.DEFAULT));
	}

	@Test
	void metadataDefault() {
		final Args args = new Args(new String[] { "tcp://localhost:8080" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo(DEFAULT_METADATA_MIME_TYPE);
		assertThat(metadata.getT2().toString(UTF_8)).isEqualTo("");
	}

	@Test
	void metadata() {
		final Args args = new Args("tcp://localhost:8080 -m {\"foo\":\"bar\"}");
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo(DEFAULT_METADATA_MIME_TYPE);
		assertThat(metadata.getT2().toString(UTF_8)).isEqualTo("{\"foo\":\"bar\"}");
	}

	@Test
	void metadataSingle() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", "--metadataMimeType",
				"application/vnd.spring.rsocket.metadata+json", "-m", "{\"route\":\"locate.aircrafts.for\"}" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("application/vnd.spring.rsocket.metadata+json");
		assertThat(metadata.getT2().toString(UTF_8)).isEqualTo("{\"route\":\"locate.aircrafts.for\"}");
	}

	@Test
	void setupData() {
		final Args args = new Args("tcp://localhost:8080 --sd hello");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("hello");
		assertThat(args.setupPayload().get().getMetadataUtf8()).isEqualTo("");
	}

	@Test
	void setupDataMissing() {
		final Args args = new Args("tcp://localhost:8080 --sd");
		assertThatThrownBy(args::setupPayload)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void setupMetaData() {
		final Args args = new Args("tcp://localhost:8080 --sm hello");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("");
		final ByteBuf setupMetadata = addCompositeMetadata(Unpooled.wrappedBuffer("hello".getBytes()), DEFAULT_METADATA_MIME_TYPE);
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void setupMetadataMissing() {
		final Args args = new Args("tcp://localhost:8080 --sm");
		assertThatThrownBy(args::setupPayload)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void setupDataAndMetadata() {
		final Args args = new Args("tcp://localhost:8080 --sd hello --sm meta");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("hello");
		final ByteBuf setupMetadata = addCompositeMetadata(Unpooled.wrappedBuffer("meta".getBytes()), DEFAULT_METADATA_MIME_TYPE);
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void setupMetadataMimeType() {
		final Args args = new Args("tcp://localhost:8080 --sd hello --sm {\"value\":\"foo\"} --smmt application/json");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("hello");
		final ByteBuf setupMetadata = addCompositeMetadata(Unpooled.wrappedBuffer("{\"value\":\"foo\"}".getBytes()), "application/json");
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void setupMetadataMimeTypeEnum() {
		final Args args = new Args("tcp://localhost:8080 --sd hello --sm {\"value\":\"foo\"} --smmt APPLICATION_JSON");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("hello");
		final ByteBuf setupMetadata = addCompositeMetadata(Unpooled.wrappedBuffer("{\"value\":\"foo\"}".getBytes()), "application/json");
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void setupMetadataMimeTypeMissing() {
		final Args args = new Args("tcp://localhost:8080 --sm foo --smmt");
		assertThatThrownBy(args::setupPayload)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void setupMetadataAuthSimple() {
		final Args args = new Args("tcp://localhost:8080 --sm simple:user:pass --smmt MESSAGE_RSOCKET_AUTHENTICATION");
		assertThat(args.setupPayload().isPresent()).isTrue();
		final ByteBuf setupMetadata = addCompositeMetadata(new SimpleAuthentication("user", "pass").toMetadata(ByteBufAllocator.DEFAULT),
				MESSAGE_RSOCKET_AUTHENTICATION.getValue());
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void setupMetadataAuthBearer() {
		final Args args = new Args("tcp://localhost:8080 --sm bearer:token --smmt MESSAGE_RSOCKET_AUTHENTICATION");
		assertThat(args.setupPayload().isPresent()).isTrue();
		final ByteBuf setupMetadata = addCompositeMetadata(new BearerAuthentication("token").toMetadata(ByteBufAllocator.DEFAULT),
				MESSAGE_RSOCKET_AUTHENTICATION.getValue());
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void setupMetadataAuthUnknown() {
		final Args args = new Args("tcp://localhost:8080 --sm foo:token --smmt MESSAGE_RSOCKET_AUTHENTICATION");
		assertThatThrownBy(args::setupPayload)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void setupMetadataAuthIllegal() {
		final Args args = new Args("tcp://localhost:8080 --sm foo --smmt MESSAGE_RSOCKET_AUTHENTICATION");
		assertThatThrownBy(args::setupPayload)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void setupMetadataMimeTypeIllegal() {
		final Args args = new Args("tcp://localhost:8080 --sd hello --sm {\"value\":\"foo\"} --smmt application/foo");
		assertThatThrownBy(args::setupPayload)
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void setupMetadataBasicAuth() {
		final Args args = new Args("tcp://localhost:8080 --sm user:pass --smmt AUTHENTICATION_BASIC");
		assertThat(args.setupPayload().isPresent()).isTrue();
		final ByteBuf setupMetadata = addCompositeMetadata(new BasicAuthentication("user", "pass").toMetadata(ByteBufAllocator.DEFAULT),
				AUTHENTICATION_BASIC.getValue());
		assertThat(args.setupPayload().get().getMetadata()).isEqualTo(setupMetadata.nioBuffer());
	}

	@Test
	void metadataAuthSimple() {
		final Args args = new Args("tcp://localhost:8080 --authSimple user:password");
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.authentication.v0");
		assertThat(metadata.getT2()).isEqualTo(new SimpleAuthentication("user", "password").toMetadata(ByteBufAllocator.DEFAULT));
	}

	@Test
	void metadataAuthBearer() {
		final Args args = new Args("tcp://localhost:8080 --authBearer TOKEN");
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.authentication.v0");
		assertThat(metadata.getT2()).isEqualTo(new BearerAuthentication("TOKEN").toMetadata(ByteBufAllocator.DEFAULT));
	}

	@Test
	void metadataComposite() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", //
				"--metadataMimeType", "application/json", //
				"-m", "{\"hello\":\"world\"}", //
				"--metadataMimeType", "text/plain", //
				"-m", "bar" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.composite-metadata.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("application/json");
		assertThat(metadata.getT2().toString(UTF_8)).contains("{\"hello\":\"world\"}");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("text/plain");
		assertThat(metadata.getT2().toString(UTF_8)).contains("bar");
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList()))
				.containsExactly("{\"hello\":\"world\"}", "bar");
		assertThat(metadataMimeTypeList).containsExactly("application/json", "text/plain");
	}

	@Test
	void metadataCompositeWithRoute() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", //
				"--metadataMimeType", "application/json", //
				"-m", "{\"hello\":\"world\"}", //
				"--metadataMimeType", "text/plain", //
				"-m", "bar", //
				"--route", "greeting" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.composite-metadata.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("application/json");
		assertThat(metadata.getT2().toString(UTF_8)).contains("{\"hello\":\"world\"}");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("text/plain");
		assertThat(metadata.getT2().toString(UTF_8)).contains("bar");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.routing.v0");
		assertThat(metadata.getT2().toString(UTF_8)).contains(new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT).toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList()))
				.containsExactly(new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT).toString(UTF_8), "{\"hello\":\"world\"}", "bar");
		assertThat(metadataMimeTypeList).containsExactly("message/x.rsocket.routing.v0", "application/json",
				"text/plain");
	}

	@Test
	void metadataCompositeRouteAndAuthSimple() {
		final Args args = new Args("tcp://localhost:8080 -r greeting -u user:demo");
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		final ByteBuf authMetadata = SimpleAuthentication.valueOf("user:demo").toMetadata(ByteBufAllocator.DEFAULT);
		final ByteBuf routingMetadata = new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT);
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.composite-metadata.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.routing.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.authentication.v0");
		assertThat(metadata.getT2().toString(UTF_8)).contains(routingMetadata.toString(UTF_8));
		assertThat(metadata.getT2().toString(UTF_8)).contains(authMetadata.toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList())).containsExactly(routingMetadata.toString(UTF_8), authMetadata.toString(UTF_8));
		assertThat(metadataMimeTypeList).containsExactly("message/x.rsocket.routing.v0", "message/x.rsocket.authentication.v0");
	}

	@Test
	void metadataCompositeRouteAndAuthBearer() {
		final Args args = new Args("tcp://localhost:8080 -r greeting --ab TOKEN");
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		final ByteBuf authMetadata = new BearerAuthentication("TOKEN").toMetadata(ByteBufAllocator.DEFAULT);
		final ByteBuf routingMetadata = new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT);
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.composite-metadata.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.routing.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.authentication.v0");
		assertThat(metadata.getT2().toString(UTF_8)).contains(routingMetadata.toString(UTF_8));
		assertThat(metadata.getT2().toString(UTF_8)).contains(authMetadata.toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList())).containsExactly(routingMetadata.toString(UTF_8), authMetadata.toString(UTF_8));
		assertThat(metadataMimeTypeList).containsExactly("message/x.rsocket.routing.v0", "message/x.rsocket.authentication.v0");
	}

	@Test
	void metadataCompositeRouteAndAuthBasic() {
		final Args args = new Args("tcp://localhost:8080 -r greeting --authBasic user:demo");
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		final ByteBuf authMetadata = BasicAuthentication.valueOf("user:demo").toMetadata(ByteBufAllocator.DEFAULT);
		final ByteBuf routingMetadata = new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT);
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.composite-metadata.v0");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.routing.v0");
		assertThat(metadata.getT2().toString(UTF_8)).contains(routingMetadata.toString(UTF_8));
		assertThat(metadata.getT2().toString(UTF_8)).contains(authMetadata.toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList())).containsExactly(routingMetadata.toString(UTF_8), authMetadata.toString(UTF_8));
		assertThat(metadataMimeTypeList).containsExactly("message/x.rsocket.routing.v0", "message/x.rsocket.authentication.basic.v0");
	}

	@Test
	void metadataCompositeWithUnknownMimeType() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", //
				"--metadataMimeType", "application/vnd.spring.rsocket.metadata+json", //
				"-m", "{}", //
				"--route", "greeting" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("message/x.rsocket.composite-metadata.v0");
		// Unknown mime type should present as a US-ASCII string
		assertThat(metadata.getT2().toString(UTF_8)).contains("application/vnd.spring.rsocket.metadata+json");
		assertThat(metadata.getT2().toString(UTF_8)).contains("{}");
		assertThat(metadata.getT2().toString(UTF_8)).doesNotContain("message/x.rsocket.routing.v0");
		assertThat(metadata.getT2().toString(UTF_8)).contains(new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT).toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList()))
				.containsExactly(new Route("greeting").toMetadata(ByteBufAllocator.DEFAULT).toString(UTF_8), "{}");
		assertThat(metadataMimeTypeList).containsExactly("message/x.rsocket.routing.v0",
				"application/vnd.spring.rsocket.metadata+json");
	}


	@Test
	void traceDefault() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", "--trace" });
		final Optional<Flags> trace = args.trace();
		assertThat(trace.isPresent()).isTrue();
		assertThat(trace.get()).isEqualTo(Flags.DEBUG);
	}

	@Test
	void traceSample() {
		final Args args = new Args(new String[] { "tcp://localhost:8080", "--trace", "SAMPLE" });
		final Optional<Flags> trace = args.trace();
		assertThat(trace.isPresent()).isTrue();
		assertThat(trace.get()).isEqualTo(Flags.SAMPLE);
	}
}