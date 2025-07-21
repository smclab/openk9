package io.openk9.datasource.model.init;

import io.openk9.datasource.grpc.Preset;
import io.openk9.datasource.grpc.PresetPluginDrivers;
import io.openk9.datasource.model.PluginDriver;
import io.openk9.datasource.model.dto.base.PluginDriverDTO;
import io.openk9.datasource.plugindriver.HttpPluginDriverInfo;
import io.vertx.core.json.Json;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.EnumMap;
import java.util.Map;

public class PluginDrivers {

	private static final Map<Preset, PresetConfiguration> CONFIGURATION_MAP = new EnumMap<>(Preset.class);
	private static final String PORT = "5000";

	static {
		CONFIGURATION_MAP.put(
			Preset.YOUTUBE,
			PresetConfiguration.builder()
				.name("Youtube")
				.description("Plugin Driver for Youtube Channel")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.YOUTUBE))
				.secure(false)
				.path("/execute")
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.CRAWLER,
			PresetConfiguration.builder()
				.name("Crawler")
				.description("Plugin Driver for Generic Crawling")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.CRAWLER))
				.secure(false)
				.path("/startUrlsCrawling")
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.EMAIL,
			PresetConfiguration.builder()
				.name("Email")
				.description("Plugin Driver for Imap Server")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.EMAIL))
				.secure(false)
				.path("/execute")
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.GITLAB,
			PresetConfiguration.builder()
				.name("Gitlab")
				.description("Plugin Driver for Gitlab Server")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.GITLAB))
				.secure(false)
				.path("/execute")
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.SITEMAP,
			PresetConfiguration.builder()
				.name("Sitemap")
				.description("Plugin Driver for Sitemap Server")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.SITEMAP))
				.secure(false)
				.path("/startSitemapCrawling")
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.DATABASE,
			PresetConfiguration.builder()
				.name("Database")
				.description("Plugin Driver for Database. Supporting PostgreSql, Mysql, MariaDB.")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.DATABASE))
				.secure(false)
				.path("/execute")
				.build()
		);
		CONFIGURATION_MAP.put(
			Preset.MINIO,
			PresetConfiguration.builder()
				.name("Minio")
				.description("Plugin Driver for Minio.")
				.type(PluginDriver.PluginDriverType.HTTP)
				.method(HttpPluginDriverInfo.Method.POST)
				.provisioning(PluginDriver.Provisioning.SYSTEM)
				.baseUri(PresetPluginDrivers.getPluginDriver(Preset.MINIO))
				.secure(false)
				.path("/execute")
				.build()
		);
	}

	public static PluginDriverDTO getPluginDriverDTO(Preset preset) {
		return createPluginDriverDTO(null, preset);
	}

	public static PluginDriverDTO getPluginDriverDTO(String schemaName, Preset preset) {
		return createPluginDriverDTO(schemaName, preset);
	}

	private static HttpPluginDriverInfo buildJsonConfig(PresetConfiguration presetConfiguration, String schemaName) {
		StringBuilder sb = new StringBuilder(presetConfiguration.getBaseUri());

		if (schemaName != null) {
			sb.append("-");
			sb.append(schemaName);
		}

		sb.append(":");
		sb.append(PORT);

		return HttpPluginDriverInfo.builder()
			.baseUri(sb.toString())
			.secure(presetConfiguration.isSecure())
			.path(presetConfiguration.getPath())
			.method(presetConfiguration.getMethod())
			.build();
	}

	private static PluginDriverDTO createPluginDriverDTO(String schemaName, Preset preset) {
		PresetConfiguration presetConfiguration = CONFIGURATION_MAP.get(preset);

		var jsonConfig = buildJsonConfig(presetConfiguration, schemaName);

		return PluginDriverDTO.builder()
			.name(presetConfiguration.getName())
			.description(presetConfiguration.getDescription())
			.type(presetConfiguration.getType())
			.provisioning(presetConfiguration.getProvisioning())
			.jsonConfig(Json.encode(jsonConfig))
			.build();
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	private static class PresetConfiguration {
		private String baseUri;
		private String description;
		private HttpPluginDriverInfo.Method method;
		private String name;
		private String path;
		private PluginDriver.Provisioning provisioning;
		private boolean secure;
		private PluginDriver.PluginDriverType type;
	}

}
