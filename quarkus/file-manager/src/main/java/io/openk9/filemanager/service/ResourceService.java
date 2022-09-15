package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;


@ApplicationScoped
public class ResourceService {

    public Uni<Resource> create(ResourceDto resourceDto) {

        Resource resource = _resourceMapper.toResource(resourceDto);
        return resource.persist();
    }


    public Uni<Resource> update(String resourceId, @Valid ResourceDto dto) {

        Resource resource = _resourceMapper.update((Resource)Resource.findByResourceId(resourceId), dto);

        return resource.persist();

    }

    @Inject
    ResourceMapper _resourceMapper;


}
