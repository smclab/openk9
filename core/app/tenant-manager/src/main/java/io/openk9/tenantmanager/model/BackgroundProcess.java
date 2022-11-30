package io.openk9.tenantmanager.model;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "background_process")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
public class BackgroundProcess {

	@Setter(AccessLevel.NONE)
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "id", nullable = false)
	private UUID id;

	@Setter(AccessLevel.NONE)
	@Column(name = "create_date")
	@CreationTimestamp
	private OffsetDateTime createDate;

	@Setter(AccessLevel.NONE)
	@Column(name = "modified_date")
	@UpdateTimestamp
	private OffsetDateTime modifiedDate;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private Status status;

	@Lob
	@Column(name = "message")
	private String message;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "tenant_id", unique = true)
	private Tenant tenant;

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || Hibernate.getClass(this) !=
						 Hibernate.getClass(o)) {
			return false;
		}
		BackgroundProcess that = (BackgroundProcess) o;
		return id != null && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	public enum Status {
		IN_PROGRESS, FINISHED, FAILED
	}

}