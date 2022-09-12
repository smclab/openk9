package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.inject.Inject;
import javax.validation.Valid;

public class ResourceService {

    public Uni<Resource> create(@Valid ResourceDto dto) {

        Resource resource = _resourceMapper.toResource(dto);

        return Panache.withTransaction(resource::persist);

    }

    @Inject
    ResourceMapper _resourceMapper;


}
