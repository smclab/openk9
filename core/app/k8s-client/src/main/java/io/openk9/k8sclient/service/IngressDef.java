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

package io.openk9.k8sclient.service;

import java.util.List;

public record IngressDef(String ingressName, String virtualHost, List<Route> routes) {

	public static IngressDef of(String ingressName, String virtualHost, List<Route> routes) {

		return new IngressDef(ingressName, virtualHost, routes);
	}

	public record Route(String path, String serviceName, int servicePort) {
		public static Route of(String path, String serviceName, int servicePort) {
			return new Route(path, serviceName, servicePort);
		}

	}

}
