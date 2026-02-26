package ${package};

import io.openk9.connector.api.HealthResource;
import io.openk9.connector.api.beans.Health;

public class HealthResourceImpl implements HealthResource {

    @Override
    public Health health() {
        Health health = new Health();
        health.setStatus(Health.Status.UP);
        return health;
    }
}