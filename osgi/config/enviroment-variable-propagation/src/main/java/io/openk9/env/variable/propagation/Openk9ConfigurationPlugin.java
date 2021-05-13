package io.openk9.env.variable.propagation;

import org.apache.felix.utils.properties.ConfigurationHandler;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationPlugin;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;

@Component(
	immediate = true,
	property = {
		ConfigurationPlugin.CM_RANKING + "=" + Openk9ConfigurationPlugin.PLUGIN_RANKING,
		"config.plugin.id" + "=" + Openk9ConfigurationPlugin.PLUGIN_ID
	},
	service = ConfigurationPlugin.class
)
public class Openk9ConfigurationPlugin implements ConfigurationPlugin {

	@Override
	public void modifyConfiguration(ServiceReference<?> reference, Dictionary<String, Object> properties) {
		final Object pid = properties.get(Constants.SERVICE_PID);
		for (Enumeration<String> keys = properties.keys(); keys.hasMoreElements(); ) {
			String key = keys.nextElement();
			// looking for env variable and system property matching key (ENV_PREFIX.pid.key).toUpperCase().replace('.', '_')
			String env = ENV_PREFIX + (pid + "." + key).toUpperCase().replaceAll("\\.", "_");
			String sys = SYS_PREFIX + pid + "." + key;

			if (System.getenv(env) != null) {
				properties.put(key, _readValue(System.getenv(env), properties.get(key)));
			}
			else if (System.getProperty(sys) != null) {
				properties.put(key, _readValue(System.getProperty(env), properties.get(key)));
			}

		}
	}

	private Object _readValue(String str, Object prevValue) {

		try {

			Object read = ConfigurationHandler.read(str);

			if (read != null) {
				return read;
			}

		}
		catch (IOException ioe) {
			_log.warn(ioe.getMessage());
		}

		if (prevValue instanceof Number) {
			return Integer.parseInt(str);
		}

		return str;

	}

	public static final int PLUGIN_RANKING = 501;
	public static final String PLUGIN_ID = "io.openk9.env.variable.propagation.plugin";
	private static final String ENV_PREFIX = "OPENK9_";
	private static final String SYS_PREFIX = "openk9.";

	private static final Logger _log = LoggerFactory.getLogger(
		Openk9ConfigurationPlugin.class);
}
