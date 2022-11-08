package io.openk9.datasource.multitenancy.resolver;

import io.quarkus.hibernate.orm.PersistenceUnitExtension;
import io.quarkus.hibernate.orm.runtime.tenant.TenantResolver;
import io.vertx.ext.web.RoutingContext;

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
        return context.get("tenantId");
    }

    @Inject
    RoutingContext context;

}