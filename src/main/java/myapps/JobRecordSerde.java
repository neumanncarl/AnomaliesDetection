package myapps;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.errors.SerializationException;

import java.io.IOException;

public class JobRecordSerde implements Serde<JobRecord> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Serializer<JobRecord> serializer() {
        return new Serializer<JobRecord>() {
            @Override
            public byte[] serialize(String topic, JobRecord data) {
                try {
                    if (data == null) {
                        return null;
                    }
                    return objectMapper.writeValueAsBytes(data);
                } catch (Exception e) {
                    throw new SerializationException("Error serializing JSON message", e);
                }
            }
        };
    }

    @Override
    public Deserializer<JobRecord> deserializer() {
        return new Deserializer<JobRecord>() {
            @Override
            public JobRecord deserialize(String topic, byte[] data) {
                if (data == null) {
                    return null;
                }
                try {
                    return objectMapper.readValue(data, JobRecord.class);
                } catch (IOException e) {
                    throw new SerializationException("Error deserializing JSON message", e);
                }
            }
        };
    }

    @Override
    public void close() {
        // Cleanup resources if necessary
    }

    @Override
    public void configure(java.util.Map<String, ?> configs, boolean isKey) {
        // Configure your serde based on the passed configurations if necessary
    }
}
