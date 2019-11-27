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

import io.rsocket.transport.ClientTransport;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.client.WebsocketClientTransport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArgsTest {

	@Test
	void clientTransportTcp() {
		final Args args = new Args(new String[]{"tcp://localhost:8080"});
		final ClientTransport clientTransport = args.clientTransport();
		assertThat(clientTransport).isOfAnyClassIn(TcpClientTransport.class);
	}

	@Test
	void clientTransportWebsocket() {
		final Args args = new Args(new String[]{"ws://localhost:8080"});
		final ClientTransport clientTransport = args.clientTransport();
		assertThat(clientTransport).isOfAnyClassIn(WebsocketClientTransport.class);
	}

	@Test
	void clientTransportHttp() {
		final Args args = new Args(new String[]{"http://localhost:8080"});
		final IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
				args::clientTransport);
		assertThat(exception.getMessage()).isEqualTo("http is unsupported scheme.");
	}

	@Test
	void port() {
		final Args args = new Args(new String[]{"ws://localhost:8080"});
		assertThat(args.port()).isEqualTo(8080);
	}

	@Test
	void portNoSecureDefault() {
		final Args args = new Args(new String[]{"ws://localhost"});
		assertThat(args.port()).isEqualTo(80);
	}

	@Test
	void portSecureDefault() {
		final Args args = new Args(new String[]{"wss://localhost"});
		assertThat(args.port()).isEqualTo(443);
	}
}