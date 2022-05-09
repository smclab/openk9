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

package io.openk9.index.writer.mappings.pub.sub.binding;

import io.openk9.ingestion.api.Binding;
import org.osgi.service.component.annotations.Component;

@Component(
	immediate = true,
	service = Binding.class
)
public class MappingsBinding implements Binding {

	@Override
	public Exchange getExchange() {
		return Exchange.of(
			"index-writer-mappings.fanout",
			Exchange.Type.fanout);
	}

	@Override
	public String getRoutingKey() {
		return "#";
	}

	@Override
	public String getQueue() {
		return "index-writer-mappings";
	}

}
