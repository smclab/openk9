package io.openk9.filemanager.service;

import io.openk9.filemanager.dto.ResourceDto;
import io.openk9.filemanager.mapper.ResourceMapper;
import io.openk9.filemanager.model.Resource;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;

import static io.quarkus.hibernate.reactive.panache.PanacheEntityBase.persist;

@ApplicationScoped
public class ResourceService {

    public Uni<Resource> create(Resource resource) {

        return resource.persist();

    }

    @Inject
    ResourceMapper _resourceMapper;


}
