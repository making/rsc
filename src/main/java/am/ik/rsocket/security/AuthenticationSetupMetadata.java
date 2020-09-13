/*
 * Copyright (C) 2020 Toshiaki Maki <makingx@gmail.com>
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
package am.ik.rsocket.security;

import am.ik.rsocket.MetadataEncoder;

public class AuthenticationSetupMetadata {
	public static MetadataEncoder valueOf(String text) {
		final String[] split = text.split(":", 2);
		if (split.length != 2) {
			throw new IllegalArgumentException("The format of Authentication Metadata in SETUP metadata must be 'type:value'. For example, 'simple:user:password' or 'bearer:token'.");
		}
		final String type = split[0].trim();
		final String value = split[1].trim();
		if ("simple".equalsIgnoreCase(type)) {
			return SimpleAuthentication.valueOf(value);
		}
		else if ("bearer".equalsIgnoreCase(type)) {
			return new BearerAuthentication(value);
		}
		else {
			throw new IllegalArgumentException("'" + type + "' is not supported as an Authentication Metadata type.");
		}
	}
}
