package io.openk9.filemanager.repository;

import io.openk9.filemanager.model.Resource;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ResourceRepository implements PanacheRepository<Resource> {

    public Resource findByResourceID(String resourceId){
        return (Resource) find("resourceId", resourceId).firstResult();
    }

}
