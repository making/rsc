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

import io.netty.buffer.ByteBuf;
import io.rsocket.metadata.TracingMetadataCodec.Flags;
import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.util.function.Tuple2;

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
		assertThat(metadata.getT2()).isEqualTo(Args.routingMetadata("locate.aircrafts.for"));
	}

	@Test
	void metadataDefault() {
		final Args args = new Args(new String[] { "tcp://localhost:8080" });
		final Tuple2<String, ByteBuf> metadata = args.composeMetadata();
		assertThat(metadata.getT1()).isEqualTo("text/plain");
		assertThat(metadata.getT2().toString(UTF_8)).isEqualTo("");
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
	void setupDataDeprecated() {
		final Args args = new Args("tcp://localhost:8080 -s hello");
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
		assertThat(args.setupPayload().get().getMetadataUtf8()).isEqualTo("hello");
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
		assertThat(args.setupPayload().get().getMetadataUtf8()).isEqualTo("meta");
	}

	@Test
	void setupMetadataMimeType() {
		final Args args = new Args("tcp://localhost:8080 --sd hello --sm {\"value\":\"foo\"} --smmt application/json");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("hello");
		assertThat(args.setupPayload().get().getMetadataUtf8()).isEqualTo("{\"value\":\"foo\"}");
	}

	@Test
	void setupMetadataMimeTypeEnum() {
		final Args args = new Args("tcp://localhost:8080 --sd hello --sm {\"value\":\"foo\"} --smmt APPLICATION_JSON");
		assertThat(args.setupPayload().isPresent()).isTrue();
		assertThat(args.setupPayload().get().getDataUtf8()).isEqualTo("hello");
		assertThat(args.setupPayload().get().getMetadataUtf8()).isEqualTo("{\"value\":\"foo\"}");
	}

	@Test
	void setupMetadataMimeTypeMissing() {
		final Args args = new Args("tcp://localhost:8080 --sm foo --smmt");
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
		assertThat(metadata.getT2().toString(UTF_8)).contains(Args.routingMetadata("greeting").toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList()))
				.containsExactly(Args.routingMetadata("greeting").toString(UTF_8), "{\"hello\":\"world\"}", "bar");
		assertThat(metadataMimeTypeList).containsExactly("message/x.rsocket.routing.v0", "application/json",
				"text/plain");
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
		assertThat(metadata.getT2().toString(UTF_8)).contains(Args.routingMetadata("greeting").toString(UTF_8));
		final List<ByteBuf> metadataList = args.metadata();
		final List<String> metadataMimeTypeList = args.metadataMimeType();
		assertThat(metadataList.stream().map(x -> x.toString(UTF_8)).collect(toList()))
				.containsExactly(Args.routingMetadata("greeting").toString(UTF_8), "{}");
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