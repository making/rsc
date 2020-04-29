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
import reactor.netty.http.client.HttpClient;

public enum Transport {

	TCP {
		@Override
		ClientTransport clientTransport(Args args) {
			return TcpClientTransport.create(args.tcpClient());
		}
	},
	WEBSOCKET {
		@Override
		ClientTransport clientTransport(Args args) {
			WebsocketClientTransport transport =  WebsocketClientTransport.create(HttpClient.from(args.tcpClient()), args.path());
			transport.setTransportHeaders(args.headers());
			return transport;
		}
	};

	abstract ClientTransport clientTransport(Args args);
}
