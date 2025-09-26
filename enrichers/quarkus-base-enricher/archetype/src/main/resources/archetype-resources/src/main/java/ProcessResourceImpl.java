package ${package};

import io.openk9.enricher.api.ProcessResource;
import io.openk9.enricher.api.beans.OpenK9Input;
import io.vertx.core.json.Json;
import jakarta.validation.constraints.NotNull;

public class ProcessResourceImpl implements ProcessResource {

    @Override
    public void process(@NotNull OpenK9Input data) {
        System.out.println(Json.encodePrettily(data));
    }
}
