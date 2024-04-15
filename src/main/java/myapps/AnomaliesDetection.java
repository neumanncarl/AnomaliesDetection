package myapps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class AnomaliesDetection {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        // Config
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "anomaly-detection-application");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put("auto.offset.reset", "earliest"); // for testing purposes only

        // Set-up
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> logMessages = builder.stream("small-topic", Consumed.with(Serdes.String(), Serdes.String()));

        // Logic
        KStream<String, JobSequenceInfo> transformedStream = logMessages.transform(AnomalyDetectionTransformer::new);
        transformedStream
                .filter((jobId, jobSequenceInfo) -> jobSequenceInfo.hasAnomaly())
                .mapValues(JobSequenceInfo::toString)
                .to("test1-topic");

        final Topology topology = builder.build();
        final KafkaStreams streams = new KafkaStreams(topology, props);
        final CountDownLatch latch = new CountDownLatch(1);

        Runtime.getRuntime().addShutdownHook(new Thread("streams-shutdown-hook") {
            @Override
            public void run() {
                streams.close();
                latch.countDown();
            }
        });

        try {
            streams.start();
            latch.await();
        } catch (Throwable e) {
            System.exit(1);
        }
        System.exit(0);
    }

    public static EquipmentEvent parseToEquipment(JsonNode node) {
        String jobReference = node.get("jobReference").asText();
        LocalDateTime timestamp = toDateTime(node.get("timestamp").asText());
        String area = node.get("area").asText();
        String equipment = node.get("equipment").asText();
        String equipmentType = node.get("equipmentType").asText();
        String equipmentState = node.get("equipmentState").asText();
        String equipmentToolRecipe = node.get("equipmentToolRecipe").asText();
        String eiEventType = node.get("eiEventType").asText();


        return new EquipmentEvent(jobReference, timestamp, area, equipment, equipmentType, equipmentState, equipmentToolRecipe, eiEventType);
    }

    public static JsonNode parseLogMessage(String message) {
        try {
            return objectMapper.readTree(message);
        } catch (Exception e) {
            e.printStackTrace();
            return objectMapper.createObjectNode();
        }
    }

    private static LocalDateTime toDateTime(String dateTimeStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

}
