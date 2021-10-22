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

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
public class EnrichItem extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrichItemId;
    @Column(nullable = false)
    private Integer _position;
    @Column(nullable = false)
    private Boolean active = false;
    @Lob
    private String jsonConfig;
    @Column(nullable = false)
    private Long enrichPipelineId;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String serviceName;

    public static List<EnrichItem> findByEnrichPipelineId(
        Long enrichPipelineId) {

        return EnrichItem.list("enrichPipelineId", enrichPipelineId);
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
        EnrichItem that = (EnrichItem) o;
        return enrichItemId != null &&
               Objects.equals(enrichItemId, that.enrichItemId);
    }

    @Override
    public int hashCode() {
        return 0;
    }

}