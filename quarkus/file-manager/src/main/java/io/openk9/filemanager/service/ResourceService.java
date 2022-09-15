package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

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


    public Uni<Resource> update(String resourceId, @Valid ResourceDto dto) {

        return Resource
                .findByResourceId(resourceId)
                .onItem()
                .ifNull()
                .failWith(() -> new WebApplicationException(
                        "Resource with id of " + resourceId  + " does not exist.", 404))
                .flatMap(resource -> {
                    Resource newResource =
                            _resourceMapper.update((Resource)resource, dto);
                    return newResource.persist();
                });

    }

    @Inject
    ResourceMapper _resourceMapper;


}
