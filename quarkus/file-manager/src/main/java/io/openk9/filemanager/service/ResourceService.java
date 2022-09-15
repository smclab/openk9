package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


@ApplicationScoped
public class ResourceService {

    public Uni<Resource> createOrUpdate(ResourceDto resourceDto) {

        Resource resource = _resourceMapper.toResource(resourceDto);

        return resource.persist();

    }

    @Inject
    ResourceMapper _resourceMapper;


}
