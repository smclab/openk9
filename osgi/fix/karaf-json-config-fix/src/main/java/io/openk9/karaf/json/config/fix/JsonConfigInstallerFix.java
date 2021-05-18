package io.openk9.karaf.json.config.fix;

import org.apache.felix.cm.json.Configurations;
import org.apache.felix.fileinstall.ArtifactInstaller;
import org.apache.felix.fileinstall.internal.DirectoryWatcher;
import org.osgi.framework.Constants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/*
 * this component is not needed in karaf 4.3.2
 * @see https://github.com/apache/karaf/commit/e8cf789646c01cfbdf26bad03567df5171609f03
 */
@Component(
	immediate = true,
	property = {
		Constants.SERVICE_RANKING + ":Integer=1000"
	},
	service = ArtifactInstaller.class
)
public class JsonConfigInstallerFix implements ArtifactInstaller {

	@Override
	public void install(File artifact) throws Exception {
		setConfig(artifact);
	}

	@Override
	public void update(File artifact) throws Exception {
		setConfig(artifact);
	}

	@Override
	public void uninstall(File artifact) throws Exception {
		deleteConfig(artifact);
	}

	@Override
	public boolean canHandle(File artifact) {
		return artifact.getName().endsWith(".k9");
	}

	private void setConfig(File artifact) throws Exception {
		final String filename = artifact.getName();
		final ConfigurationPID configurationPID = ConfigurationPID.parseFilename(filename);
		Configuration configuration = getConfiguration(toConfigKey(artifact), configurationPID);
		Dictionary<String, Object> props = configuration.getProperties();
		Hashtable<String, Object> old = props != null ? new Hashtable<>(new DictionaryAsMap<>(props)) : null;
		Hashtable<String, Object> properties = Configurations.buildReader().build(new FileReader(artifact)).readConfiguration();
		if (old != null) {
			old.remove(DirectoryWatcher.FILENAME);
			old.remove(Constants.SERVICE_PID);
			old.remove(ConfigurationAdmin.SERVICE_FACTORYPID);
		}
		boolean updated = false;
		if (old == null || old.size() != properties.size()) {
			updated = true;
		} else {
			for (String key : old.keySet()) {
				Object oldValue = old.get(key);
				Object propertiesValue = properties.get(key);
				if (isArray(oldValue) && isArray(propertiesValue)) {
					updated = !Objects.deepEquals(oldValue, propertiesValue);
				} else {
					updated = !oldValue.equals(propertiesValue);
				}
				if (updated) {
					break;
				}
			}
		}
		if (updated) {
			properties.put(DirectoryWatcher.FILENAME, toConfigKey(artifact));
			if (old == null) {
				_log.info("Creating configuration from {}", artifact.getName());
			} else {
				_log.info("Updating configuration from {}", artifact.getName());
			}
			configuration.update(properties);
		}
	}

	void deleteConfig(File artifact) throws Exception {
		Configuration config = findExistingConfiguration(toConfigKey(artifact));
		if (Objects.nonNull(config)) {
			config.delete();
			_log.info("Configuration for {} found and deleted", artifact.getName());
		} else {
			_log.info("Configuration for {} not found, unable to delete", artifact.getName());
		}
	}

	String toConfigKey(File f) {
		return f.getAbsoluteFile().toURI().toString();
	}

	Configuration getConfiguration(String configKey, ConfigurationPID configurationPID) throws Exception {
		Configuration oldConfiguration = findExistingConfiguration(configKey);
		Configuration cachedConfiguration = oldConfiguration != null ?
			_configurationAdmin.getConfiguration(oldConfiguration.getPid(), null) : null;
		if (cachedConfiguration != null) {
			return cachedConfiguration;
		} else {
			final Configuration newConfiguration;
			if (configurationPID.isFactory()) {
				if (configurationPID.isR7()) {
					newConfiguration = _configurationAdmin.getFactoryConfiguration(configurationPID.getFactoryPid(), configurationPID.getName(), "?");
				} else {
					newConfiguration = _configurationAdmin.createFactoryConfiguration(configurationPID.getFactoryPid(), "?");
				}
			} else {
				newConfiguration = _configurationAdmin.getConfiguration(configurationPID.getPid(), "?");
			}
			return newConfiguration;
		}
	}

	Configuration findExistingConfiguration(String configKey) throws Exception {
		String filter = "(" + DirectoryWatcher.FILENAME + "=" + escapeFilterValue(configKey) + ")";
		Configuration[] configurations = _configurationAdmin.listConfigurations(filter);
		if (configurations != null && configurations.length > 0) {
			return configurations[0];
		} else {
			return null;
		}
	}

	private String escapeFilterValue(String s) {
		return s.replaceAll("[(]", "\\\\(").
			replaceAll("[)]", "\\\\)").
			replaceAll("[=]", "\\\\=").
			replaceAll("[\\*]", "\\\\*");
	}

	public static boolean isArray(Object obj) {
		return obj!=null && obj.getClass().isArray();
	}

	@Reference
	private ConfigurationAdmin _configurationAdmin;

	private static final Logger _log = LoggerFactory.getLogger(
		JsonConfigInstallerFix.class);

	private static class ConfigurationPID {

		private final String pid;
		private final String factoryPid;
		private final String name;

		private static final String TILDE = "~";
		private static final String DASH = "-";

		public ConfigurationPID(final String pid) {
			this.pid = pid;
			this.factoryPid = null;
			this.name = null;
		}

		public ConfigurationPID(final String pid, final String factoryPid, final String name) {
			this.pid = pid;
			this.factoryPid = factoryPid;
			this.name = name;
		}

		public String getPid() {
			return this.pid;
		}

		public String getFactoryPid() {
			return this.factoryPid;
		}

		public String getName() {
			return this.name;
		}

		public boolean isFactory() {
			return Objects.nonNull(factoryPid);
		}

		public boolean isR7() {
			return pid.contains(TILDE);
		}

		public static ConfigurationPID parsePid(final String pid) {
			final int index = pid.contains(TILDE) ? pid.indexOf(TILDE) : pid.indexOf(DASH);
			if (index > 0) {
				final String factoryPid = pid.substring(0, index);
				final String name = pid.substring(index + 1);
				return new ConfigurationPID(pid, factoryPid, name);
			} else {
				return new ConfigurationPID(pid);
			}
		}

		public static ConfigurationPID parseFilename(final String filename) {
			final String pid = filename.substring(0, filename.lastIndexOf('.'));
			return parsePid(pid);
		}

		public static ConfigurationPID parseFilename(final String filename, final String extension) {
			final String pid;
			if (extension.isEmpty()) {
				pid = filename;
			} else {
				final String ending = String.format(".%s", extension);
				if (filename.endsWith(ending)) {
					pid = filename.substring(0, filename.length() - ending.length());
				} else {
					final String message = String.format("Parsing filename failed. Filename '%s' does not have given extension '%s'.", filename, extension);
					throw new IllegalArgumentException(message);
				}
			}
			return parsePid(pid);
		}

	}

	private static class DictionaryAsMap<U, V> extends AbstractMap<U, V>
	{

		private Dictionary<U, V> dict;

		public DictionaryAsMap(Dictionary<U, V> dict)
		{
			this.dict = dict;
		}

		@Override
		public Set<Entry<U, V>> entrySet()
		{
			return new AbstractSet<Entry<U, V>>()
			{
				@Override
				public Iterator<Entry<U, V>> iterator()
				{
					final Enumeration<U> e = dict.keys();
					return new Iterator<Map.Entry<U, V>>()
					{
						private U key;
						public boolean hasNext()
						{
							return e.hasMoreElements();
						}

						public Map.Entry<U, V> next()
						{
							key = e.nextElement();
							return new DictionaryAsMap.KeyEntry(key);
						}

						public void remove()
						{
							if (key == null)
							{
								throw new IllegalStateException();
							}
							dict.remove(key);
						}
					};
				}

				@Override
				public int size()
				{
					return dict.size();
				}
			};
		}

		@Override
		public V put(U key, V value) {
			return dict.put(key, value);
		}

		class KeyEntry implements Map.Entry<U,V> {

			private final U key;

			KeyEntry(U key) {
				this.key = key;
			}

			public U getKey() {
				return key;
			}

			public V getValue() {
				return dict.get(key);
			}

			public V setValue(V value) {
				return DictionaryAsMap.this.put(key, value);
			}
		}

	}

}
