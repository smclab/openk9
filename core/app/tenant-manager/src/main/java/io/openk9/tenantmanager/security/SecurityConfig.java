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

package io.openk9.tenantmanager.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@ApplicationScoped
public class SecurityConfig {

	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BASIC_PREFIX = "basic ";
	private static final int PREFIX_LENGTH = BASIC_PREFIX.length();
	private static final Logger log = Logger.getLogger(SecurityConfig.class);

	@ConfigProperty(name = "io.openk9.tenantmanager.security.admin.password")
	String adminPasswordString;
	private byte[] adminPassword;

	@PostConstruct
	void init() {
		log.info("Set up SecurityConfig, requests must be authenticated via Basic auth");
		this.adminPassword = adminPasswordString.getBytes(StandardCharsets.UTF_8);
	}

	/**
	 * A dummy filter that verify HTTP Basic Authentication on any request without
	 * relying on Quarkus Security, because right now we don't need to rely on a
	 * more sophisticated implementation (like Jpa, OAuth2, Elytron, etc.).
	 * We just need to verify that the admin password provided in the request
	 * equals to the adminPassword configured for this application through an
	 * environment variable (or a secret in a vault).
	 *
	 * @param rc the context of the current request.
	 */
	@RouteFilter(Integer.MAX_VALUE)
	void authFilter(RoutingContext rc) {
		HttpServerRequest request = rc.request();

		String path = request.path();
		if (!path.startsWith("/api")) {
			rc.next();
			return;
		}

		// check the authorization header and scheme for the request
		String authorization = request.getHeader(AUTHORIZATION_HEADER);
		if (authorization == null) {
			log.warn("request doesn't contain an authorization header");
			rc.response().setStatusCode(401).end();
			return; // request does not contain an authorization header
		}
		if (!authorization.toLowerCase(Locale.ENGLISH).startsWith(BASIC_PREFIX)) {
			log.warn("request doesn't contain basic auth scheme");
			rc.response().setStatusCode(401).end();
			return; // request does not contain a valid basic auth scheme
		}

		// extract username:password credentials
		String base64Challenge = authorization.substring(PREFIX_LENGTH);
		byte[] decode = Base64.getDecoder().decode(base64Challenge);
		String plainChallenge = new String(decode, StandardCharsets.UTF_8);
		String userName = null;
		byte[] password = null;

		int colonPos;
		if ((colonPos = plainChallenge.indexOf(':')) > -1) {
			userName = plainChallenge.substring(0, colonPos);
			String passwordString = plainChallenge.substring(colonPos + 1);
			password = passwordString.getBytes(StandardCharsets.UTF_8);

			if (log.isDebugEnabled()) {
				log.debugf("Found basic auth header %s:*****", userName);
			}
		}

		// credentials check
		if (!"admin".equals(userName)
			|| !MessageDigest.isEqual(adminPassword, password)) {

			if (log.isDebugEnabled()) {
				log.debugf("Invalid credentials.");
			}

			rc.response().setStatusCode(401).end();
			return;
		}

		// valid credentials are enough, we don't need to inject
		// principal information in the routingContext, the request
		// can just go through the next filters.
		rc.next();
	}

}