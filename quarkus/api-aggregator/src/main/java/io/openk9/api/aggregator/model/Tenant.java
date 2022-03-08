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
import javax.persistence.UniqueConstraint;
import java.util.Objects;

@Entity(name = "security_tenant")
@Table(indexes = {
	@Index(name = "idx_tenant_name", columnList = "name")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uc_tenant_name", columnNames = {"name"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Tenant extends PanacheEntity {
	private String name;
	private String authServerUrl;
	private String clientId;
	private String clientSecret;
	private boolean active;

	public static Uni<Tenant> findByName(String name){
		return find("name", name).firstResult();
	}

	public static Uni<Long> countByName(String name){
		return find("name", name).count();
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
