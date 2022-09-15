package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.WebApplicationException;


@ApplicationScoped
public class ResourceService {

    public Uni<Resource> create(ResourceDto resourceDto) {

        Resource resource = _resourceMapper.toResource(resourceDto);
        return resource.persist();
    }


    public void update(long id, @Valid ResourceDto dto) {

        logger.info(id);

        Resource resource = (Resource)Resource.findById(id);

        logger.info(resource.getResourceId());
        logger.info(resource.toString());

    }

    @Inject
    ResourceMapper _resourceMapper;

    @Inject
    Logger logger;


}
