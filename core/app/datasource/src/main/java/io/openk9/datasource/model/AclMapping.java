package io.openk9.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "acl_mapping")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
@Cacheable
public class AclMapping {

	@EmbeddedId
	private PluginDriverDocTypeFieldKey key;

	@ToString.Exclude
	@ManyToOne(fetch = javax.persistence.FetchType.LAZY)
	@MapsId("pluginDriverId")
	@JoinColumn(name = "plugin_driver_id")
	private PluginDriver pluginDriver;

	@ManyToOne
	@MapsId("docTypeFieldId")
	@JoinColumn(name = "doc_type_field_id")
	@ToString.Exclude
	private DocTypeField docTypeField;

	@Enumerated(EnumType.STRING)
	@Column(name = "user_field", nullable = false)
	private UserField userField;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		AclMapping that = (AclMapping) o;
		return key != null && Objects.equals(key, that.key);
	}

	@Override
	public int hashCode() {
		return Objects.hash(key);
	}

}