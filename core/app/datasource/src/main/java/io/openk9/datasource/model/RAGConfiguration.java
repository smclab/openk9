/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.openk9.datasource.model;

import io.openk9.datasource.model.util.K9Entity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "rag_configuration")
@Data
@RequiredArgsConstructor
public class RAGConfiguration extends K9Entity {

	private static final Integer DEFAULT_CHUNK_WINDOW = 0;
	private static final Boolean DEFAULT_REFORMULATE = false;
	private static final String EMPTY_STRING = "";

	@Column(name = "chunk_window")
	private Integer chunkWindow = DEFAULT_CHUNK_WINDOW;
	@Column(name = "description", length = 4096)
	private String description;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;
	@Column(name = "name", nullable = false, unique = true)
	private String name;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "prompt")
	private String prompt = EMPTY_STRING;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "prompt_no_rag")
	private String promptNoRag = EMPTY_STRING;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "rag_tool_description")
	private String ragToolDescription = EMPTY_STRING;
	@Column(name = "reformulate")
	private Boolean reformulate = DEFAULT_REFORMULATE;
	@JdbcTypeCode(SqlTypes.LONG32VARCHAR)
	@Column(name = "rephrase_prompt")
	private String rephrasePrompt = EMPTY_STRING;
	@Column(name = "type", updatable = false)
	@Immutable
	@Enumerated(EnumType.STRING)
	private RAGType type;

	public void setChunkWindow(Integer chunkWindow) {
		this.chunkWindow = Objects.requireNonNullElse(chunkWindow, DEFAULT_CHUNK_WINDOW);
	}

	public void setPrompt(String prompt) {
		this.prompt = Objects.requireNonNullElse(prompt, EMPTY_STRING);
	}

	public void setPromptNoRag(String promptNoRag) {
		this.promptNoRag = Objects.requireNonNullElse(promptNoRag, EMPTY_STRING);
	}

	public void setRagToolDescription(String ragToolDescription) {
		this.ragToolDescription =
			Objects.requireNonNullElse(ragToolDescription, EMPTY_STRING);
	}

	public void setReformulate(Boolean reformulate) {
		this.reformulate = Objects.requireNonNullElse(reformulate, DEFAULT_REFORMULATE);
	}

	public void setRephrasePrompt(String rephrasePrompt) {
		this.rephrasePrompt = Objects.requireNonNullElse(rephrasePrompt, EMPTY_STRING);
	}

	public void setType(RAGType type) {
		this.type = Objects.requireNonNull(type);
	}
}
