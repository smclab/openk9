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

package com.openk9.schemaregistry.internal.repository;

import com.openk9.schemaregistry.exception.InvalidSchemaException;
import com.openk9.schemaregistry.model.Schema;
import com.openk9.schemaregistry.repository.SchemaRegistryRepository;
import com.openk9.schemaregistry.validator.SchemaValidator;
import io.r2dbc.spi.ConnectionFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component(
	immediate = true,
	service = SchemaRegistryRepository.class
)
public class SchemaRegistryRepositoryImpl implements SchemaRegistryRepository {

	@Override
	public Mono<Schema> registerSchema(Mono<Schema> mono) {

		return mono
			.<Schema>handle((schema, sink) -> {

				try {
					_schemaValidator.validate(schema.getDefinition());
				}
				catch (InvalidSchemaException e) {
					sink.error(e);
					return;
				}

				sink.next(schema);
			})
			.flatMap(o ->
					findBySubjectAndFormatOrderByVersion(
						o.getSubject(), o.getFormat())
					.collectList()
					.flatMap(schemas -> {
						if (schemas.isEmpty()) {
							return addSchema(
								Schema.of(
									o.getId(), 1, o.getSubject(),
									o.getFormat(), o.getDefinition()));
						}
						else {

							Schema result = _schemaValidator.match(
								schemas,
								o.getDefinition());

							if (result == null) {
								Schema schema = Schema.of(
									o.getId(), schemas.get(
										schemas.size() - 1)
												   .getVersion() + 1,
									o.getSubject(), o.getFormat(),
									o.getDefinition());

								return addSchema(schema);
							}
							else {
								return Mono.just(result);
							}
						}
					}));
	}

	@Override
	public Mono<Schema> addSchema(Schema schema) {


		return Mono.from(_connectionFactory.create())
			.flatMap(connection -> QueryUtil.addSchema(connection, schema));
	}

	@Override
	public Flux<Schema> addSchemas(
		List<Schema> schemas) {
		return Mono.from(_connectionFactory.create())
			.flatMapMany(
				connection -> QueryUtil.addSchemas(connection, schemas));
	}

	@Override
	public Mono<Schema> findById(Integer id) {

		return Mono.from(_connectionFactory.create())
			.flatMap(connection -> QueryUtil.findById(connection, id));
	}

	@Override
	public Mono<Schema> findBySubjectAndFormatAndVersion(
		String subject, String format, Integer version) {

		return Mono.from(_connectionFactory.create())
			.flatMap(
				connection -> QueryUtil
					.findBySubjectAndFormatAndVersion(
						connection, subject, format, version));
	}

	@Override
	public Flux<Schema> findBySubjectAndFormatOrderByVersion(
		String subject, String format) {

		return Mono.from(_connectionFactory.create())
			.flatMapMany(
				connection -> QueryUtil
					.findBySubjectAndFormatOrderByVersion(
						connection, subject, format));
	}

	@Override
	public Mono<?> removeSchema(Mono<Schema> schema) {
		return Mono.from(_connectionFactory.create())
			.flatMap(
				connection -> QueryUtil
					.removeSchema(connection, schema));
	}

	@Reference
	private ConnectionFactory _connectionFactory;

	@Reference
	private SchemaValidator _schemaValidator;

}
