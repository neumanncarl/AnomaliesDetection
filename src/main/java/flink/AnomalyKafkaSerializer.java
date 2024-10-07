package flink;

import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.pojo.Anomaly;

import org.jetbrains.annotations.Nullable;

public class AnomalyKafkaSerializer implements KafkaSerializationSchema<Anomaly> {
    private final String topic;

    public AnomalyKafkaSerializer(String topic) {
        this.topic = topic;
    }

    @Override
    public ProducerRecord<byte[], byte[]> serialize(Anomaly anomaly, @Nullable Long timestamp) {
        byte[] value = null;
        try {
            value = new AnomalySerde().serialize(anomaly);
        } catch (Exception e) {
            System.err.println("Serialization Error: " + e.getMessage());
        }
        return new ProducerRecord<>(topic, value);
    }
}
