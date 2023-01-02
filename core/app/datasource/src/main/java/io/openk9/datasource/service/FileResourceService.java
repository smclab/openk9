package io.openk9.datasource.service;

import io.openk9.datasource.model.FileResource;
import io.openk9.datasource.model.FileResource_;
import io.openk9.datasource.model.dto.FileResourceDTO;
import io.openk9.datasource.service.util.BaseK9EntityService;
import io.smallrye.mutiny.Uni;
import org.hibernate.FlushMode;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;


@ApplicationScoped
public class FileResourceService extends BaseK9EntityService<FileResource, FileResourceDTO> {


    @Override
    public Class<FileResource> getEntityClass() {
        return FileResource.class;
    }

    public Uni<FileResource> findByDatasourceAndFile(String datasourceId, String fileId) {
        return withStatelessTransaction((s) -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<FileResource> cq = cb.createQuery(FileResource.class);
            Root<FileResource> root = cq.from(FileResource.class);
            cq.where(cb.equal(root.get(FileResource_.datasourceId), datasourceId));
            cq.where(cb.equal(root.get(FileResource_.fileId), fileId));
            return s.createQuery(cq)
                .setFlushMode(FlushMode.MANUAL)
                .getSingleResultOrNull();
        });
    }


    public Uni<FileResource> findByResourceId(String resourceId) {
        return withStatelessTransaction((s) -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<FileResource> cq = cb.createQuery(FileResource.class);
            Root<FileResource> root = cq.from(FileResource.class);
            cq.where(cb.equal(root.get(FileResource_.resourceId), resourceId));
            return s.createQuery(cq)
                .setFlushMode(FlushMode.MANUAL)
                .getSingleResultOrNull();
        });
    }


}
