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

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import java.time.Instant;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor(staticName = "of")
public class Datasource extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long datasourceId;
    @Column(nullable = false)
    private Boolean active = false;
    @Lob
    private String description;
    @Lob
    private String jsonConfig;
    private Instant lastIngestionDate;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private Long tenantId;
    @Column(nullable = false)
    private String scheduling;
    @Column(nullable = false)
    private String driverServiceName;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) !=
                         Hibernate.getClass(o)) {
            return false;
        }
        Datasource that = (Datasource) o;
        return datasourceId != null &&
               Objects.equals(datasourceId, that.datasourceId);
    }

    @Override
    public int hashCode() {
        return 0;
    }
}