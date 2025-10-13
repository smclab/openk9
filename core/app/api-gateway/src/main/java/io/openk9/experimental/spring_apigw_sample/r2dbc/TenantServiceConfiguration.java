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

package io.openk9.experimental.spring_apigw_sample.r2dbc;

import java.util.Set;

import io.openk9.experimental.spring_apigw_sample.security.TenantSecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.NoOpCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class TenantServiceConfiguration {

	@Autowired
	DatabaseClient databaseClient;
	@Autowired
	R2dbcEntityTemplate entityTemplate;

	@Bean
	@Profile("!poc")
	CacheManager cacheManager() {
		SimpleCacheManager cacheManager = new SimpleCacheManager();
		cacheManager.setCaches(Set.of(new ConcurrentMapCache("default")));

		return cacheManager;
	}

	@Bean
	TenantSecurityService tenantServiceR2dbc(
		DatabaseClient databaseClient, CacheManager cacheManager) {

		var cache = cacheManager.getCache("default");
		if (cache == null) {
			cache = new NoOpCache("noop");
		}

		return new TenantSecurityServiceR2dbc(databaseClient, cache);
	}

	@Bean
	TenantWriteServiceR2dbc tenantWriteService() {
		return new TenantWriteServiceR2dbc(databaseClient, entityTemplate);
	}

}
