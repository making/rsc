package am.ik.rsocket;

import java.util.Arrays;
import java.util.Scanner;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class InteractionModelTest {

	@Test
	void scanToFlux() {
		final Scanner scanner = new Scanner(String.join(System.lineSeparator(), Arrays.asList("hello", "world")));
		final Flux<String> flux = InteractionModel.scanToFlux(scanner);
		StepVerifier.create(flux) //
				.expectNext("hello") //
				.expectNext("world") //
				.expectComplete() //
				.verify();
	}

	@Test
	void scanToMono() {
		final Scanner scanner = new Scanner(String.join(System.lineSeparator(), Arrays.asList("hello", "world")));
		final Mono<String> mono = InteractionModel.scanToMono(scanner);
		StepVerifier.create(mono) //
				.expectNext("hello" + System.lineSeparator() + "world") //
				.expectComplete() //
				.verify();
	}
}