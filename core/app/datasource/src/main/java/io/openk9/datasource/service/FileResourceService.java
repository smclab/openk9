package io.openk9.datasource.service;

import com.google.protobuf.Empty;
import io.openk9.datasource.mapper.FileResourceMapper;
import io.openk9.datasource.model.FileResource;
import io.openk9.datasource.model.FileResource_;
import io.openk9.datasource.model.dto.FileResourceDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaDelete;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.function.BiFunction;


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
