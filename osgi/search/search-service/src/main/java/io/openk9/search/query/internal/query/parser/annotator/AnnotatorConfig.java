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

package io.openk9.search.query.internal.query.parser.annotator;

import org.elasticsearch.common.unit.Fuzziness;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
	name = "Annotator Configuration",
	pid = "$"
)
public @interface AnnotatorConfig {

	String OCD = "io.openk9.search.query.internal.query.parser.annotator.AnnotatorConfig";
	String PID = "io.openk9.search.query.internal.query.parser.annotator.AnnotatorConfig";

	InternalFuzziness nerAnnotatorFuzziness() default InternalFuzziness.ONE;

	InternalFuzziness restFuzziness() default InternalFuzziness.AUTO;

	int nerSize() default 5;

	int restSize() default 10;

	int autocompleteSize() default 10;

	int autocorrectionSize() default 2;

	long timeoutMs() default 10_000;

	String[] autocompleteEntityFields() default {"name", "type", "id", "tenantId"};

	String[] autocompleteEntityTypes() default {"person", "organization", "location", "email"};


	String[] autocompleteAnnotator() default {};

	String[] autocorrectAnnotator() default {};

	String[] aggregatorAnnotator() default {};

	String[] nerAnnotator() default {
		"person", "loc", "organization"
	};

	boolean aggregatorKeywordKeyEnable() default false;

	String[] stopWords() default {
		"ad", "al", "allo", "ai", "agli", "all", "agl", "alla", "alle",
		"con", "col", "coi", "da", "dal", "dallo", "dai", "dagli", "dall",
		"dagl", "dalla", "dalle", "di", "del", "dello", "dei", "degli",
		"dell", "degl", "della", "delle", "in", "nel", "nello", "nei", "negli",
		"nell", "negl", "nella", "nelle", "su", "sul", "sullo", "sui", "sugli",
		"sull", "sugl", "sulla", "sulle", "per", "tra", "contro", "io", "tu",
		"lui", "lei", "noi", "voi", "loro", "mio", "mia", "miei", "mie", "tuo",
		"tua", "tuoi", "tue", "suo", "sua", "suoi", "sue", "nostro", "nostra",
		"nostri", "nostre", "vostro", "vostra", "vostri", "vostre", "mi", "ti",
		"ci", "vi", "lo", "la", "li", "le", "gli", "ne", "il", "un", "uno", "una",
		"ma", "ed", "se", "perché", "anche", "come", "dov", "dove", "che", "chi", "cui",
		"non", "più", "quale", "quanto", "quanti", "quanta", "quante", "quello", "quelli",
		"quella", "quelle", "questo", "questi", "questa", "queste", "si", "tutto", "tutti",
		"a", "c", "e", "i", "l", "o", "ho", "hai", "ha", "abbiamo", "avete", "hanno", "abbia",
		"abbiate", "abbiano", "avrò", "avrai", "avrà", "avremo", "avrete", "avranno", "avrei",
		"avresti", "avrebbe", "avremmo", "avreste", "avrebbero", "avevo", "avevi", "aveva",
		"avevamo", "avevate", "avevano", "ebbi", "avesti", "ebbe", "avemmo", "aveste", "ebbero",
		"avessi", "avesse", "avessimo", "avessero", "avendo", "avuto", "avuta", "avuti", "avute",
		"sono", "sei", "è", "siamo", "siete", "sia", "siate", "siano", "sarò", "sarai", "sarà", "saremo",
		"sarete", "saranno", "sarei", "saresti", "sarebbe", "saremmo", "sareste", "sarebbero", "ero", "eri",
		"era", "eravamo", "eravate", "erano", "fui", "fosti", "fu", "fummo", "foste", "furono", "fossi",
		"fosse", "fossimo", "fossero", "essendo", "faccio", "fai", "facciamo", "fanno", "faccia", "facciate",
		"facciano", "farò", "farai", "farà", "faremo", "farete", "faranno", "farei", "faresti", "farebbe",
		"faremmo", "fareste", "farebbero", "facevo", "facevi", "faceva", "facevamo", "facevate", "facevano", "feci",
		"facesti", "fece", "facemmo", "faceste", "fecero", "facessi", "facesse", "facessimo", "facessero", "facendo",
		"sto", "stai", "sta", "stiamo", "stanno", "stia", "stiate", "stiano", "starò", "starai", "starà", "staremo",
		"starete", "staranno", "starei", "staresti", "starebbe", "staremmo", "stareste", "starebbero", "stavo", "stavi",
		"stava", "stavamo", "stavate", "stavano", "stetti", "stesti", "stette", "stemmo", "steste", "stettero", "stessi",
		"stesse", "stessimo", "stessero", "stando"
	};

	String ruleJsonPath() default "";

	int autoCompleteTokenCount() default 2;

	enum InternalFuzziness {
		ZERO(Fuzziness.ZERO), ONE(Fuzziness.ONE), TWO(Fuzziness.TWO), AUTO(Fuzziness.AUTO);

		InternalFuzziness(Fuzziness fuzziness) {
			_fuzziness = fuzziness;
		}

		public Fuzziness getFuzziness() {
			return _fuzziness;
		}

		private final Fuzziness _fuzziness;

	}

}
