package io.openk9.index.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@Builder
public class IndexTemplateDTO {
	private String indexTemplateName;
	private String settings;
	private List<String> indexPatterns;
	private String mappings;
	private List<String> componentTemplates;
	private long priority;
}
