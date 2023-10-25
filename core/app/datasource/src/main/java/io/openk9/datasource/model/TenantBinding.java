package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

@Entity
@Table(name = "tenant_binding")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class TenantBinding extends K9Entity {
	@Column(name = "virtual_host", nullable = false, unique = true)
	private String virtualHost;

	@OneToOne
	@JoinColumn(name = "tenant_binding_bucket_id")
	private Bucket bucket;

}