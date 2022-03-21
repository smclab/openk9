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
import org.osgi.service.component.annotations.ReferencePolicyOption;
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
import java.util.stream.Collectors;

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

				String prefix = _serviceNameSupplier.get();

				if (list.succeeded()) {
					KeyValueList result = list.result();

					List<KeyValue> list1 = result.getList();

					if (list1 != null) {

						if (_log.isDebugEnabled()) {

							_log.debug(
								"keys length: " + list1.size()
							);

							List<String> collect =
								list1.stream().map(KeyValue::getKey).collect(
									Collectors.toList());

							_log.debug("keys found: " + collect);
						}

						for (KeyValue keyValue : list1) {

						String jsonValue = keyValue.getValue();

						try {

							if (!jsonValue.isBlank()) {

								String kvKey = keyValue.getKey();

								String substring = kvKey.substring(prefix.length() + 1);

								String[] split = substring.split(FOLDER_SEPARATOR);

								boolean isFactory = split.length == 2;

								Configuration configuration;

								String pid = split[0];

								if (isFactory) {
									String name = split[1];
									configuration = _configurationAdmin.getFactoryConfiguration(
										pid, name, null);
									if (_log.isDebugEnabled()) {
										_log.debug("getFactoryConfig: " + pid + " name: " + name);
									}
								}
								else {
									configuration = _configurationAdmin.getConfiguration(
											pid, null);
									if (_log.isDebugEnabled()) {
										_log.debug("getConfig: " + pid);
									}
								}

								JsonObject jsonConfig = new JsonObject(jsonValue);

								Dictionary<String, Object> dict = new Hashtable<>();

								for (Map.Entry<String, Object> entry : jsonConfig) {
									Object value = entry.getValue();

									if (value instanceof JsonArray) {
										dict.put(entry.getKey(), _createArray(((JsonArray) value)));
									}
									else if (value instanceof JsonObject) {
										dict.put(entry.getKey(), value.toString());
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
									if (isFactory) {
										_log.info("update configuration: " + configuration.getFactoryPid());
									}
									else {
										_log.info("update configuration: " + configuration.getPid());
									}

								}

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

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ConsulClient _consulClient;

	@Reference(
		target = "(service.name=this)",
		policyOption = ReferencePolicyOption.GREEDY
	)
	private Supplier<String> _serviceNameSupplier;

	@Reference(policyOption = ReferencePolicyOption.GREEDY)
	private ConfigurationAdmin _configurationAdmin;

	private static final Logger _log = LoggerFactory.getLogger(
		ConsulConfigurationListener.class);

	public static final String FOLDER_SEPARATOR = "/";

}
