package myapps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import java.util.Map;

public class EquipmentEventSerde implements Serde<EquipmentEvent> {

    private final ObjectMapper objectMapper;

    public EquipmentEventSerde() {
        this.objectMapper = new ObjectMapper();
        // Register the JavaTimeModule to handle LocalDateTime serialization
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public Serializer<EquipmentEvent> serializer() {
        return new Serializer<EquipmentEvent>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {
            }

            @Override
            public byte[] serialize(String topic, EquipmentEvent data) {
                try {
                    return objectMapper.writeValueAsBytes(data);
                } catch (Exception e) {
                    throw new RuntimeException("Error serializing EquipmentEvent to JSON", e);
                }
            }

            @Override
            public void close() {
            }
        };
    }

    @Override
    public Deserializer<EquipmentEvent> deserializer() {
        return new Deserializer<EquipmentEvent>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {
            }

            @Override
            public EquipmentEvent deserialize(String topic, byte[] data) {
                if (data == null) return null;
                try {
                    return objectMapper.readValue(data, EquipmentEvent.class);
                } catch (Exception e) {
                    throw new RuntimeException("Error deserializing JSON to EquipmentEvent", e);
                }
            }

            @Override
            public void close() {
            }
        };
    }
}
