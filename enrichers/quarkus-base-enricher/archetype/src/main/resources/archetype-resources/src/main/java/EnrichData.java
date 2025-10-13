package ${package};

import io.openk9.enricher.api.beans.Payload;
import java.util.LinkedHashMap;
import java.util.Map;

public class EnrichData {

    private Payload payload;
    private Map<String, Object> enrichData = new LinkedHashMap<>();

    public EnrichData() {
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Map<String, Object> getEnrichData() {
        return enrichData;
    }

    public void setEnrichData(String name, Object value) {
        enrichData.put(name, value);
    }
}
