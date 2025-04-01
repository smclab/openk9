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

package io.openk9.datasource.pipeline.stages.working;

import io.openk9.datasource.pipeline.actor.WriterException;
import io.openk9.datasource.util.CborSerializable;

public interface Writer {

	interface Command extends CborSerializable {}

	interface Response extends CborSerializable {
		HeldMessage heldMessage();
	}

	record Start(byte[] dataPayload, HeldMessage heldMessage)
		implements Command {}

	record Success(
		byte[] dataPayload,
		HeldMessage heldMessage
	) implements Response {}

	record Failure(WriterException exception, HeldMessage heldMessage) implements Response {}

}
