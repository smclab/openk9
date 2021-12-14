package io.openk9.vertx.configuration.plugin;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.consul.ConsulClient;
import io.vertx.ext.consul.KeyValue;
import io.vertx.ext.consul.KeyValueList;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Component(
	immediate = true,
	service = ConsulConfigurationListener.class
)
public class ConsulConfigurationListener {

	@Activate
	void activate(BundleContext bundleContext) {
		_executorService = Executors.newSingleThreadScheduledExecutor();

		_executorService.scheduleAtFixedRate(() ->
			_consulClient.getValues(_serviceNameSupplier.get(), list -> {

			if (list.succeeded()) {
				KeyValueList result = list.result();

				List<KeyValue> list1 = result.getList();

				if (list1 != null) {

					for (KeyValue keyValue : list1) {
						String pid = keyValue.getKey();

						if (pid.equals(_serviceNameSupplier.get())) {
							continue;
						}
						else if (pid.startsWith(_serviceNameSupplier.get())) {
							pid = pid.substring(_serviceNameSupplier.get().length() + 1);
						}

						JsonObject jsonConfig =
							new JsonObject(keyValue.getValue());

						try {
							Configuration configuration =
								_configurationAdmin.getConfiguration(pid, null);

							Dictionary<String, Object> dict = new Hashtable<>();

							for (Map.Entry<String, Object> entry : jsonConfig) {
								Object value = entry.getValue();

								if (value instanceof JsonArray) {
									dict.put(entry.getKey(), _createArray(((JsonArray) value)));
								}
								else {
									dict.put(entry.getKey(), value);
								}
							}

							Dictionary<String, Object> properties =
								configuration.getProperties();

							if (properties == null) {
								properties = new Hashtable<>();
							}

							Enumeration<String> keys = dict.keys();

							while (keys.hasMoreElements()) {
								String key = keys.nextElement();
								properties.put(key, dict.get(key));
							}

							if (configuration.updateIfDifferent(properties)) {
								_log.info("update configuration: " + pid);
							}

						}
						catch (IOException e) {
							throw new RuntimeException(e);
						}

					}
				}
			}

		}), 1000, 5000, TimeUnit.MILLISECONDS);

	}

	private Object[] _createArray(JsonArray jsonArray) {

		if (jsonArray.isEmpty()) {
			return new String[0];
		}

		Object[] result = null;

		for (int i = 0; i < jsonArray.size(); i++) {

			Object value = jsonArray.getValue(i);

			if (result == null) {
				if (value instanceof Integer) {
					result = new Integer[jsonArray.size()];
				}
				else if (value instanceof Long) {
					result = new Long[jsonArray.size()];
				}
				else if (value instanceof Double) {
					result = new Double[jsonArray.size()];
				}
				else if (value instanceof Float) {
					result = new Float[jsonArray.size()];
				}
				else {
					result = new String[jsonArray.size()];
				}
			}

			result[i] = value;

		}

		return result;

	}

	@Deactivate
	void deactivate() {
		_executorService.shutdown();
	}

	private ScheduledExecutorService _executorService;

	@Reference
	private ConsulClient _consulClient;

	@Reference(
		target = "(service.name=this)"
	)
	private Supplier<String> _serviceNameSupplier;

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	private static final Logger _log = LoggerFactory.getLogger(
		ConsulConfigurationListener.class);

}
