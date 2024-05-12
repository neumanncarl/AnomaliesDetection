package myapps;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myapps.EquipmentEvent;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.List;

public class ListEquipmentEventSerde implements Serde<List<EquipmentEvent>> {

    private final Serializer<List<EquipmentEvent>> serializer = new ListEquipmentEventSerializer();
    private final Deserializer<List<EquipmentEvent>> deserializer = new ListEquipmentEventDeserializer();

    @Override
    public Serializer<List<EquipmentEvent>> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<List<EquipmentEvent>> deserializer() {
        return deserializer;
    }

    public static class ListEquipmentEventSerializer implements Serializer<List<EquipmentEvent>> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] serialize(String topic, List<EquipmentEvent> data) {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException("Failed to serialize List<EquipmentEvent>", e);
            }
        }
    }

    public static class ListEquipmentEventDeserializer implements Deserializer<List<EquipmentEvent>> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public List<EquipmentEvent> deserialize(String topic, byte[] data) {
            try {
                return objectMapper.readValue(data, new TypeReference<List<EquipmentEvent>>() {});
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize List<EquipmentEvent>", e);
            }
        }
    }
}
