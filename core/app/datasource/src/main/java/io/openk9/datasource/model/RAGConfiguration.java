package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rag_configuration")
@Data
public class RAGConfiguration extends K9Entity {

	@Column(name = "description", length = 4096)
	private String description;
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@Column(name = "type")
	@Enumerated(EnumType.STRING)
	private RAGType type;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "prompt")
	private String prompt;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "rephrase_prompt")
	private String rephrasePrompt;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "prompt_no_rag")
	private String promptNoRag;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "rag_tool_description")
	private String ragToolDescription;
	@Column(name = "chunk_window")
	private int chunkWindow;
	@Column(name = "reformulate")
	private boolean reformulate;
}
