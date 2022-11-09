package io.openk9.datasource.multitenancy.resolver;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

@PersistenceUnitExtension
@RequestScoped
public class DatasourceTenantResolver implements TenantResolver {

    @Override
    public String getDefaultTenantId() {
        return "base";
    }

    @Override
    public String resolveTenantId() {
        return context.getTenantName();
    }

    @Inject
    io.openk9.datasource.tenant.TenantResolver context;

}