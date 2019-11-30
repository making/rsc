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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.resume.PeriodicResumeStrategy;
import io.rsocket.transport.ClientTransport;
import io.rsocket.util.DefaultPayload;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

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
			if (!args.hasUri()) {
				System.err.println("Uri is required.");
				System.err.println();
				args.printHelp(System.out);
				return;
			}
			if (args.secure() && System.getenv("JAVA_HOME") != null) {
				findLibsunec(new File(System.getenv("JAVA_HOME") + "/jre/lib"))
						.map(f -> f.getParentFile().getAbsolutePath())
						.ifPresent(p -> System.setProperty("java.library.path", p));
			}
			run(args).blockLast();
		} catch (RuntimeException e) {
			if (args.stacktrace()) {
				e.printStackTrace();
			} else {
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
		final RSocketFactory.ClientRSocketFactory factory = RSocketFactory.connect();
		args.resume().ifPresent(duration -> factory.resume().resumeSessionDuration(duration)
				.resumeStrategy(() -> new PeriodicResumeStrategy(Duration.ofSeconds(5))));
		args.setup().map(DefaultPayload::create).ifPresent(factory::setupPayload);
		return factory //
				.frameDecoder(PayloadDecoder.ZERO_COPY) //
				.metadataMimeType(args.composeMetadata().getT1()) //
				.dataMimeType(args.dataMimeType()) //
				.transport(clientTransport) //
				.start() //
				.flatMapMany(rsocket -> args.interactionModel().request(rsocket, args));
	}

	static void configureDebugLevel(String loggerName) {
		final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		final Logger logger = loggerContext.getLogger(loggerName);
		logger.setLevel(Level.DEBUG);
	}

	static void printVersion() {
		// Version class will be generated during Maven's generated-sources phase
		System.out.println(Version.getVersion());
	}

	static Optional<File> findLibsunec(File dir) {
		final File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isDirectory()) {
					final Optional<File> found = findLibsunec(file);
					if (found.isPresent()) {
						return found;
					}
				}
				if (file.getName().startsWith("libsunec")) {
					return Optional.of(file);
				}
			}
		}
		return Optional.empty();
	}
}
