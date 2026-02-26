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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "file_resource", uniqueConstraints = {
    @UniqueConstraint(name = "uc_fileresource_resource_id", columnNames = {
        "resource_id"}),
    @UniqueConstraint(name = "uc_fileresource_fileid_datasource_id", columnNames = {
        "file_id", "datasource_id"})
})
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
public class FileResource extends K9Entity {

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(name = "file_id", nullable = false)
    private String fileId;

    @Column(name = "datasource_id", nullable = false)
    private String datasourceId;

}
