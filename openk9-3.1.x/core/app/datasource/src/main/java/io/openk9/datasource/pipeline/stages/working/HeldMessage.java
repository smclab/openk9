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

import java.util.Objects;

import io.openk9.common.util.ingestion.ShardingKey;

public record HeldMessage(
	ShardingKey processKey,
	long messageNumber,
	long parsingDate,
	String contentId
) {
	@Override
	public boolean equals(Object o) {
		if (this == o) {return true;}
		if (!(o instanceof HeldMessage that)) {return false;}
		return parsingDate == that.parsingDate && messageNumber == that.messageNumber &&
			   Objects.equals(processKey, that.processKey) &&
			   Objects.equals(contentId, that.contentId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			processKey, messageNumber, parsingDate, contentId);
	}

}
