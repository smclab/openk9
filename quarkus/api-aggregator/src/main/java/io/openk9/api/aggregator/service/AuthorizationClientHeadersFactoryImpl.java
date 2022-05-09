/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.api.aggregator.service;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.ext.ClientHeadersFactory;
import org.eclipse.microprofile.rest.client.ext.DefaultClientHeadersFactoryImpl;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AuthorizationClientHeadersFactoryImpl
	implements ClientHeadersFactory {

	public final static String PROPAGATE_PROPERTY = "org.eclipse.microprofile.rest.client.propagateHeaders.auth.client";
	private final static String CLASS_NAME = DefaultClientHeadersFactoryImpl.class.getName();
	private final static Logger LOG = Logger.getLogger(CLASS_NAME);

	private static Optional<Config> config() {
		try {
			return Optional.ofNullable(ConfigProvider.getConfig());
		}
		catch (ExceptionInInitializerError | NoClassDefFoundError | IllegalStateException ex) {
			// expected if no MP Config implementation is available
			return Optional.empty();
		}
	}

	private static Optional<String> getHeadersProperty() {
		Optional<Config> c = config();
		if (c.isPresent()) {
			return Optional.ofNullable(c.get().getOptionalValue(PROPAGATE_PROPERTY, String.class).orElse(null));
		}
		return Optional.empty();
	}

	@Override
	public MultivaluedMap<String, String> update(MultivaluedMap<String, String> incomingHeaders,
												 MultivaluedMap<String, String> clientOutgoingHeaders) {

		if (LOG.isLoggable(Level.FINER)) {
			LOG.entering(CLASS_NAME, "update", new Object[]{incomingHeaders, clientOutgoingHeaders});
		}
		MultivaluedMap<String, String> propagatedHeaders = new MultivaluedHashMap<>();
		Optional<String> propagateHeaderString = getHeadersProperty();
		if (propagateHeaderString.isPresent()) {
			Arrays.stream(propagateHeaderString.get().split(","))
				.forEach( header -> {
					if (incomingHeaders.containsKey(header)) {
						propagatedHeaders.put(header, incomingHeaders.get(header));
					}
				});
		}
		if (LOG.isLoggable(Level.FINER)) {
			LOG.exiting(CLASS_NAME, "update", propagatedHeaders);
		}
		return propagatedHeaders;
	}

}
