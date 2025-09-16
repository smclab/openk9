package ${package};

import io.openk9.connector.api.beans.IngestionDTO;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.json.Json;

public class IngestionDTOCodec implements MessageCodec<IngestionDTO, IngestionDTO> {

    @Override
    public void encodeToWire(Buffer buffer, IngestionDTO ingestionDTO) {
        String ingestionDTOtoString = Json.encode(ingestionDTO);
        buffer.appendInt(ingestionDTOtoString.length());
        buffer.appendString(ingestionDTOtoString);
    }

    @Override
    public IngestionDTO decodeFromWire(int pos, Buffer buffer) {
        int length = buffer.getInt(pos);
        pos += 4;
        String json = buffer.getString(pos, pos + length);
        return Json.decodeValue(json, IngestionDTO.class);
    }

    @Override
    public IngestionDTO transform(IngestionDTO ingestionDTO) {
        return ingestionDTO;
    }

    @Override
    public String name() {
        return "ingestionDTOCodec";
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}