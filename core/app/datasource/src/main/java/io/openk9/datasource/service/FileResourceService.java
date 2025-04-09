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

package io.openk9.datasource.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import io.openk9.datasource.mapper.FileResourceMapper;
import io.openk9.datasource.model.FileResource;
import io.openk9.datasource.model.FileResource_;
import io.openk9.datasource.model.dto.base.FileResourceDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;

import com.google.protobuf.Empty;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;


@ApplicationScoped
public class FileResourceService extends BaseK9EntityService<FileResource, FileResourceDTO> {

    FileResourceService(FileResourceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Class<FileResource> getEntityClass() {
        return FileResource.class;
    }

    public Uni<FileResource> findByDatasourceAndFile(String datasourceId, String fileId) {
        return sessionFactory
            .withTransaction(s -> _findByDatasourceAndFile(datasourceId, fileId, s));
    }

    public Uni<FileResource> findByDatasourceAndFile(
        String tenantId, String datasourceId, String fileId) {

        return sessionFactory
            .withTransaction(tenantId, (s, t) -> _findByDatasourceAndFile(datasourceId, fileId, s));
    }

    public Uni<FileResource> findByResourceId(String resourceId) {
        return sessionFactory.withTransaction(s -> _findByResourceId(resourceId, s));
    }

    public Uni<FileResource> findByResourceId(String tenantId, String resourceId) {
        return sessionFactory.withTransaction(tenantId, (s, t) -> _findByResourceId(resourceId, s));
    }

    public Uni<com.google.protobuf.Empty> deleteFileResource(String resourceId) {
        return sessionFactory.withTransaction(s -> _deleteFileResource(resourceId, s));
    }

    public Uni<com.google.protobuf.Empty> deleteFileResource(String tenantId, String resourceId) {
        return sessionFactory
            .withTransaction(tenantId, (s, t) -> _deleteFileResource(resourceId, s));
    }

    private Uni<FileResource> _findByDatasourceAndFile(
        String datasourceId, String fileId, Mutiny.Session s) {

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<FileResource> cq = cb.createQuery(FileResource.class);
        Root<FileResource> root = cq.from(FileResource.class);
        Predicate predicateResult = cb.and(
            cb.equal(root.get(FileResource_.fileId), fileId),
            cb.equal(root.get(FileResource_.datasourceId), datasourceId));
        cq.where(predicateResult);
        return s.createQuery(cq)
            .getSingleResultOrNull();
    }

    private Uni<FileResource> _findByResourceId(String resourceId, Mutiny.Session s) {
        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaQuery<FileResource> cq = cb.createQuery(FileResource.class);
        Root<FileResource> root = cq.from(FileResource.class);
        cq.where(cb.equal(root.get(FileResource_.resourceId), resourceId));
        return s.createQuery(cq)
            .getSingleResultOrNull();
    }

    private Uni<com.google.protobuf.Empty> _deleteFileResource(
        String resourceId, Mutiny.Session s) {

        CriteriaBuilder cb = sessionFactory.getCriteriaBuilder();
        CriteriaDelete<FileResource> delete = cb.
            createCriteriaDelete(FileResource.class);
        Root<FileResource> root = delete.from(FileResource.class);
        delete.where(cb.equal(root.get(FileResource_.resourceId), resourceId));
        return s.createQuery(delete).executeUpdate().replaceWith(Empty.getDefaultInstance());
    }

}
