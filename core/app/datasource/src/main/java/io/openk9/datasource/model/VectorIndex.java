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
import jakarta.persistence.Lob;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;

import java.sql.Types;

@Entity
@Table(name = "vector_index")
@NamedQuery(
	name = VectorIndex.FETCH_BY_SCHEDULE_ID,
	query = "select vi " +
			" from Scheduler s " +
			" join s.oldDataIndex di " +
			" join di.vectorIndex vi " +
			" where s.scheduleId = :scheduleId"
)
@Getter
@Setter
public class VectorIndex extends K9Entity {

	public static final String FETCH_BY_SCHEDULE_ID = "VectorIndex.fetchByScheduleId";

	@Column(name = "name", nullable = false, unique = true)
	private String name;

	@Column(name = "description", length = 4096)
	private String description;

	@Column(name = "text_embedding_field", nullable = false)
	private String textEmbeddingField;

	@Column(name = "title_field", nullable = false)
	private String titleField;

	@Column(name = "url_field", nullable = false)
	private String urlField;

	@Enumerated(EnumType.STRING)
	@Column(name = "chunk_type")
	private ChunkType chunkType;

	@Lob
	@JdbcTypeCode(Types.LONGNVARCHAR)
	@Column(name = "json_config")
	private String jsonConfig;

	@Column(name = "chunk_window_size")
	private Integer chunkWindowSize = 0;

	@Column(name = "metadata_mapping")
	private String metadataMapping;

	@OneToOne(mappedBy = "vectorIndex")
	private DataIndex dataIndex;

	public enum ChunkType {
		DEFAULT,
		TEXT_SPLITTER,
		TOKEN_TEXT_SPLITTER,
		CHARACTER_TEXT_SPLITTER,
		SEMANTIC_SPLITTER
	}

	public String getIndexName() {
		return getTenant() + "-" + name;
	}
}
