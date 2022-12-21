package io.openk9.datasource.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
@EqualsAndHashCode
public class PluginDriverDocTypeFieldKey  implements Serializable {
	@Column(name = "plugin_driver_id")
	private Long pluginDriverId;
	@Column(name = "doc_type_field_id")
	private Long docTypeFieldId;
}
