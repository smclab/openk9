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

package io.openk9.ingestion.rabbitmq.factory;

import com.rabbitmq.client.AMQP;
import io.openk9.ingestion.api.BasicProperties;
import io.openk9.ingestion.api.BasicPropertiesFactory;
import io.openk9.ingestion.rabbitmq.wrapper.BasicPropertiesWrapper;
import org.osgi.service.component.annotations.Component;

import java.util.Objects;
import java.util.function.Function;

@Component(
	immediate = true,
	service = BasicPropertiesFactory.class
)
public class BasicPropertiesFactoryImpl implements BasicPropertiesFactory {
	@Override
	public BasicProperties createBasicProperties(
		Function<BasicProperties.Builder, BasicProperties.Builder> function) {

		Objects.requireNonNull(function, "function is null");

		return function.apply(
			new BasicPropertiesWrapper.BuilderWrapper(
				new AMQP.BasicProperties.Builder())).build();
	}
}
