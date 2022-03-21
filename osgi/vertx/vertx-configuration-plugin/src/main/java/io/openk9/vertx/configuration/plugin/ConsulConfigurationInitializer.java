package io.openk9.vertx.configuration.plugin;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component(
	immediate = true,
	property = {
		"consul.config.disabled=true"
	},
	service = ConsulConfigurationInitializer.class,
	enabled = false
)
public class ConsulConfigurationInitializer {

	@Activate
	void activate(BundleContext bundleContext) throws Exception {
		_serviceTracker = new ServiceTracker<>(
			bundleContext, bundleContext.createFilter(
				"(&" +
				"(component.name=*)" +
				"(!(consul.config.disabled=true))" +
				")"
		),
			new ServiceTrackerCustomizer<>() {
				@Override
				public Object addingService(
					ServiceReference<Object> reference) {

					Dictionary<String, Object> properties =
						reference.getProperties();

					Object pid = properties.get(Constants.SERVICE_PID);

					if (pid == null) {
						pid = properties.get("component.name");
					}

					try {
						if (pid instanceof String[]) {
							for (String p : (String[]) pid) {
								_storeConfiguration(reference, p);
							}

						}
						else {
							_storeConfiguration(reference, (String) pid);
						}
					}
					catch (Exception e) {
						_log.error(e.getMessage(), e);
					}

					return null;

				}

				@Override
				public void modifiedService(
					ServiceReference<Object> reference, Object key) {

				}

				@Override
				public void removedService(
					ServiceReference<Object> reference, Object key) {
					//_consulClient.deleteValue(key);
				}
			});

		_serviceTracker.open(true);
	}

	private void _storeConfiguration(
		ServiceReference<Object> reference, String pid) throws IOException {

		Configuration configuration =
			_configurationAdmin
				.getConfiguration(pid, null);

		Dictionary<String, Object> properties = configuration.getProperties();

		if (properties == null) {
			properties = new Hashtable<>();
		}

		Dictionary<String, Object> referenceProperties =
			reference.getProperties();

		Enumeration<String> keys = referenceProperties.keys();

		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			properties.put(key, referenceProperties.get(key));
		}

		properties.put("consul.config.disabled", "true");

		configuration.updateIfDifferent(properties);

		List<String> propKeys = Collections.list(properties.keys());

		Map<String, Object> dictCopy = propKeys.stream()
			.collect(Collectors.toMap(Function.identity(), properties::get));

		_consulClient
			.putValue(
				String.join("/", _serviceNameSupplier.get(), pid),
				JsonObject.mapFrom(dictCopy).encodePrettily())
			.toCompletionStage()
			.toCompletableFuture()
			.join();

		_log.info("store pid: " + pid);
	}

	@Modified
	void modified(BundleContext bundleContext) throws Exception {
		deactivate();
		activate(bundleContext);
	}

	@Deactivate
	void deactivate() {
		_serviceTracker.close();
	}

	private ServiceTracker<Object, Object> _serviceTracker;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private volatile ConsulClient _consulClient;

	@Reference(
		target = "(service.name=this)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private volatile Supplier<String> _serviceNameSupplier;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ConfigurationAdmin _configurationAdmin;

	private static final Logger _log = LoggerFactory.getLogger(
		ConsulConfigurationInitializer.class);


}
