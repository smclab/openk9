package io.openk9.datasource.event.consumer.internal;

import io.openk9.datasource.event.configuration.DatasourceEventConfiguration;
import io.openk9.datasource.event.consumer.api.Constants;
import io.openk9.ingestion.api.Binding;
import io.openk9.ingestion.api.BindingRegistry;
import io.openk9.model.Datasource;
import io.openk9.model.EnrichItem;
import io.openk9.model.EnrichPipeline;
import io.openk9.model.Tenant;
import io.openk9.osgi.util.AutoCloseables;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component(
	immediate = true,
	service = DatasourceEventBinding.class
)
@Designate(ocd = DatasourceEventConfiguration.class)
public class DatasourceEventBinding {

	@Activate
	void activate(DatasourceEventConfiguration config) {
		for (String entityName : _ENTITY_NAMES) {
			for (String entityType : _EVENT_TYPES) {
				_autoCloseableSafes.add(
					_bindingRegistry.register(
						Binding.Exchange.of(
							config.exchange(), Binding.Exchange.Type.direct),
						_createRoutingKey(
							config.routingKeyTemplate(),
							entityType,
							entityName),
						Constants.QUEUE_PREFIX + entityType + "-" + entityName
					)
				);
			}
		}
	}

	@Modified
	void modified(DatasourceEventConfiguration config) {
		deactivate();
		activate(config);
	}

	@Deactivate
	void deactivate() {
		for (AutoCloseables.AutoCloseableSafe autoCloseableSafe : _autoCloseableSafes) {
			autoCloseableSafe.close();
		}
		_autoCloseableSafes.clear();
	}

	private static String _createRoutingKey(
		String template, String eventType, String entityName) {

		return template
			.replace("{eventType}", eventType)
			.replace("{entityName}", entityName);

	}

	private final List<AutoCloseables.AutoCloseableSafe> _autoCloseableSafes =
		new ArrayList<>();

	private static final Set<String> _ENTITY_NAMES =
		Set.of(
			Datasource.class.getSimpleName(),
			Tenant.class.getSimpleName(),
			EnrichItem.class.getSimpleName(),
			EnrichPipeline.class.getSimpleName(),
			Constants.ALL_OCCURRENCE
		);

	private static final Set<String> _EVENT_TYPES =
		Set.of("UPDATE", "INSERT", "DELETE", Constants.ALL_OCCURRENCE);

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private BindingRegistry _bindingRegistry;

}
