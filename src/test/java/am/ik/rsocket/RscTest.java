package am.ik.rsocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
public class RscTest {

	@Test
	void test(CapturedOutput capture) throws Exception {
		Rsc.main(new String[]{});
		assertThat(capture.toString()).isNotEmpty();
	}
}
