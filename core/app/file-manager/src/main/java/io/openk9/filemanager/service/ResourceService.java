package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.*;
import javax.transaction.Transactional;
import javax.validation.Valid;


@ApplicationScoped
public class ResourceService {

    @Inject
    EntityManager em;

    @Transactional
    public void create(ResourceDto resourceDto) {

        Resource resource = _resourceMapper.toResource(resourceDto);
        em.persist(resource);
    }

    @Transactional
    public void update(long id, @Valid ResourceDto dto) {

        try {
            Resource resource = em.find(Resource.class, id);
            Resource updatedResource = _resourceMapper.update(resource, dto);
            em.persist(updatedResource);

        }
        catch (EntityNotFoundException e) {
            logger.info("Entity not found");
        }
    }

    @Transactional
    public Resource findByResourceId(String resourceId) {

        try {
            Query query = em.createQuery("SELECT r FROM Resource r WHERE r.resourceId = :resourceId");
            query.setParameter("resourceId", resourceId);
            return (Resource) query.getSingleResult();
        }
        catch (EntityNotFoundException e) {
            logger.info("Entity not found");
            return null;
        }
    }

    @Transactional
    public Resource findByDatasourceAndFile(String datasourceId, String fileId) {

        try {
            Query query = em.createQuery("SELECT r FROM Resource r WHERE r.datasourceId = :datasourceId AND " +
                    "r.fileId = :fileId");
            query.setParameter("datasourceId", datasourceId);
            query.setParameter("fileId", fileId);
            return (Resource) query.getSingleResult();
        }
        catch (EntityNotFoundException | NoResultException e) {
            logger.info("Entity not found");
            return null;
        }
    }


    @Inject
    ResourceMapper _resourceMapper;

    @Inject
    Logger logger;


}
