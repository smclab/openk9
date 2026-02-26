package ${package};

import io.openk9.connector.api.SampleResource;
import io.openk9.connector.api.beans.DatasourcePayload;
import io.openk9.connector.api.beans.IngestionDTO;

import java.util.UUID;

public class SampleResourceImpl implements SampleResource {

    @Override
    public IngestionDTO sample() {
        IngestionDTO ingestionDTO = new IngestionDTO();
        ingestionDTO.setParsingDate("17053261");
        ingestionDTO.setContentId(UUID.randomUUID());
        ingestionDTO.setRawContent("test");
        ingestionDTO.setDatasourcePayload(getDatasourcePayload());
        ingestionDTO.setDatasourceId(984573908L);
        ingestionDTO.setScheduleId(UUID.randomUUID());
        ingestionDTO.setTenantId("23698881");
        ingestionDTO.setAcl(null);
        return ingestionDTO;
    }

    private DatasourcePayload getDatasourcePayload(){
        DatasourcePayload datasourcePayload = new DatasourcePayload();
        datasourcePayload.setAdditionalProperty("uuid", UUID.randomUUID());
        datasourcePayload.setAdditionalProperty("test", "DatasourcePayload test");
        return datasourcePayload;
    }
}
