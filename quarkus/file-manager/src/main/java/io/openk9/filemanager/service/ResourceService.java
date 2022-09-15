package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.openk9.filemanager.repository.ResourceRepository;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class ResourceService {

    public Uni<Resource> createOrUpdate(ResourceDto resourceDto) {

        Resource resource = _resourceMapper.toResource(resourceDto);

        return resource.persist();

    }

    public Uni<Resource> update(String state, String resourceId) {

        Resource resource = repository.findByResourceID(resourceId);
        resource.setState(Resource.State.valueOf("OK"));  // <--- this way works
        return repository.persist(resource);

    }

    @Inject
    ResourceMapper _resourceMapper;

    @Inject
    ResourceRepository repository;


}
