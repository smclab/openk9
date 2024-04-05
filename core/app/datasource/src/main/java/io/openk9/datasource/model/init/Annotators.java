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

package io.openk9.datasource.model.init;

import io.openk9.datasource.model.AnnotatorType;
import io.openk9.datasource.model.dto.AnnotatorDTO;
import io.openk9.datasource.model.util.Fuzziness;

import java.util.Set;

public class Annotators {

	private static final AnnotatorDTO TOKEN_ANNOTATOR = AnnotatorDTO.builder()
		.name("token annotator")
		.description("")
		.fieldName("token")
		.fuziness(Fuzziness.ZERO)
		.size(1)
		.type(AnnotatorType.TOKEN)
		.build();
	private static final AnnotatorDTO KEYWORD_ANNOTATOR = AnnotatorDTO.builder()
		.name("keyword annotator")
		.description("")
		.fieldName("keyword")
		.fuziness(Fuzziness.ZERO)
		.size(1)
		.type(AnnotatorType.KEYWORD)
		.build();
	private static final AnnotatorDTO STOPWORD_ANNOTATOR = AnnotatorDTO.builder()
		.name("stopword annotator")
		.description("")
		.fieldName("stopword")
		.fuziness(Fuzziness.ZERO)
		.size(1)
		.type(AnnotatorType.STOPWORD)
		.build();

	public static final Set<AnnotatorDTO> INSTANCE = Set.of(
		TOKEN_ANNOTATOR,
		KEYWORD_ANNOTATOR,
		STOPWORD_ANNOTATOR
	);

	private Annotators() {}

}
