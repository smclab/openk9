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

import io.openk9.datasource.model.dto.LanguageDTO;

import java.util.Set;

public class Languages {

	public static final LanguageDTO ARABIC = LanguageDTO.builder()
		.name("Arabic")
		.value("ar_SA")
		.build();
	public static final LanguageDTO CATALAN = LanguageDTO.builder()
		.name("Catalan")
		.value("ca_ES")
		.build();
	public static final LanguageDTO CHINESE = LanguageDTO.builder()
		.name("Chinese")
		.value("zh_CN")
		.build();
	public static final LanguageDTO DUTCH = LanguageDTO.builder()
		.name("Dutch")
		.value("nl_NL")
		.build();
	public static final LanguageDTO ENGLISH = LanguageDTO.builder()
		.name("English")
		.value("en_US")
		.build();
	public static final LanguageDTO FINNISH = LanguageDTO.builder()
		.name("Finnish")
		.value("fi_FI")
		.build();
	public static final LanguageDTO FRENCH = LanguageDTO.builder()
		.name("French")
		.value("fr_FR")
		.build();
	public static final LanguageDTO GERMAN = LanguageDTO.builder()
		.name("German")
		.value("de_DE")
		.build();
	public static final LanguageDTO HUNGARIAN = LanguageDTO.builder()
		.name("Hungarian")
		.value("hu_HU")
		.build();
	public static final LanguageDTO ITALIAN = LanguageDTO.builder()
		.name("Italian")
		.value("it_IT")
		.build();
	public static final LanguageDTO JAPANESE = LanguageDTO.builder()
		.name("Japanese")
		.value("ja_JP")
		.build();
	public static final LanguageDTO PORTUGUESE = LanguageDTO.builder()
		.name("Portuguese")
		.value("pt_BR")
		.build();
	public static final LanguageDTO SPANISH = LanguageDTO.builder()
		.name("Spanish")
		.value("es_ES")
		.build();
	public static final LanguageDTO SWEDISH = LanguageDTO.builder()
		.name("Swedish")
		.value("sv_SE")
		.build();


	public static final Set<LanguageDTO> INSTANCE = Set.of(
		ARABIC,
		CATALAN,
		CHINESE,
		DUTCH,
		ENGLISH,
		FINNISH,
		FRENCH,
		GERMAN,
		HUNGARIAN,
		ITALIAN,
		JAPANESE,
		PORTUGUESE,
		SPANISH,
		SWEDISH
	);

	private Languages() {}

}
