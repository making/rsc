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
			return WebsocketClientTransport.create(HttpClient.from(args.tcpClient()), args.path());
		}
	};

	abstract ClientTransport clientTransport(Args args);
}
