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

package io.openk9.k8sclient;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.kubernetes.client.KubernetesClientObjectMapperCustomizer;

import jakarta.inject.Singleton;

/**
 * Configures the Fabric8 Kubernetes client's ObjectMapper to ignore
 * unknown JSON properties. ArgoCD Application resources include
 * top-level fields (e.g. "operation") that are not modeled in the
 * generated Java CRD classes, causing deserialization failures on
 * createOr/update operations without this setting.
 */
@Singleton
public class KubernetesObjectMapperCustomizer
	implements KubernetesClientObjectMapperCustomizer {

	@Override
	public void customize(ObjectMapper objectMapper) {
		objectMapper.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

}
