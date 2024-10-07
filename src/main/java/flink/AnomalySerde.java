package flink;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.example.pojo.Anomaly;

import java.io.IOException;

public class AnomalySerde implements DeserializationSchema<Anomaly>, SerializationSchema<Anomaly> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .findAndRegisterModules()  // This is required to handle Java 8 date and time types
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // Optional to ensure date is not written as timestamp

    @Override
    public Anomaly deserialize(byte[] message) throws IOException {
        return objectMapper.readValue(message, Anomaly.class);
    }

    @Override
    public boolean isEndOfStream(Anomaly nextElement) {
        return false;
    }

    @Override
    public byte[] serialize(Anomaly anomaly) {
        try {
            return objectMapper.writeValueAsBytes(anomaly);
        } catch (IOException e) {
            throw new RuntimeException("Unable to serialize Anomaly object", e);
        }
    }

    @Override
    public TypeInformation<Anomaly> getProducedType() {
        return TypeInformation.of(Anomaly.class);
    }
}