package am.ik.rsocket;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.ClientTransport;
import joptsimple.OptionException;
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
			run(args).blockLast();
		} catch (OptionException | IllegalArgumentException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}

	static Flux<?> run(Args args) {
		if (args.debug()) {
			LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			Logger rootLogger = loggerContext.getLogger("io.rsocket.FrameLogger");
			rootLogger.setLevel(Level.DEBUG);
		}
		final ClientTransport clientTransport = args.clientTransport();
		return RSocketFactory.connect() //
				.frameDecoder(PayloadDecoder.ZERO_COPY) //
				.metadataMimeType(args.metadataMimeType()) //
				.dataMimeType(args.dataMimeType()) //
				.transport(clientTransport) //
				.start() //
				.flatMapMany(rsocket -> args.interactionModel().request(rsocket, args));
	}

	static void printVersion() {
		// Version class will be generated during Maven's generated-sources phase
		System.out.println(Version.getVersion());
	}
}
