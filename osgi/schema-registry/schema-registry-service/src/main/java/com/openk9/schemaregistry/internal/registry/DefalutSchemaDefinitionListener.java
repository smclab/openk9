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

package com.openk9.schemaregistry.internal.registry;

import com.openk9.schemaregistry.model.Schema;
import com.openk9.schemaregistry.register.SchemaDefinition;
import com.openk9.schemaregistry.register.SchemaDefinitionListener;
import com.openk9.schemaregistry.repository.SchemaRegistryRepository;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Mono;

@Component(
	immediate = true,
	service = SchemaDefinitionListener.class
)
public class DefalutSchemaDefinitionListener
	implements SchemaDefinitionListener {

	@interface Config {
		boolean deleteSchema() default false;
	}

	@Activate
	public void activate(Config config) {
		_config = config;
	}

	@Modified
	public void modified(Config config) {
		activate(config);
	}

	@Override
	public void onRemove(SchemaDefinition schemaDefinition) {
		if (_config.deleteSchema()) {
			_schemaRegistryRepository.removeSchema(
				_schemaRegistryRepository
					.findBySubjectAndFormatAndVersion(
						schemaDefinition.getSubject(),
						schemaDefinition.getFormat(),
						schemaDefinition.getVersion()
					))
				.subscribe();
		}
	}

	@Override
	public void onCreate(SchemaDefinition schemaDefinition) {
		_schemaRegistryRepository
			.registerSchema(
				Mono.just(
					Schema
						.builder()
						.definition(schemaDefinition.getDefinition())
						.format(schemaDefinition.getFormat())
						.version(schemaDefinition.getVersion())
						.subject(schemaDefinition.getSubject())
						.build()
				)
			)
			.subscribe();
	}


	private Config _config;

	@Reference
	private SchemaRegistryRepository _schemaRegistryRepository;

}
