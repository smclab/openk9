package io.openk9.search.query.internal.query.parser;

import io.openk9.search.api.query.parser.Annotator;
import io.openk9.search.query.internal.query.parser.util.Rules;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import java.util.List;

@Component(
	immediate = true, service = GrammarProvider.class
)
public class GrammarProvider {

	@Activate
	void activate(BundleContext bundleContext) {
		_grammar = new Grammar(
			List.of(GrammarMixin.of(Rules.getRules(), _annotatorList)));
	}

	@Modified
	void modified(BundleContext bundleContext) {
		deactivate();
		activate(bundleContext);
	}

	@Deactivate
	void deactivate() {
		_grammar = null;
	}

	public Grammar getGrammar() {
		return _grammar;
	}

	private transient Grammar _grammar;

	@Reference(
		policy = ReferencePolicy.STATIC,
		policyOption = ReferencePolicyOption.GREEDY
	)
	private List<Annotator> _annotatorList;

}
