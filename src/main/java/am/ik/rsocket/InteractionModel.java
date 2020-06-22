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

import java.util.Scanner;

import io.netty.buffer.Unpooled;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.util.DefaultPayload;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import static java.nio.charset.StandardCharsets.UTF_8;

public enum InteractionModel {
	REQUEST_RESPONSE {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			final Mono<Payload> payload = payloadMono(args);
			return payload.flatMap(p -> rsocket.requestResponse(p).map(Payload::getDataUtf8) //
					.transform(s -> args.log().map(s::log).orElse(s))
					.transform(s -> args.quiet() ? s : s.doOnNext(System.out::println)));
		}
	},
	REQUEST_STREAM {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			final Mono<Payload> payload = payloadMono(args);
			return payload.flatMapMany(p -> rsocket.requestStream(p).map(Payload::getDataUtf8)
					.transform(s -> args.log().map(s::log).orElse(s))
					.transform(s -> args.limitRate().map(s::limitRate).orElse(s))
					.transform(s -> args.take().map(s::take).orElse(s))
					.transform(s -> args.delayElements().map(s::delayElements).orElse(s))
					.transform(s -> args.quiet() ? s : s.doOnNext(System.out::println)));
		}
	},
	REQUEST_CHANNEL {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			final Flux<Payload> payloads = payloadFlux(args);
			return rsocket.requestChannel(payloads).map(Payload::getDataUtf8)
					.transform(s -> args.log().map(s::log).orElse(s))
					.transform(s -> args.limitRate().map(s::limitRate).orElse(s))
					.transform(s -> args.take().map(s::take).orElse(s))
					.transform(s -> args.delayElements().map(s::delayElements).orElse(s))
					.transform(s -> args.quiet() ? s : s.doOnNext(System.out::println));
		}
	},
	FIRE_AND_FORGET {
		@Override
		Publisher<?> request(RSocket rsocket, Args args) {
			final Mono<Payload> payload = payloadMono(args);
			return payload.flatMap(p -> rsocket.fireAndForget(p).transform(s -> args.log().map(s::log).orElse(s)));
		}
	};

	abstract Publisher<?> request(RSocket rsocket, Args args);

	static Flux<Payload> payloadFlux(Args args) {
		if (args.readFromStdin()) {
			final Scanner scanner = new Scanner(System.in);
			return scanToFlux(scanner) //
					.transform(s -> args.log().map(__ -> s.log("input")).orElse(s)) //
					.map(x -> DefaultPayload.create(Unpooled.wrappedBuffer(x.getBytes(UTF_8)),
							args.composeMetadata().getT2()))
					.doOnTerminate(scanner::close);
		} else {
			return Flux.just(DefaultPayload.create(args.data(), args.composeMetadata().getT2()));
		}
	}

	static Mono<Payload> payloadMono(Args args) {
		if (args.readFromStdin()) {
			final Scanner scanner = new Scanner(System.in);
			return scanToMono(scanner) //
					.transform(s -> args.log().map(__ -> s.log("input")).orElse(s)) //
					.map(x -> DefaultPayload.create(Unpooled.wrappedBuffer(x.getBytes(UTF_8)),
							args.composeMetadata().getT2()))
					.doOnTerminate(scanner::close);
		} else {
			return Mono.just(DefaultPayload.create(args.data(), args.composeMetadata().getT2()));
		}
	}

	static Flux<String> scanToFlux(Scanner scanner) {
		return Flux.<String>generate(sink -> {
			if (!scanner.hasNext()) {
				sink.complete();
			} else {
				sink.next(scanner.nextLine());
			}
		}).subscribeOn(Schedulers.boundedElastic());
	}

	static Mono<String> scanToMono(Scanner scanner) {
		return scanToFlux(scanner) //
				.collectList() //
				.map(list -> String.join(System.lineSeparator(), list));
	}
}
