package io.openk9.search.client.api.indextemplate;

import org.elasticsearch.cluster.metadata.ComposableIndexTemplate;

import java.util.List;

public interface IndexTemplateService {

	void createOrUpdateIndexTemplate(
		String indexTemplateName, String settings, List<String> indexPatterns,
		String mappings, List<String> componentTemplates, long priority);

	void createOrUpdateIndexTemplate(
		String indexTemplateName, String settings, List<String> indexPatterns,
		String mappings, long priority);

	void createOrUpdateIndexTemplate(
		String indexTemplateName, List<String> indexPatterns, String mappings,
		long priority);

	void createOrUpdateIndexTemplate(
		String indexTemplateName,
		ComposableIndexTemplate composableIndexTemplate);

}
