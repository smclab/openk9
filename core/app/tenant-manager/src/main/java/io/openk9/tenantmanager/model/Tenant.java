package io.openk9.tenantmanager.model;

import io.openk9.common.graphql.util.relay.GraphqlId;
import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "tenant")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@RegisterForReflection
public class Tenant implements GraphqlId {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private Long id;

	@Setter(AccessLevel.NONE)
	@Column(name = "create_date")
	@CreationTimestamp
	private OffsetDateTime createDate;

	@Setter(AccessLevel.NONE)
	@Column(name = "modified_date")
	@UpdateTimestamp
	private OffsetDateTime modifiedDate;

	@Column(name = "schema_name", nullable = false)
	private String schemaName;

	@Column(name = "virtual_host", nullable = false, unique = true)
	private String virtualHost;

	@Column(name = "client_id", nullable = false)
	private String clientId;

	@Column(name = "client_secret")
	private String clientSecret;

	@Column(name = "realm_name", nullable = false)
	private String realmName;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		Tenant tenant = (Tenant) o;
		return id != null && Objects.equals(id, tenant.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}
}