package myapps;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.IOException;
import java.util.Map;

public class RuntimeStatsSerde implements Serde<RuntimeStats> {

    private final Serializer<RuntimeStats> serializer = new RuntimeStatsSerializer();
    private final Deserializer<RuntimeStats> deserializer = new RuntimeStatsDeserializer();

    @Override
    public Serializer<RuntimeStats> serializer() {
        return serializer;
    }

    @Override
    public Deserializer<RuntimeStats> deserializer() {
        return deserializer;
    }

    public static class RuntimeStatsSerializer implements Serializer<RuntimeStats> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] serialize(String topic, RuntimeStats data) {
            if (data == null) return null;
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error serializing RuntimeStats", e);
            }
        }
    }

    public static class RuntimeStatsDeserializer implements Deserializer<RuntimeStats> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public RuntimeStats deserialize(String topic, byte[] data) {
            if (data == null) return null;
            try {
                return objectMapper.readValue(data, RuntimeStats.class);
            } catch (IOException e) {
                throw new RuntimeException("Error deserializing RuntimeStats", e);
            }
        }
    }
}
