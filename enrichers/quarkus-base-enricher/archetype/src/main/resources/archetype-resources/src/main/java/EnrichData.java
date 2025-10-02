package ${package};

import io.openk9.enricher.api.beans.Payload;

public class EnrichData {

    private Payload payload;

    public EnrichData() {
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }
}
