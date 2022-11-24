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

package io.openk9.tika.config;

import lombok.Getter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;

@Getter
@ApplicationScoped
public class TikaConfiguration {

	@ConfigProperty(name = "openk9.tika.ocr.enabled", defaultValue = "false")
	boolean ocrEnabled;

	@ConfigProperty(name = "openk9.tika.ocr.rabbitmq.exchange", defaultValue = "amq.topic")
	String ocrExchange;

	@ConfigProperty(name = "openk9.tika.ocr.rabbitmq.routingkey", defaultValue = "io.openk9.tika.ocr")
	String ocrRoutingKey;

	@ConfigProperty(name = "openk9.tika.ocr.character.length", defaultValue = "10")
	int characterLength;

	@ConfigProperty(name = "openk9.tika.rabbitmq.exchange", defaultValue = "amq.topic")
	String currentExchange;

}
