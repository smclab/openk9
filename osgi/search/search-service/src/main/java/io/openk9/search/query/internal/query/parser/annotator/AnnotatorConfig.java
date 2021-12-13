package io.openk9.search.query.internal.query.parser.annotator;

import org.elasticsearch.common.unit.Fuzziness;

import java.time.Duration;

public interface AnnotatorConfig {

	public String[] nerAnnotator();

	public String[] aggregatorAnnotator();

	public String[] stopWords();

	public Fuzziness nerAnnotatorFuzziness();

	public Fuzziness restFuzziness();

	public int restSize();

	public int nerSize();

	public Duration timeout();

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

}
