package io.openk9.search.query.internal.query.parser.annotator;

import org.elasticsearch.common.unit.Fuzziness;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import java.time.Duration;

@Component(immediate = true, service = AnnotatorConfig.class)
@Designate(ocd = AnnotatorConfig.Config.class)
public class AnnotatorConfig {

	@ObjectClassDefinition(
		name = "Annotator Configuration"
	)
	@interface Config {
		InternalFuzziness nerAnnotatorFuzziness() default InternalFuzziness.ONE;

		InternalFuzziness restFuzziness() default InternalFuzziness.AUTO;

		int nerSize() default 5;

		int restSize() default 10;

		long timeoutMs() default 10_000;

		String[] aggregatorAnnotator() default {

		};

		String[] nerAnnotator() default {
			"person", "loc", "organization"
		};

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

	}

	@Activate
	void activate(Config config) {
		_config = config;
	}

	@Modified
	void modified(Config config) {
		_config = config;
	}

	@Deactivate
	void deactivate() {
	}

	public String[] nerAnnotator() {
		return _config.nerAnnotator();
	}

	public String[] aggregatorAnnotator() {
		return _config.aggregatorAnnotator();
	}

	public String[] stopWords() {
		return _config.stopWords();
	}

	public Fuzziness nerAnnotatorFuzziness() {
		return _config.nerAnnotatorFuzziness().getFuzziness();
	}

	public Fuzziness restFuzziness() {
		return _config.restFuzziness().getFuzziness();
	}

	public int restSize() {
		return _config.restSize();
	}

	public int nerSize() {
		return _config.nerSize();
	}

	public Duration timeout() {
		return Duration.ofMillis(_config.timeoutMs());
	}

	public enum InternalFuzziness {
		ZERO(Fuzziness.ZERO), ONE(Fuzziness.ONE), TWO(Fuzziness.TWO), AUTO(Fuzziness.AUTO);

		InternalFuzziness(Fuzziness fuzziness) {
			_fuzziness = fuzziness;
		}

		public Fuzziness getFuzziness() {
			return _fuzziness;
		}

		private final Fuzziness _fuzziness;

	}

	private Config _config;

}
