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
package am.ik.rsocket.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import am.ik.rsocket.InteractionModel;
import io.rsocket.metadata.TracingMetadataCodec;
import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;

public class CliCompletionHelpFormatter implements HelpFormatter {
	private final Map<String, Object[]> possibleValues = new HashMap<String, Object[]>() {
		{
			put("am.ik.rsocket.InteractionModel", InteractionModel.values());
			put("am.ik.rsocket.completion.ShellType", ShellType.values());
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
