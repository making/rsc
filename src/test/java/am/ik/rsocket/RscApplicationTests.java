package am.ik.rsocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class RscApplicationTests {

	@Test
	void help(CapturedOutput capture) throws Exception {
		RscApplication.main(new String[] { "-h" });
		assertThat(capture.toString()).isNotEmpty();
	}

	@Test
	void helpYamlMode(CapturedOutput capture) throws Exception {
		RscApplication.main(new String[] { "-h", "cli-completion" });
		assertThat(capture.toString()).isNotEmpty();
	}
}
