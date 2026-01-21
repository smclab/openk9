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

package io.quarkus.hibernate.reactive;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.EntityManagerFactory;

import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.generator.Generator;
import org.hibernate.metamodel.MappingMetamodel;
import org.hibernate.metamodel.mapping.SelectableConsumer;
import org.hibernate.metamodel.mapping.SelectableMapping;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.persister.entity.EntityPersister;

public final class SchemaUtil {

    private SchemaUtil() {
    }

    public static Set<String> getColumnNames(
            EntityManagerFactory entityManagerFactory,
            Class<?> entityType) {
        Set<String> result = new HashSet<>();
        AbstractEntityPersister persister = (AbstractEntityPersister) entityManagerFactory
                .unwrap(SessionFactoryImplementor.class)
                .getMetamodel().entityPersister(entityType);
        if (persister == null) {
            return result;
        }
        for (String propertyName : persister.getPropertyNames()) {
            Collections.addAll(result, persister.getPropertyColumnNames(propertyName));
        }
        return result;
    }

    public static String getColumnTypeName(
            EntityManagerFactory entityManagerFactory, Class<?> entityType,
            String columnName) {
        MappingMetamodel domainModel = entityManagerFactory
                .unwrap(SessionFactoryImplementor.class).getRuntimeMetamodels().getMappingMetamodel();
        EntityPersister entityDescriptor = domainModel.findEntityDescriptor(entityType);
        var columnFinder = new SelectableConsumer() {
            private SelectableMapping found;

            @Override
            public void accept(int selectionIndex, SelectableMapping selectableMapping) {
                if (found == null && selectableMapping.getSelectableName().equals(columnName)) {
                    found = selectableMapping;
                }
            }
        };
        entityDescriptor.forEachSelectable(columnFinder);
        return columnFinder.found.getJdbcMapping().getJdbcType().getFriendlyName();
    }

    public static Generator getGenerator(
            EntityManagerFactory entityManagerFactory,
            Class<?> entityType) {
        MappingMetamodel domainModel = entityManagerFactory
                .unwrap(SessionFactoryImplementor.class).getRuntimeMetamodels().getMappingMetamodel();
        EntityPersister entityDescriptor = domainModel.findEntityDescriptor(entityType);
        return entityDescriptor.getGenerator();
    }
}
