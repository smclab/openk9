package io.openk9.api.aggregator.model;

import io.quarkus.hibernate.reactive.panache.PanacheEntity;
import io.smallrye.mutiny.Uni;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.util.Objects;

@Entity(name = "security_tenant")
@Table(indexes = {
	@Index(name = "idx_tenant_realm_name", columnList = "realmName")
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Tenant extends PanacheEntity {
	private String realmName;
	private String virtualHost;
	private String clientId;
	private String clientSecret;
	private boolean active;

	public static Uni<Tenant> findByRealmName(String name){
		return find("realmName", name).firstResult();
	}

	public static Uni<Long> countByRealmName(String name){
		return find("realmName", name).count();
	}

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
