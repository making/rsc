package am.ik.rsocket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import io.rsocket.metadata.TracingMetadataCodec;
import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;

public class CliCompletionHelpFormatter implements HelpFormatter {
	private final Map<String, Object[]> possibleValues = new HashMap<String, Object[]>() {
		{
			put("am.ik.rsocket.InteractionModel", InteractionModel.values());
			put("io.rsocket.metadata.TracingMetadataCodec$Flags", TracingMetadataCodec.Flags.values());
		}
	};

	@Override
	public String format(Map<String, ? extends OptionDescriptor> options) {
		Comparator<OptionDescriptor> comparator = Comparator.comparing(optionDescriptor -> optionDescriptor.options().iterator().next());
		Set<OptionDescriptor> sorted = new TreeSet<>(comparator);
		sorted.addAll(options.values());
		final StringBuilder sb = new StringBuilder();
		sb.append("name: rsc").append(System.lineSeparator());
		sb.append("binary_name: rsc").append(System.lineSeparator());
		sb.append("before_help: |").append(System.lineSeparator());
		sb.append("  usage: rsc Uri [Options]").append(System.lineSeparator());
		sb.append("  Non-option arguments: [String: Uri]").append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("args:").append(System.lineSeparator());
		sorted.forEach(descriptor -> {
			final List<String> opts = new ArrayList<>(descriptor.options());
			if (opts.contains("[arguments]")) {
				return;
			}
			Collections.sort(opts, Comparator.comparingInt(String::length).reversed());
			final String longest = opts.get(0);
			final String shortest = opts.get(opts.size() - 1);
			sb.append("- ").append(longest).append(":").append(System.lineSeparator());
			if (shortest.length() == 1) {
				sb.append("    short: ").append(shortest).append(System.lineSeparator());
				opts.remove(shortest);
			}
			sb.append("    long: ").append(longest).append(System.lineSeparator());
			if (opts.size() > 1) {
				sb.append("    aliases: ").append(opts.subList(1, opts.size())).append(System.lineSeparator());
			}
			sb.append("    required: ").append(descriptor.isRequired()).append(System.lineSeparator());
			sb.append("    about: \"").append(descriptor.description()).append("\"").append(System.lineSeparator());
			final String indicator = descriptor.argumentTypeIndicator();
			sb.append("    takes_value: ").append(!"".equals(indicator)).append(System.lineSeparator());
			if (possibleValues.containsKey(indicator)) {
				sb.append("    possible_values: ").append(Arrays.toString(possibleValues.get(indicator))).append(System.lineSeparator());
			}
		});
		return sb.toString();
	}
}
