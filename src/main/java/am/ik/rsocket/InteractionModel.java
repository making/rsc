package am.ik.rsocket;

import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

public enum InteractionModel {
	REQUEST_RESPONSE {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			return rsocket.requestResponse(DefaultPayload.create(args.data(), args.metadata()))
					.map(Payload::getDataUtf8).transform(s -> args.log().map(s::log).orElse(s));
		}
	},
	REQUEST_STREAM {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			return rsocket.requestStream(DefaultPayload.create(args.data(), args.metadata())).map(Payload::getDataUtf8)
					.transform(s -> args.log().map(s::log).orElse(s))
					.transform(s -> args.limitRate().map(s::limitRate).orElse(s))
					.transform(s -> args.take().map(s::take).orElse(s))
					.transform(s -> args.delayElements().map(s::delayElements).orElse(s));
		}
	},
	REQUEST_CHANNEL, FIRE_AND_FORGET {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			return rsocket.fireAndForget(DefaultPayload.create(args.data(), args.metadata()))
					.transform(s -> args.log().map(s::log).orElse(s));
		}
	};

	Publisher<?> request(RSocket rsocket, Args args) {
		return Mono.error(new IllegalArgumentException(name() + " is not supported."));
	}
}
