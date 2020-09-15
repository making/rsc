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

import java.io.File;
import java.time.Duration;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;

import am.ik.rsocket.tracing.Reporter;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.rsocket.core.RSocketConnector;
import io.rsocket.core.Resume;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.metadata.TracingMetadataCodec.Flags;
import io.rsocket.transport.ClientTransport;
import org.slf4j.LoggerFactory;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

public class Rsc {

	public static void main(String[] a) throws Exception {
		final Args args = new Args(a);
		try {
			if (args.help()) {
				args.printHelp(System.out);
				return;
			}
			if (args.version()) {
				printVersion();
				return;
			}
			if (args.showSystemProperties()) {
				printSystemProperties();
				return;
			}
			if (args.resume().isPresent() && args.retry().isPresent()) {
				System.err.println("--resume and --retry are mutually exclusive.");
				System.err.println();
				System.exit(2);
			}
			if (!args.hasUri()) {
				System.err.println("Uri is required.");
				System.err.println();
				args.printHelp(System.out);
				System.exit(2);
			}
			if (args.secure() && System.getenv("JAVA_HOME") != null) {
				final File javaHome = new File(System.getenv("JAVA_HOME"));
				if (System.getProperty("java.library.path") == null
						|| System.getProperty("java.library.path").isEmpty()) {
					findFile(javaHome, file -> file.getName().startsWith("libsunec."))
							.map(f -> f.getParentFile().getAbsolutePath())
							.ifPresent(p -> System.setProperty("java.library.path", p));
				}
				if (System.getProperty("javax.net.ssl.trustStore") == null
						|| System.getProperty("javax.net.ssl.trustStore").isEmpty()) {
					findFile(javaHome, file -> file.getName().equals("cacerts"))
							.ifPresent(f -> System.setProperty("javax.net.ssl.trustStore", f.getAbsolutePath()));
				}
			}
			final long begin = System.nanoTime();
			run(args)
					.doOnTerminate(() -> handleSpan(args, (System.nanoTime() - begin) / 1000))
					.blockLast();
			if (args.interactionModel() == InteractionModel.FIRE_AND_FORGET) {
				// Workaround for https://github.com/making/rsc/issues/18
				Thread.sleep(10);
			}
		}
		catch (RuntimeException e) {
			if (args.stacktrace()) {
				e.printStackTrace();
			}
			else {
				System.err.println("Error: " + e.getMessage());
				System.err.println();
				System.err.println("Use --stacktrace option for details.");
			}
			System.exit(1);
		}
	}

	static Flux<?> run(Args args) {
		if (args.debug()) {
			configureDebugLevel("io.rsocket.FrameLogger");
		}
		args.log().ifPresent(Rsc::configureDebugLevel);
		final ClientTransport clientTransport = args.clientTransport();
		final RSocketConnector connector = RSocketConnector.create();
		final String metadataMimeType = args.composeMetadata().getT1();
		args.resume().ifPresent(duration -> connector.resume(new Resume()
				.sessionDuration(duration)
				.retry(Retry.fixedDelay(10, Duration.ofSeconds(5)))));
		args.retry().ifPresent(maxAttempts -> connector
				.reconnect(retry(maxAttempts)));
		args.setupPayload().ifPresent(connector::setupPayload);
		return connector
				.payloadDecoder(PayloadDecoder.ZERO_COPY)
				.metadataMimeType(metadataMimeType)
				.dataMimeType(args.dataMimeType())
				.connect(clientTransport)
				.flatMapMany(rsocket -> args.interactionModel().request(rsocket, args))
				.transform(s -> args.retry().map(maxAttempts -> s.retryWhen(retry(maxAttempts))).orElse(s));
	}

	static void handleSpan(Args args, long duration) {
		args.span()
				.ifPresent(span -> {
					if (args.printB3()) {
						System.err.println("b3=" + span.toB3SingleHeaderFormat());
					}
					args.trace()
							.filter(flags -> flags == Flags.DEBUG || flags == Flags.SAMPLE)
							.flatMap(flags -> args.zipkinUrl())
							.ifPresent(url -> Reporter.report(String.format("%s/api/v2/spans", url),
									span,
									args.interactionModel().name().toLowerCase().replace("_", "-"),
									duration));
				});
	}

	static Retry retry(int maxAttempts) {
		return Retry.fixedDelay(maxAttempts, Duration.ofSeconds(1))
				.filter(e -> !Exceptions.isRetryExhausted(e))
				.doBeforeRetry(System.err::println);
	}

	static void configureDebugLevel(String loggerName) {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		final Logger logger = loggerContext.getLogger(loggerName);
		logger.setLevel(Level.DEBUG);
	}

	static void printVersion() {
		// Version class will be generated during Maven's generated-sources phase
		System.out.println(Version.getVersionAsJson());
	}

	static void printSystemProperties() {
		new TreeMap<>(System.getProperties()).forEach((k, v) -> System.out.println(k + "\t= " + v));
	}

	static Optional<File> findFile(File dir, Predicate<File> predicate) {
		final File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					final Optional<File> found = findFile(file, predicate);
					if (found.isPresent()) {
						return found;
					}
				}
				if (predicate.test(file)) {
					return Optional.of(file);
				}
			}
		}
		return Optional.empty();
	}
}
