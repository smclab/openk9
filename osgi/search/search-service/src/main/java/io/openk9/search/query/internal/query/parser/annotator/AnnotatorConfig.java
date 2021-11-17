package io.openk9.search.query.internal.query.parser.annotator;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;

@Component(immediate = true, service = AnnotatorConfig.class)
public class AnnotatorConfig {

	@interface Config {
		String[] stopWords() default {
			"di", "a", "da", "in", "con", "su", "per", "tra", "fra", "e", "i"
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

	public String[] stopWords() {
		return _config.stopWords();
	}

	private Config _config;

}
