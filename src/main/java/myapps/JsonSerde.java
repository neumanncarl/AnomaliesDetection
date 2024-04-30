package myapps;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.Serdes;

import java.io.IOException;

public class JsonSerde<T> implements Serde<T> {

    private final Class<T> targetType;

    public JsonSerde(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public Serializer<T> serializer() {
        return new JsonSerializer<>();
    }

    @Override
    public Deserializer<T> deserializer() {
        return new JsonDeserializer<>(targetType);
    }

    private static class JsonSerializer<T> implements Serializer<T> {
        private final ObjectMapper objectMapper = new ObjectMapper();

        @Override
        public byte[] serialize(String topic, T data) {
            try {
                return objectMapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new SerializationException("Error serializing to JSON", e);
            }
        }
    }

    private static class JsonDeserializer<T> implements Deserializer<T> {
        private final ObjectMapper objectMapper = new ObjectMapper();
        private final Class<T> targetType;

        public JsonDeserializer(Class<T> targetType) {
            this.targetType = targetType;
        }

        @Override
        public T deserialize(String topic, byte[] bytes) {
            if (bytes == null) return null;
            try {
                return objectMapper.readValue(bytes, targetType);
            } catch (IOException e) {
                throw new SerializationException("Error deserializing from JSON", e);
            }
        }
    }
}
