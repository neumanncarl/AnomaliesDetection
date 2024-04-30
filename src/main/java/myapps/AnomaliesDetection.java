package myapps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class AnomaliesDetection {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws IOException {

        // Config
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "anomaly-detection-application4");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put("auto.offset.reset", "earliest"); // for testing purposes only

        // Set-up
        final StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> logMessages = builder.stream("big1", Consumed.with(Serdes.String(), Serdes.String()));

        //Serde<EquipmentEvent> equipmentEventSerde = new JsonSerde<>(EquipmentEvent.class);

        // Logic
        KStream<String, Job> transformedStream = logMessages
//                .groupBy((key, value) -> {
//                    JsonNode parsedMessage = parseLogMessage(value);
//                    return parsedMessage.has("equipment") ? parsedMessage.get("equipment").asText() : "unknown_equipment";
//                })
                .transform(AnomalyDetectionTransformer::new);
        transformedStream
                .filter((jobId, job) -> {if (job != null) return job.hasAnomaly(); return false;})
                .mapValues(Job::toString)
                .to("big-out");

        final Topology topology = builder.build();
        try (KafkaStreams streams = new KafkaStreams(topology, props)) {
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
        }
        System.exit(0);
    }

    public static EquipmentEvent parseToEquipment(JsonNode node) {
        LocalDateTime timestamp = toDateTime(node.get("timestamp").asText());
        String area = node.get("area").asText();
        String equipment = node.get("equipment").asText();
        String equipmentType = node.get("equipmenttype").asText();
        String equipmentState = node.get("equipmentstate").asText();
        String equipmentToolRecipe = node.get("equipmenttoolrecipe").asText();
        String eiEventType = node.get("eieventtype").asText();
        String jobReference = node.get("jobreference").asText();

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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(dateTimeStr, formatter);
    }

}
